# Agentisk Udvikler — Labmanual

*Elevhæfte · 6 timer · du bygger en AI-agent fra bunden*

---

## Hvad du bygger

Over seks blokke bygger du en rigtig AI-agent — ét lag ad gangen. Du starter med
et enkelt modelkald og ender med en agent, der lægger en plan, bruger værktøjer
til at læse og ændre kode, og har sikkerhedsværn mod misbrug. Hver blok genbruger
koden fra den forrige, så det vokser i hænderne på dig.

Det vigtigste du tager med: **en agent er ikke magi.** Det er tre ting — et
HTTP-kald til en sprogmodel, noget JSON, og et loop. Når du har bygget loopet
selv, kan du gennemskue ethvert agent-framework.

---

## Før du går i gang

1. **Vælg dit spor:** `agentic-lab-java/` (Java) eller `agentic-lab-node/` (TypeScript).
2. **Forbind til modellen** — din underviser giver dig adressen:
   ```bash
   export LLM_BASE_URL="http://<gpu-host>:8000/v1"
   export LLM_MODEL="Qwen/Qwen3-Coder-30B-A3B"
   ```
3. **Kør en blok:**
   - Java: `mvn -q compile exec:java -Dexec.mainClass=dk.zealand.agenticlab.Demo -Dexec.args="0"`
   - Node: `npx tsx src/demo.ts 0`
4. Hvis blok 0 svarer, er du klar.

Modellen kører på en maskine, din underviser kontrollerer. **Intet, du skriver,
forlader lokalet** — det er hele pointen med en lokal model.

---

## Sådan læser du hver blok

| Felt | Betydning |
|---|---|
| **Mål** | hvad du skal forstå |
| **Det bygger du** | den konkrete opgave |
| **Tjekpunkt** | hvornår du er færdig |
| **Stræk** | ekstra, hvis du er hurtig |

---

## Ordliste (slå op undervejs)

- **Agent** — et program, der lader en sprogmodel tage handlinger via værktøjer, ikke kun svare med tekst.
- **Værktøjskald (tool call)** — modellen *beder om* at få kørt en funktion (med navn + argumenter). Den kører den ikke selv.
- **Agent-loop / ReAct** — mønstret: ræsonnér → handl (kald værktøj) → observér (resultat) → gentag.
- **Trinbudget** — et loft over antal runder, så agenten ikke looper i det uendelige.
- **Prompt injection** — når tekst, agenten *læser*, prøver at kapre den som var det instruktioner.
- **Godkendelsesport** — et menneske skal sige ja, før agenten må udføre en farlig handling.

---

## Blok 0 — Hej, lokal model

**Mål:** forstå at et modelkald bare er tekst ind → tekst ud.
**Det bygger du:** kør blok 0; send en system- og en brugerbesked, og læs svaret.
**Tjekpunkt:** du får et svar, og du kan forklare, hvad systembeskeden gjorde.
**Stræk:** stil samme spørgsmål tre gange med `temperature` 0 og igen med 1 — hvorfor er svarene mere ens ved 0?

## Blok 1 — Chat → værktøjskald

**Mål:** det afgørende skift — modellen *kører ingenting*. Den udsender en struktureret anmodning.
**Det bygger du:** kør blok 1. Den giver modellen et værktøj og stiller et spørgsmål, der kræver det. Læs `tool_calls` i svaret.
**Tjekpunkt:** du kan se navnet på det værktøj, modellen bad om, og de argumenter, den valgte — og du forstår, at *intet er kørt endnu*.
**Stræk:** stil et spørgsmål, der ikke kræver værktøjet. Hvad gør modellen så?

## Blok 2 — Agent-loopet

**Mål:** luk loopet — kør værktøjet, giv resultatet tilbage, lad modellen svare færdigt.
**Det bygger du:** kør blok 2. Følg med i `Agent`-koden: den læser `tool_calls`, kører værktøjet, lægger resultatet tilbage som en `tool`-besked og spørger modellen igen.
**Tjekpunkt:** agenten svarer korrekt på "Hvor mange linjer er der i App.java?" ved selv at kalde et værktøj.
**Stræk:** find linjen i `Agent`, hvor resultatet lægges tilbage. Hvad sker der, hvis du fjerner den?

## Blok 3 — En værktøjskasse

**Mål:** giv agenten flere værktøjer og en rigtig opgave; oplev hvorfor man skal sætte et trinbudget.
**Det bygger du:** kør blok 3 — agenten har nu `read_file`, `list_dir`, `grep` og `count_lines`. Opgave: *find alle `TODO` og `FIXME` i projektet og opsummér, hvad der mangler.*
**Tjekpunkt:** agenten finder markørerne i `App.java` og giver en kort opsummering.
**Stræk:** sæt trinbudgettet meget lavt og se den ramme loftet. Hvorfor er et loft vigtigt?

## Blok 4 — Værn & red-team *(sikkerhedsblokken)*

**Mål:** gør agenten sikker — og bevis først, at den ikke er det.
**Det bygger du:**
1. Kør blok 4. Den har en **godkendelsesport**: før agenten må skrive til en fil, skal *du* sige `j`.
2. Agenten bliver bedt om at læse `notes.md` — som skjuler en **injektion**, der prøver at få den til at slette `App.java`.
**Tjekpunkt:** du ser injektionen blive fanget — enten fordi agenten behandler værktøjstekst som data, eller fordi godkendelsesporten stopper den, når du siger `n`.
**Stræk:** fjern ét forsvar ad gangen (system-reglen, indkapslingen, porten) og find ud af, *hvilket der bar læsset.*

## Blok 5 — Multi-agent

**Mål:** se hvorfor man deler en stor opgave op i specialiserede roller.
**Det bygger du:** kør blok 5 — en **planlægger** deler målet i trin, en **arbejder** udfører trin 1 med værktøjer, og en **sikkerhedsanmelder** gennemgår resultatet.
**Tjekpunkt:** du får en plan, et stykke arbejde og en anmeldelse — tre forskellige "stemmer".
**Stræk:** hvor i kæden tilføjer rolledelingen ventetid? Hvornår er det besværet værd?

---

## Afsluttende opgave (vælg én)

Byg videre på din agent og demonstrér den på en af disse:

- **Log-triage** — agenten læser en adgangslog og flager mistænkelige poster.
- **Afhængigheds-audit** — agenten tjekker `pom.xml`/`package.json` mod en "kendt-dårlig"-liste.
- **Kode-review** — agenten gennemgår en diff for sårbarheder (injection, hemmeligheder, usikker deserialisering).
- **Sikker-kodning-tutor** — agenten forklarer en sårbar kodestump og foreslår en rettelse (med godkendelse, før den omskriver).

---

## Hjælp & fejlfinding

- **Intet svar / timeout i blok 0** → tjek at `LLM_BASE_URL` er sat korrekt, og at modellen kører. Spørg din underviser.
- **Agenten "glemmer" hvad den lige har set** → kontekstvinduet er for lille; sig det til underviseren.
- **Agenten kører i ring** → den mangler et trinbudget — det er netop pointen i blok 3.
- **"Den sagde, den kørte værktøjet, men der skete ingenting"** → modellen *kører aldrig* værktøjet. Det gør din kode. Genlæs blok 1.
- **Værktøjskald kommer tilbage som rå tekst** → modellen eller serveren er ikke sat op til værktøjskald; det er en underviser-opgave.
