package tutorials.human_in_the_loop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.agentic.scope.AgenticScopeKey;
import dev.langchain4j.agentic.scope.AgenticScopeSerializer;
import dev.langchain4j.agentic.scope.AgenticScopeStore;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/** Stores AgenticScopes as JSON files in a directory. */
public final class FileSystemAgenticScopeStore implements AgenticScopeStore {

    private static final String FILE_SUFFIX = ".json";
    private static final String KEY_FIELD = "key";
    private static final String SCOPE_FIELD = "scope";

    private final Path directory;
    private final ObjectMapper objectMapper;

    public FileSystemAgenticScopeStore(Path directory) {
        this(directory, new ObjectMapper().findAndRegisterModules());
    }

    public FileSystemAgenticScopeStore(Path directory, ObjectMapper objectMapper) {
        this.directory = directory.toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean save(AgenticScopeKey key, DefaultAgenticScope scope) {
        try {
            Files.createDirectories(directory);

            ObjectNode document = objectMapper.createObjectNode();
            document.set(KEY_FIELD, objectMapper.valueToTree(key));
            JsonNode scopeNode = objectMapper.readTree(AgenticScopeSerializer.toJson(scope));
            document.set(SCOPE_FIELD, scopeNode);

            Path target = pathFor(key);
            Path temp = Files.createTempFile(directory, "scope-", ".tmp");
            try {
                objectMapper.writeValue(temp.toFile(), document);
                try {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save AgenticScope: " + key, e);
        }
    }

    @Override
    public Optional<DefaultAgenticScope> load(AgenticScopeKey key) {
        Path file = pathFor(key);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }

        try {
            JsonNode document = objectMapper.readTree(file.toFile());
            AgenticScopeKey storedKey = objectMapper.treeToValue(
                    document.get(KEY_FIELD), AgenticScopeKey.class);
            if (!key.equals(storedKey)) {
                return Optional.empty();
            }
            String scopeJson = objectMapper.writeValueAsString(document.get(SCOPE_FIELD));
            return Optional.of(AgenticScopeSerializer.fromJson(scopeJson));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load AgenticScope: " + key, e);
        }
    }

    @Override
    public boolean delete(AgenticScopeKey key) {
        try {
            return Files.deleteIfExists(pathFor(key));
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete AgenticScope: " + key, e);
        }
    }

    @Override
    public Set<AgenticScopeKey> getAllKeys() {
        if (!Files.isDirectory(directory)) {
            return Set.of();
        }

        Set<AgenticScopeKey> keys = new HashSet<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(FILE_SUFFIX))
                    .forEach(file -> {
                        try {
                            JsonNode document = objectMapper.readTree(file.toFile());
                            keys.add(objectMapper.treeToValue(
                                    document.get(KEY_FIELD), AgenticScopeKey.class));
                        } catch (IOException e) {
                            throw new IllegalStateException(
                                    "Could not read AgenticScope file: " + file, e);
                        }
                    });
            return keys;
        } catch (IOException e) {
            throw new IllegalStateException("Could not list AgenticScopes in " + directory, e);
        }
    }

    private Path pathFor(AgenticScopeKey key) {
        try {
            byte[] keyJson = objectMapper.writeValueAsBytes(key);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(keyJson);
            StringBuilder filename = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                filename.append(String.format("%02x", value));
            }
            return directory.resolve(filename + FILE_SUFFIX);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create filename for AgenticScope: " + key, e);
        }
    }
}
