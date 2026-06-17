package dk.zealand.agenticlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.BiPredicate;

/**
 * Agent-loopet — det ene begreb hele laben bygger op til:
 *
 *   spørg model -> den beder om værktøjer -> VI kører dem -> giv resultat tilbage
 *               -> indtil modellen holder op med at bede om værktøjer (endeligt svar)
 *               -> eller trinbudgettet løber tør (blok 3: agenter looper; sæt altid loft)
 *
 * Godkendelsesporten (blok 4) sidder mellem "model vil kalde et værktøj" og
 * "vi kører det rent faktisk" for værktøjer markeret som farlige.
 */
public class Agent {

    private final LlmClient llm;
    private final Tools tools;
    private final String systemPrompt;
    private final ObjectMapper M = new ObjectMapper();

    /** (værktøjsnavn, args) -> tilladt? Som standard tillades alt. */
    private BiPredicate<String, JsonNode> approver = (name, args) -> true;

    public Agent(LlmClient llm, Tools tools, String systemPrompt) {
        this.llm = llm;
        this.tools = tools;
        this.systemPrompt = systemPrompt;
    }

    public Agent withApprover(BiPredicate<String, JsonNode> approver) {
        this.approver = approver;
        return this;
    }

    public String run(String userGoal, int maxSteps) throws Exception {
        ArrayNode messages = M.createArrayNode();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userGoal));

        for (int step = 0; step < maxSteps; step++) {
            JsonNode assistant = llm.chat(messages, tools.schemaArray());
            messages.add(assistant);                       // behold hele historikken

            JsonNode calls = assistant.path("tool_calls");
            if (!calls.isArray() || calls.isEmpty()) {
                return assistant.path("content").asText();  // modellen er færdig
            }

            for (JsonNode call : calls) {
                String id = call.path("id").asText();
                String name = call.path("function").path("name").asText();
                JsonNode args = M.readTree(call.path("function").path("arguments").asText("{}"));

                String result;
                if (tools.needsApproval(name) && !approver.test(name, args)) {
                    result = "AFVIST: et menneske afviste handlingen.";
                } else {
                    result = tools.execute(name, args);
                }

                // Blok 4-mitigering: indkapsl værktøjsoutput, så modellen behandler
                // det som DATA, ikke som nye instruktioner (forsvar mod prompt injection).
                messages.add(toolMessage(id, "<tool_result>\n" + result + "\n</tool_result>"));
            }
        }
        return "STOPPET: trinbudget (" + maxSteps + ") overskredet.";
    }

    private ObjectNode message(String role, String content) {
        ObjectNode m = M.createObjectNode();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private ObjectNode toolMessage(String toolCallId, String content) {
        ObjectNode m = M.createObjectNode();
        m.put("role", "tool");
        m.put("tool_call_id", toolCallId);
        m.put("content", content);
        return m;
    }
}
