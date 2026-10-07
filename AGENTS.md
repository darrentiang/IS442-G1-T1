# AGENTS.md — Study Buddy Matcher (IS442)

Instructions for any AI coding agent in this repo (Claude Code, Codex, Copilot, Cursor and others). The design spec is `docs/FOUNDATION.md`; the look and UI behaviour are in `docs/UI_GUIDELINES.md`. Build to them, do not redesign them. If something is ambiguous or seems wrong, stop and ask instead of improvising.

## Project

IS442 (Object-Oriented Programming, SMU) group project: a web app that matches students with study partners using a deterministic scoring engine, and lets study groups coordinate sessions through availability overlap. Graded on visible OOP design, Java coding conventions, and a live demo. Most of the team is new to Java, so keep code simple and readable.

## Stack (fixed — do not add anything)

- Java 21, Maven (use the wrapper: `.\mvnw.cmd` on Windows, `./mvnw` elsewhere)
- Spring Boot 3: `spring-boot-starter-web` and `spring-boot-starter-thymeleaf` only
- Thymeleaf server-rendered pages, plain HTML, one hand-written stylesheet (`static/css/app.css`)
- In-memory storage (HashMap repositories). No database.

Run: `.\mvnw.cmd spring-boot:run`, then open http://localhost:8080.

## Hard rules

1. **No database, no JPA, no Spring Data, no Lombok, no validation starter, no CSS or JS frameworks, no CDN links, no new dependencies** of any kind without team agreement.
2. **Domain firewall:** nothing in `com.studybuddy.model` or `com.studybuddy.engine` may import `org.springframework.*` or `jakarta.*`. Plain Java only.
3. **Spring annotations allowed:** `@SpringBootApplication`, `@Controller`, `@RestController`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@Service`, `@Repository` (InMemory implementations only), `@Component` (startup tasks only, e.g. SeedDataLoader). Constructor injection only. **Never field `@Autowired`.**
4. **No `Optional`, no streams, no lambdas** in `model`, `engine`, `repository` or `service` — return the object or `null`, use plain loops.
5. **Availability is `boolean[7][15]`** — day 0 = Monday, hour index 0 = 08:00. No `java.time` in it. `LocalDate`/`LocalDateTime` only where FOUNDATION.md shows them.
6. **Java Coding Conventions:** 4-space indent, braces on the same line, PascalCase classes, camelCase members, UPPER_SNAKE constants, one class per file.
7. **Javadoc on every class and every public method:** one line stating its responsibility.
8. **`contactNumber` on StudentProfile has NO getter.** Read it only through `toPublicView()` / `toConnectedView()`. Never add a getter.
9. **Shared classes** (everything in FOUNDATION.md section 3) are not changed by an agent. If a task seems to need a new field or class there, stop and tell the user.

## Where things go

Packages are by layer. Dependencies point down only: controller → service → repository → model.

| Package | Put here | Naming | Rules |
|---|---|---|---|
| `com.studybuddy.model` | Entities and enums | `Student`, `StudyGroup`, `RequestStatus` | Plain Java. No Spring, no repositories, no HTML |
| `com.studybuddy.engine` | Matching: criteria, strategies, config, results | `AvailabilityCriterion`, `BalancedStrategy` | Plain Java. Criteria implement `MatchCriterion`, strategies implement `MatchingStrategy` |
| `com.studybuddy.repository` | Storage | `XRepository` (interface) + `InMemoryXRepository` (`@Repository`) | The ONLY place that holds data. Add finder methods your feature needs to the interface and implementation |
| `com.studybuddy.service` | Business rules | `XService` (`@Service`) | Calls repositories and other services. Returns model objects, never HTML, never touches `Model` |
| `com.studybuddy.controller` | Web requests | `XController` (`@Controller`); JSON endpoints in `XApiController` (`@RestController`) | Thin: read input, call a service, choose a page. No business rules, no data storage |
| `com.studybuddy.seed` | Startup demo data | `SeedDataLoader` (`@Component`) | Only saves through repositories |

Controllers by area: `CourseController`, `ProfileController`, `MatchController`, `ConnectionController`, `GroupController`, `SessionController`, `AdminController`. Add to the existing one for your area before creating a new one.

**URLs:** lowercase, kebab-case, by area: `/profile`, `/matches`, `/connections`, `/groups/{id}`, `/groups/{id}/join`, `/admin/config`. GET shows a page; POST performs an action. JSON endpoints start with `/api/`.

**Templates:** `src/main/resources/templates/<area>/<page>.html`, kebab-case file names.

| Folder | Holds |
|---|---|
| `templates/fragments/` | Shared pieces: `head`, `nav`, `toast`, `field-error`, `badge`, `empty-state` |
| `templates/shared/` | Pages no single area owns (user picker, home, courses) |
| `templates/student/` | Profile, find buddies, candidate profile, my buddies |
| `templates/group/` | Group list, group detail, sessions |
| `templates/admin/` | Users, matching config |

Every page includes `fragments/head`, `fragments/nav` and `fragments/toast`. Copy `templates/shared/courses.html` as the starting point for any new page.

**Static files:**
- CSS lives only in `static/css/app.css`. Pages use its classes (see UI_GUIDELINES.md). No `<style>` blocks, no `style=""` attributes.
- JavaScript lives only in `static/js/`, one file per page that needs it (`admin-config.js`, `availability-grid.js`). No inline `<script>` code.

**Docs:** `docs/FOUNDATION.md` (design), `docs/UI_GUIDELINES.md` (look and behaviour), `docs/*.mmd` (class diagrams).

## Adding a feature

Follow the Course example in FOUNDATION.md section 6. Four pieces, top to bottom, plus a page:

1. Model class already exists (shared spine) — use it as is.
2. Repository: add any finder method you need to `XRepository` and `InMemoryXRepository`.
3. Service: the rules, in `XService`.
4. Controller: a thin method in your area's controller.
5. Page: `templates/<area>/<page>.html`, built from the shared classes and fragments.

## Forms and feedback

Pages are server-rendered, so a button submits a form and the server returns a new page. Feedback follows four patterns (details in UI_GUIDELINES.md):

| Situation | Do this |
|---|---|
| A field is invalid | Do NOT redirect. Put a `Map<String, String> fieldErrors` (field name → message) in the model and return the same view; values stay filled; the `field-error` fragment shows the message under the field |
| An action finished (success or failure not tied to a field) | Use Post/Redirect/Get: add flash attributes `toastMessage` and `toastType` (`success` or `error`) via `RedirectAttributes`, then `return "redirect:/..."`. The `toast` fragment shows it on the next page |
| A lasting page state | A banner in the page itself |
| A list is empty | The `empty-state` fragment, never a blank table |

Validation is plain Java in the service (it returns the error map or throws); no validation annotations.
