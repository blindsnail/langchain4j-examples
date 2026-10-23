package Tutorials;

import dev.langchain4j.service.Result;

interface Assistant {

    Result<String> chat(String userMessage);
}
