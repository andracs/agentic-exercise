package dk.zealand.agenticlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Hele "tal med modellen"-laget i én klasse — uden framework.
 * Den sender en OpenAI-formet chat-completions-forespørgsel til dit vLLM-endpoint
 * og returnerer choices[0].message (som kan indeholde tool_calls).
 *
 * Blok 0-5 bygger udelukkende på denne. Eleverne bør læse den én gang og se,
 * at der ikke er nogen magi: en agent er HTTP + JSON + et loop.
 */
public class LlmClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;   // fx http://<gpu-host>:8000/v1
    private final String model;

    public LlmClient(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    /** Ét chat-completions-kald. Send tools=null når du ikke vil bruge værktøjskald. */
    public JsonNode chat(ArrayNode messages, ArrayNode tools) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.set("messages", messages);
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", tools);
            body.put("tool_choice", "auto");
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer not-needed")   // vLLM ignorerer den
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("LLM HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return mapper.readTree(resp.body()).path("choices").path(0).path("message");
    }
}
