package dk.zealand.agenticlab;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * KONTRASTFILEN. Kør den FØRST efter eleverne har bygget loopet i hånden
 * (blok 2-3). Den gør det samme — men @Tool-metoderne opdages automatisk, og
 * værktøjs-loopet køres for dig af AiServices.
 *
 * Pointen ligger i forskellen: ~50 linjers Agent.java skrumper til et interface
 * + annoterede metoder. Framework'et er ikke magi; det er loopet, du allerede
 * har skrevet.
 *
 *   mvn -q compile exec:java -Dexec.mainClass=dk.zealand.agenticlab.LangChain4jDemo
 *
 * (API-navnene følger LangChain4j 1.x; hvis din version har omdøbt
 *  chatLanguageModel(...) til chatModel(...), så ret det ene builder-kald.)
 */
public class LangChain4jDemo {

    static class DevTools {
        @Tool("Tæl linjerne i en tekstfil under sample-projektet")
        int countLines(String path) throws IOException {
            return Files.readAllLines(Path.of("sample").resolve(path)).size();
        }

        @Tool("Læs en tekstfil under sample-projektet")
        String readFile(String path) throws IOException {
            return Files.readString(Path.of("sample").resolve(path));
        }
    }

    interface CodingAgent {
        @SystemMessage("Du er en kodeassistent. Brug værktøjer til at inspicere filer, før du svarer.")
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        var model = OpenAiChatModel.builder()
                .baseUrl(System.getenv().getOrDefault("LLM_BASE_URL", "http://localhost:8000/v1"))
                .apiKey("not-needed")
                .modelName(System.getenv().getOrDefault("LLM_MODEL", "Qwen/Qwen3-Coder-30B-A3B"))
                .temperature(0.2)
                .build();

        CodingAgent agent = AiServices.builder(CodingAgent.class)
                .chatLanguageModel(model)
                .tools(new DevTools())
                .build();

        System.out.println(agent.chat("Hvor mange linjer er der i App.java, og hvad gør den?"));
    }
}
