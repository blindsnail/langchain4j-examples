package tutorials.blackboard;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.blackboard.BlackboardPlanner;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import util.ChatModelProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static dev.langchain4j.agentic.patterns.blackboard.ConflictResolutionStrategy.agentOfType;
import static dev.langchain4j.agentic.patterns.blackboard.ConflictResolutionStrategy.declarationOrder;

/**
 * Demonstrates data-driven agent activation with a medical-diagnostics blackboard.
 */
public class BlackboardAgentExample {

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();
    private static final BufferedReader CONSOLE = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        AgentMonitor monitor = new AgentMonitor();

//        MedicalDiagnostics diagnostics = createDiagnostics(monitor);
        MedicalDiagnostics diagnostics = createReviewedDiagnostics(monitor);

        String diagnose = diagnostics.diagnose(
                "Patient reports fatigue, nausea, and dizziness.",
                "Hemoglobin is slightly below normal; other values are within range.",
                "Taking metformin and an over-the-counter cold medicine.");
        System.out.println(diagnose);

        HtmlReportGenerator.generateReport(monitor, Path.of("blackboard-agent.html"));
    }

    /**
     * Builds the basic workflow; eligible agents are selected in declaration order.
     */
    public static MedicalDiagnostics createDiagnostics(AgentMonitor monitor) {
        SymptomExtractor symptomExtractor = AgenticServices.agentBuilder(SymptomExtractor.class)
                .chatModel(CHAT_MODEL).build();
        LabResultAnalyzer labAnalyzer = AgenticServices.agentBuilder(LabResultAnalyzer.class)
                .chatModel(CHAT_MODEL).build();
        DrugInteractionChecker drugInteractionChecker = AgenticServices.agentBuilder(DrugInteractionChecker.class)
                .chatModel(CHAT_MODEL).build();
        DiagnosisAgent diagnosisAgent = AgenticServices.agentBuilder(DiagnosisAgent.class)
                .chatModel(CHAT_MODEL).build();

        return AgenticServices.plannerBuilder(MedicalDiagnostics.class)
                .subAgents(symptomExtractor, labAnalyzer, drugInteractionChecker, diagnosisAgent)
                .planner(BlackboardPlanner::new)
                .listener(monitor)
                .outputKey("diagnosis")
                .build();
    }

    /**
     * Adds state-aware priority and human review, allowing rejected diagnoses to be revised.
     */
    public static MedicalDiagnostics createReviewedDiagnostics(AgentMonitor monitor) {
        SymptomExtractor symptomExtractor = AgenticServices.agentBuilder(SymptomExtractor.class)
                .chatModel(CHAT_MODEL).build();
        LabResultAnalyzer labAnalyzer = AgenticServices.agentBuilder(LabResultAnalyzer.class)
                .chatModel(CHAT_MODEL).build();
        DrugInteractionChecker drugInteractionChecker = AgenticServices.agentBuilder(DrugInteractionChecker.class)
                .chatModel(CHAT_MODEL).build();
        DiagnosisAgent diagnosisAgent = AgenticServices.agentBuilder(DiagnosisAgent.class)
                .chatModel(CHAT_MODEL).build();

        HumanInTheLoop humanReview = AgenticServices.humanInTheLoopBuilder()
                .description("Review the diagnosis and either approve it or request additional analysis")
                .outputKey("symptoms")
                .inputKey(String.class, "diagnosis")
                .responseProvider(scope -> {
                    String diagnosis = scope.readState("diagnosis", "");
                    String symptoms = scope.readState("symptoms", "");

                    System.out.println("\n=== Diagnosis for review ===");
                    System.out.println(diagnosis);
                    while (true) {
                        System.out.print("Approve this diagnosis? [y/n]: ");
                        String answer = readConsoleLine();
                        if ("y".equalsIgnoreCase(answer) || "yes".equalsIgnoreCase(answer)) {
                            scope.writeState("approvedDiagnosis", diagnosis);
                            return symptoms;
                        }
                        if ("n".equalsIgnoreCase(answer) || "no".equalsIgnoreCase(answer)) {
                            System.out.print("What additional patient information should be considered? ");
                            String additionalInformation = readConsoleLine();
                            if (additionalInformation.isBlank()) {
                                System.out.println("Please provide additional information so the diagnosis can be reconsidered.");
                                continue;
                            }
                            return symptoms + ". Additional patient information: " + additionalInformation;
                        }
                        System.out.println("Please answer y or n.");
                    }
                })
                .build();

        return AgenticServices.plannerBuilder(MedicalDiagnostics.class)
                .subAgents(symptomExtractor, labAnalyzer, drugInteractionChecker, diagnosisAgent, humanReview)
                .planner(() -> new BlackboardPlanner(
                        scope -> scope.hasState("approvedDiagnosis"),
                        agentOfType(DrugInteractionChecker.class, scope -> {
                            String symptoms = scope.readState("symptoms", "").toLowerCase();
                            return symptoms.contains("medication") || symptoms.contains("drug");
                        }).or(declarationOrder())))
                .listener(monitor)
                .outputKey("approvedDiagnosis")
                .build();
    }

    private static String readConsoleLine() {
        try {
            String line = CONSOLE.readLine();
            if (line == null) {
                throw new IllegalStateException("Console input ended before the diagnosis was reviewed.");
            }
            return line.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the diagnosis review from the console.", e);
        }
    }

    public interface MedicalDiagnostics {
        String diagnose(@V("patientInput") String patientInput,
                        @V("labResults") String labResults,
                        @V("medications") String medications);
    }

    public interface SymptomExtractor {
        @SystemMessage("Extract the patient's reported symptoms from the patient input. Return a concise description.")
        @UserMessage("Patient input: {{patientInput}}")
        @Agent(value = "Extract symptoms from patient input", outputKey = "symptoms")
        String extract(@V("patientInput") String patientInput);
    }

    public interface LabResultAnalyzer {
        @SystemMessage("Analyze the supplied laboratory results and summarize clinically relevant abnormalities.")
        @UserMessage("Symptoms: {{symptoms}}\nLaboratory results: {{labResults}}")
        @Agent(value = "Analyze laboratory results in light of the symptoms", outputKey = "labAnalysis")
        String analyze(@V("symptoms") String symptoms, @V("labResults") String labResults);
    }

    public interface DrugInteractionChecker {
        @SystemMessage("Review medications for possible interactions or medication-related effects relevant to the symptoms.")
        @UserMessage("Symptoms: {{symptoms}}\nMedications: {{medications}}")
        @Agent(value = "Check medications for possible interactions", outputKey = "drugInteractionAnalysis")
        String check(@V("symptoms") String symptoms, @V("medications") String medications);
    }

    public interface DiagnosisAgent {
        @SystemMessage("""
                    Synthesize the available clinical evidence into a cautious, concise diagnostic assessment.
                    Clearly state uncertainty and recommend professional medical evaluation.
                """)
        @UserMessage("Symptoms: {{symptoms}}\nLab analysis: {{labAnalysis}}\nMedication analysis: {{drugInteractionAnalysis}}")
        @Agent(value = "Produce a diagnosis from the available evidence", outputKey = "diagnosis")
        String diagnose(@V("symptoms") String symptoms,
                        @V("labAnalysis") String labAnalysis,
                        @V("drugInteractionAnalysis") String drugInteractionAnalysis);
    }
}
