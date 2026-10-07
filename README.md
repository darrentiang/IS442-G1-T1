# Study Buddy Matcher (IS442-G1-T1)

A web app that matches SMU students to study partners with a deterministic scoring engine, and helps study groups coordinate sessions through availability overlap.

## Stack

- Java 21, Maven (via the included Maven Wrapper — no Maven install needed)
- Spring Boot 3.x (`spring-boot-starter-web`, `spring-boot-starter-thymeleaf`)
- Thymeleaf server-side pages, plain HTML, one hand-written stylesheet (`static/css/app.css`)
- In-memory storage (HashMap repositories) — no database

## Running the app

The only prerequisite is a JDK 21 or newer, with `JAVA_HOME` pointing at it. From the project root:

| Shell | Command |
|---|---|
| PowerShell | `.\mvnw.cmd spring-boot:run` |
| cmd.exe | `mvnw spring-boot:run` |
| macOS / Linux / Git Bash | `./mvnw spring-boot:run` |

Then open <http://localhost:8080/courses>. The first run downloads Maven and the project dependencies, so it takes a few minutes; later runs start in seconds. Stop the app with `Ctrl+C`.

Other useful goals, using the same command form: `compile`, and `javadoc:javadoc` (generates the API reference into `target/reports/apidocs/`).

## Design

The design spec — layers, packages, shared classes, class diagrams, and the worked Course example every feature follows — is [docs/FOUNDATION.md](docs/FOUNDATION.md). The look and UI feedback patterns are in [docs/UI_GUIDELINES.md](docs/UI_GUIDELINES.md). Rules for AI coding agents are in [AGENTS.md](AGENTS.md).

## Team

Darren, Hong Yi, Karina, Kenneth, Rachel, Weilin, Ela
