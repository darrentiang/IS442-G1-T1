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
2. **Domain firewall:** nothing in `com.studybuddy.model`, `com.studybuddy.engine` or `com.studybuddy.importer` may import `org.springframework.*` or `jakarta.*`. Plain Java only.
3. **Spring annotations allowed:** `@SpringBootApplication`, `@Controller`, `@RestController`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@Service`, `@Repository` (InMemory implementations only), `@Component` (startup tasks only, e.g. SeedDataLoader), `@Configuration` (`WebConfig` only), and implementing `HandlerInterceptor` (`AuthInterceptor` only): a check every page needs is a cross-cutting concern, so it lives in one class instead of in every controller. Constructor injection only. **Never field `@Autowired`.**
4. **No `Optional`, no streams, no lambdas** in `model`, `engine`, `importer`, `repository` or `service` — return the object or `null`, use plain loops.
5. **Availability is `boolean[7][15]`** — day 0 = Monday, hour index 0 = 08:00. No `java.time` in it. `LocalDate`/`LocalDateTime` only where FOUNDATION.md shows them, plus `IcsTimetableImporter` (calendar parsing).
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
| `com.studybuddy.importer` | Timetable import | `TimetableImporter` (interface), `IcsTimetableImporter`, `TimetableImport`, `TimetableImportException` | Plain Java, same firewall as `engine`. One importer class per file format |
| `com.studybuddy.repository` | Storage | `XRepository` (interface) + `InMemoryXRepository` (`@Repository`) | The ONLY place that holds data. Add finder methods your feature needs to the interface and implementation |
| `com.studybuddy.service` | Business rules | `XService` (`@Service`) | Calls repositories and other services. Returns model objects, never HTML, never touches `Model` |
| `com.studybuddy.controller` | Web requests | `XController` (`@Controller`); JSON endpoints in `XApiController` (`@RestController`) | Thin: read input, call a service, choose a page. No business rules, no data storage |
| `com.studybuddy.seed` | Startup demo data | `SeedDataLoader` (`@Component`), `CsvFile`, one `XSeeder` per CSV file | Only saves through repositories. A new CSV file gets its own class implementing `Seeder`, added to the list in `SeedDataLoader` |
| `src/main/resources/seed/` | Demo data as CSV | `courses.csv`, `admins.csv`, `students.csv` | Edit the CSV, not Java. Lists use `\|`, availability uses `DAY start-end;...`, no commas inside a cell |

Controllers by area: `UserPickerController` and `HomeController` (shared pages), `CourseController`, `ProfileController` (My profile, availability grid, timetable import), `MatchController`, `BuddyController` (Find Buddies and candidate profiles), `ConnectionController`, `GroupController`, `SessionController`, `AdminController`. Add to the existing one for your area before creating a new one.

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

## Current user

The user picker (`/`) signs a user in. `AuthInterceptor` (registered in `WebConfig`) runs before every other page: it sends the visitor to `/` when nobody is signed in, the user no longer exists or is suspended, and keeps everyone but administrators out of `/admin/...`. Controllers do not repeat those checks; they read the signed-in user with `CurrentUser.getId(session)` plus `UserService`, or `CurrentUser.studentOrNull(session, userService)` on student pages. `CurrentUser` (controller package) is the only Java code that touches the session; it stores only the user's id, name and role label, and the nav fragment reads the name and role. Nothing else goes in the session.

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
| An action finished (success or failure not tied to a field) | Use Post/Redirect/Get: `Toasts.success(redirectAttributes, "...")` or `Toasts.error(redirectAttributes, "...")`, then `return "redirect:/..."`. The `toast` fragment shows it on the next page. Never write the flash attribute names by hand |
| A lasting page state | A banner in the page itself |
| A list is empty | The `empty-state` fragment, never a blank table |

Validation is plain Java in the service (it returns the error map or throws); no validation annotations.

## Conventions new code follows

These came out of the R1 refactor. Follow them so the code keeps one way of doing each thing.

- **Toasts:** flash messages go through `Toasts.success` / `Toasts.error` (controller package). Only `Toasts` knows the attribute names.
- **Rule violations:** a service or entity that refuses an action throws `BusinessRuleException` (model package) or one of its subclasses (`GroupFullException`, `RequestNotAllowedException`, `RequestAlreadyHandledException`, `NotGroupLeaderException`) with a message written for people. Controllers catch `BusinessRuleException` and show the message as an error toast. Never throw or catch `IllegalStateException` for a rule. Add a subclass only when a caller needs to tell that rule apart.
- **Entity equality:** entities compare by id (`User`, `StudyGroup`, `JoinRequest`, `MatchRequest`, `Connection`, `StudySession`) or by code (`Course`) with `equals`/`hashCode` (`instanceof`, cast, compare; `Objects.hash`). Compare entities with `equals`, never `==`.
- **Collections:** a getter for an entity's own collection returns a read-only view (`Collections.unmodifiableList` / `Set` / `Map`); a setter copies its argument. Change a collection through the owning object's method (`group.addMember(...)`), never through the getter. Forms (`ProfileForm`, `GroupForm`, `MatchingConfigForm`) stay mutable for Spring binding. Goal sets are `EnumSet`s, so they always list in declaration order.
- **Enums:** text shown to people comes from `getLabel()` (templates use `${x.label}`); text from a form becomes an enum with `X.fromName(text)`, which returns null for null, blank or unknown text. No hand-written loops over `values()`, no `#strings.capitalize(...)` on enum names.
- **Grid shape:** `AvailabilityGrid.DAYS`, `HOURS_PER_DAY`, `FIRST_HOUR`, `LAST_HOUR`, `dayName(day)` and `dayIndex(name)` are the only place the grid's shape is written down. A page grid extends `GridView` and implements `cellClass`.
- **Access checks:** `AuthInterceptor` handles sign-in, suspended accounts and the admin area, so controllers do not repeat those checks. A student-only page starts with `Student student = CurrentUser.studentOrNull(session, userService);` and, when it is null, an error toast with that page's message and a redirect.
- **Seed data:** one `Seeder` class per CSV file, built with only the repositories it needs. It reads rows through `CsvFile`, whose parsers name the file and line in every error.
- **Strategies:** a `MatchingStrategy` describes itself (`getLabel()`, `getDescription()`); the admin config page lists whatever strategies the engine has.

## Discord group chat

Each study group can have a private Discord channel in one shared server. Only members who connected their Discord account get the group's role, so they see only their groups' channels.

- **Settings:** `DISCORD_*` values in a git-ignored `.env` file (or environment variables); see `.env.example`. Never commit them. If any is missing, the feature turns itself off and group pages hide the chat section.
- **Where the code is:** `DiscordGateway` (interface) and `HttpDiscordGateway` make the web calls; `DiscordService` holds the rules; `DiscordController` serves `/groups/{id}/discord/setup`, `/groups/{id}/discord/connect` and `/discord/callback`. Links live in their own model classes (`DiscordChannel`, `DiscordAccount`, `DiscordLoginAttempt`) with in-memory repositories; `StudyGroup` and `Student` know nothing about Discord.
- **Group events:** `StudyGroupService` and `MembershipService` tell every `GroupEventListener` when a group is created or closed and when a member joins or leaves. `DiscordService` is one listener. To react to group changes in a new feature, implement `GroupEventListener` instead of adding calls to the group services. A listener must never throw.
- **Failures never block a group action:** Discord errors are logged; the group change still happens, and the leader can press "Set up Discord channel" later. Actions the user asked for directly (set up, connect) turn a Discord failure into a `BusinessRuleException` toast.
- **Login state** is kept in `DiscordLoginAttemptRepository`, not the session, so `CurrentUser` stays the only code that touches the session.
- **Seed data:** groups start without channels. The leader of the showcase group presses "Set up Discord channel".
