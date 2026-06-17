# Agentisk udvikler-lab — Java-spor

Samme 6-bloks opbygning som hovedlaben, i Java til en Datamatiker/PBA-cohort.
Hele agent-motoren er **ren Java (JDK `HttpClient` + Jackson)**, så eleverne ser
hver enkelt mekanisme. Én enkelt LangChain4j-fil til sidst viser framework'et
folde det loop sammen, de selv har bygget i hånden — den forskel er pointen.

## Forudsætninger
- JDK 21+, Maven
- Den lejede GPU-instans, der serverer en model med værktøjskald via vLLM
  (OpenAI-kompatibel). Sæt hvor den bor:

```bash
export LLM_BASE_URL="http://<gpu-host>:8000/v1"
export LLM_MODEL="Qwen/Qwen3-Coder-30B-A3B"
```

## Kør en blok
```bash
mvn -q compile exec:java -Dexec.mainClass=dk.zealand.agenticlab.Demo -Dexec.args="2"
#                                                                                   ^ blok 0..5
```
Første `mvn compile` henter Jackson + LangChain4j — det build er også røgtesten.

## Sådan mapper blokkene til koden

| Blok | Det eleverne gør | Hvor |
|---|---|---|
| 0. Hej, model | ét svar, tekst ind/ud | `Demo.block0_hej` → `LlmClient` |
| 1. Chat → værktøjskald | se modellen *bede om* et værktøj (ikke køre det) | `Demo.block1_vaerktoejskald` |
| 2. Agent-loopet | kør værktøjet, giv det tilbage, afslut | `Demo.block2_loop` → `Agent` |
| 3. En værktøjskasse | flertrins-opgave + trinbudget | `Demo.block3_vaerktoejskasse` → `Tools` |
| 4. Værn & red-team | godkendelsesport + forsvar mod prompt injection | `Demo.block4_vaern` |
| 5. Multi-agent | planlægger → arbejder → sikkerhedsanmelder | `Demo.block5_multiagent` |
| ★ Framework-pointen | loopet, kørt af LangChain4j | `LangChain4jDemo` |

## Delene
- **`LlmClient`** — ét `HttpClient`-POST til `/chat/completions`. Hele "tal med
  modellen"-laget. Intet framework, ingen magi.
- **`Tools`** — et register: hvert værktøj = et JSON-skema (det modellen ser) +
  en Java-implementering (det der kører). Med **sti-traverseringsværn** og et
  **godkendelseskrævende** `write_file`. `devToolbox(rod)` sandbox'er alle filop
  under `rod`.
- **`Agent`** — loopet: spørg → kør værktøjer → giv tilbage → gentag, med
  trinbudget og godkendelsesport. ~50 linjer; læs det én gang, så er begrebet dit.
- **`Demo`** — én kørbar main pr. blok.
- **`LangChain4jDemo`** — samme funktion via `@Tool` + `AiServices`. Kør den
  *efter* blok 3, så forskellen lærer pointen.

## Blok 4-red-teamet
`sample/notes.md` skjuler en HTML-kommentar, der beder agenten slette `App.java`.
Kør blok 4 og se:
1. En naiv agent (uden værn) prøver at adlyde tekst, den *læste fra et værktøj*.
2. Forsvaret stopper den: `write_file` kræver godkendelse, værktøjsoutput er pakket
   i `<tool_result>`-tags, og systemprompten siger, at værktøjstekst er data — ikke
   ordrer. Lad eleverne fjerne ét forsvar ad gangen og se, hvilket der bar læsset.

## Noter
- LangChain4j-API'et følger 1.x-linjen; har din version omdøbt
  `chatLanguageModel(...)` til `chatModel(...)`, så ret det ene builder-kald og
  bump `langchain4j.version` i `pom.xml` til nyeste 1.x.
- Node.js/TypeScript-udgaven ligger i `agentic-lab-node/` med præcis samme struktur.
