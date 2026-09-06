# CLAUDE.md — Study Buddy Matcher (IS442 group project)

Rules for any AI coding agent working in this repo. These apply to every session, every team member, every territory. The design spec is `docs/FOUNDATION.md` — build to it, do not redesign it. If something is ambiguous or seems wrong, stop and ask instead of improvising.

## What this project is

IS442 (Object-Oriented Programming, SMU) group project: a Study Buddy Matcher web app. Students are matched to study partners by a deterministic scoring engine; groups coordinate sessions via availability overlap. Graded on visible OOP design (SRP, layering, encapsulation, polymorphism), Java coding conventions, and a live demo. Seven-person team, most of whom are new to Java — every line must stay explainable by its owner.

## Stack (fixed — do not add anything)

- Java 21, Maven
- Spring Boot 3.x: `spring-boot-starter-web`, `spring-boot-starter-thymeleaf` ONLY
- Thymeleaf server-side rendering; plain HTML; Pico.css via CDN (no npm, no build step)
- In-memory storage (HashMap repositories). No database.

## Hard rules

1. **No database, no JPA, no Spring Data, no Lombok, no new dependencies** without team agreement recorded in the group chat.
2. **Domain firewall:** nothing in `com.studybuddy.model` or `com.studybuddy.engine` may import `org.springframework.*` or `jakarta.*`. Plain Java only.
3. **Spring footprint capped:** `@SpringBootApplication`, `@Controller`/`@RestController` + mappings, `@Service`, `@Repository` on InMemory implementations, constructor injection. **Never field `@Autowired`.**
4. **No `Optional`, no streams, no lambdas** in shared/spine code — return the object or `null`; plain loops. Reason: not taught in this module; every member must be able to explain all code.
5. **No `java.time` in the availability model** — the grid is `boolean[7][15]`, day 0 = Monday, hour index 0 = 08:00. `LocalDate`/`LocalDateTime` only where FOUNDATION.md shows them.
6. **Java Coding Conventions:** 4-space indent, braces on same line, PascalCase classes, camelCase members, one class per file.
7. **Javadoc on every class and every public method** — one line stating its responsibility. `mvn javadoc:javadoc` output is part of the graded documentation.
8. **`contactNumber` on StudentProfile has NO getter.** Access only via `toPublicView()` / `toConnectedView()`. This is a graded encapsulation requirement — never "fix" it.
9. **Layering:** controller → service → repository → model. Controllers hold no business logic; services build no HTML; only repositories hold data.
10. Follow the four-layer pattern shown by the Course example in `docs/FOUNDATION.md` section 6 when adding any feature.

## Change protocol

Shared classes (everything in FOUNDATION.md section 3) change only after a one-sentence proposal in the team group chat is agreed. Doc, diagram, and stub update together in one commit.
