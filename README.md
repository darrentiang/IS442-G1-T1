# Study Buddy Matcher

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-server--rendered-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Storage](https://img.shields.io/badge/storage-in--memory-lightgrey)](#configuration-and-seed-data)
[![Course](https://img.shields.io/badge/SMU-IS442%20OOP-1F3A68)](#team-and-work-division)

A web app that helps SMU students find compatible study partners and run study groups. A deterministic scoring engine ranks potential buddies by course, availability, study mode, goals and more; students connect through buddy requests, and contact numbers are only revealed once both sides agree.

Built by **IS442 G1-T1** for the IS442 Object-Oriented Programming project.

![Find Buddies: ranked matches with a mutual fit score and the reasons behind it](docs/screenshots/03-find-buddies.png)

## Contents

- [Features](#features)
- [Quick start](#quick-start)
- [Demo walkthrough](#demo-walkthrough)
- [Architecture](#architecture)
- [Design principles and patterns](#design-principles-and-patterns)
- [The matching engine](#the-matching-engine)
- [Configuration and seed data](#configuration-and-seed-data)
- [Project structure](#project-structure)
- [Open-source libraries](#open-source-libraries)
- [Team and work division](#team-and-work-division)
- [Use of AI](#use-of-ai)
- [Known limitations](#known-limitations)
- [Further documentation](#further-documentation)

## Features

The brief defines three roles. Every functional requirement is implemented.

![Use cases by role](docs/diagrams/use-cases.png)

### Students

| Area | What a student can do |
|---|---|
| **Profile & preferences** | Create and update a profile (name, school, programme, year, contact number, current and completed courses); set the course they need a buddy for, study mode (in person / online / either), group size (one-to-one / small group / either), study goals and intensity; mark weekly availability on a 7-day grid or **import it from an `.ics` timetable** |
| **Matching** | Search by course or study goal; get a **ranked list** with a mutual fit score and the top reasons; filter by mode and hours free together; open a candidate's public profile, where the **contact number stays hidden** |
| **Requests & connections** | Send a buddy request with an optional message; accept or decline incoming requests, after which the **contact number becomes visible** to both; view active buddies and end a connection at any time |
| **Groups** | Browse and request to join study groups; leave a group |

### Study group leaders

Create a group for a course with a name, description, study goals, mode, weekly availability and maximum size; edit, close and reopen it; review join requests, accept or reject them, and remove members. When a leader leaves, the earliest-joined member takes over automatically.

### System administrators

| Area | What an administrator can do |
|---|---|
| **Accounts** | Create, update and delete student and administrator accounts; suspend and reactivate accounts |
| **Status & usage** | See each account's status, last sign-in and activity: requests sent and received, connections, groups joined and led |
| **Matching configuration** | Turn criteria on or off, set each criterion's weight, choose the ranking strategy, and tune result limits and thresholds. Changes apply to the next search |

<details>
<summary><b>Screenshots</b> (click to expand)</summary>

| | |
|---|---|
| **Sign in as any seeded user**<br>![User picker](docs/screenshots/01-user-picker.png) | **Student home**<br>![Student home](docs/screenshots/02-student-home.png) |
| **Candidate profile: contact hidden**<br>![Public profile](docs/screenshots/04-candidate-public.png) | **Incoming buddy requests**<br>![Buddy requests](docs/screenshots/05-buddy-requests.png) |
| **My buddies: contact shared**<br>![My buddies](docs/screenshots/06-my-buddies.png) | **Candidate profile after connecting**<br>![Connected profile](docs/screenshots/07-candidate-connected.png) |
| **My profile**<br>![My profile](docs/screenshots/08-my-profile.png) | **Weekly availability and `.ics` import**<br>![Availability](docs/screenshots/09-availability.png) |
| **Browse study groups**<br>![Groups](docs/screenshots/10-groups.png) | **Group page as a member**<br>![Group member view](docs/screenshots/11-group-member-view.png) |
| **Leader: manage members and requests**<br>![Manage group](docs/screenshots/12-group-manage.png) | **Admin: all accounts**<br>![Accounts](docs/screenshots/13-admin-accounts.png) |
| **Admin: account status and usage**<br>![Account detail](docs/screenshots/14-admin-account-detail.png) | **Admin: matching configuration**<br>![Matching config](docs/screenshots/15-admin-matching-config.png) |

</details>

## Quick start

**Prerequisite:** JDK 21 or newer, with `JAVA_HOME` set. Nothing else: Maven comes with the project through the Maven Wrapper, and there is no database to install.

```bash
git clone https://github.com/darrentiang/IS442-G1-T1.git
cd IS442-G1-T1
```

| Shell | Run the app |
|---|---|
| PowerShell | `.\mvnw.cmd spring-boot:run` |
| cmd.exe | `mvnw spring-boot:run` |
| macOS / Linux / Git Bash | `./mvnw spring-boot:run` |

Open **<http://localhost:8080>** and pick a user to sign in as. The first run downloads Maven and the dependencies (a few minutes); later runs start in seconds. Stop with `Ctrl+C`.

Other goals, using the same command form: `compile`, and `javadoc:javadoc` (API reference in `target/reports/apidocs/`).

> **Data is in memory.** Every restart reloads the seed data, so you can demo freely and start clean each time.

## Demo walkthrough

There are no passwords: the start page lists every seeded user, and you sign in by picking one. **Switch user** in the top bar returns to that page.

| Sign in as | Good for showing |
|---|---|
| **A001** Study Buddy Admin | Accounts (create, edit, suspend, delete), usage information, matching configuration |
| **S036** Jonathan Heng | Four pending buddy requests to accept or decline, then **My buddies** with contact numbers |
| **S001** Tan Wei Jie | Leader of *IS442 Finals Crammers* (G001): manage members, join requests, close/reopen, leave and hand over |
| **S002** Nur Aisyah Binte Rahman | A regular group member who can leave G001 |

**A five-minute tour of the core flow:**
1. Sign in as **S036** and open **Find buddies**: ranked IS442 matches, each with a mutual fit score and reasons.
2. Open a candidate: the contact number reads *"Visible after you connect"*.
3. Go to **Requests**, accept the request from Arjun Menon, then open **My buddies**: his contact number is now shown.
4. Click **End connection**: he leaves the list and his number is hidden again.
5. Sign in as **A001**, open **Matching config**, switch the strategy to *Availability first*, and repeat step 1 to see the ranking change.

## Architecture

The app is a single Spring Boot service with server-rendered pages. It is organised in **layers, and every dependency points down**: controllers call services, services call repositories and the engine, and nothing calls upwards.

![Layered architecture](docs/diagrams/architecture.png)

| Layer | Package | Responsibility |
|---|---|---|
| Presentation | `controller`, `templates/` | Read the request, call one service, choose a page. No business rules. `AuthInterceptor` handles sign-in, suspended accounts and the admin area for every page in one place |
| Business logic | `service` | The rules: validation, request lifecycle, group membership, account cleanup. Returns model objects, never HTML |
| Domain | `model`, `engine`, `importer` | Plain Java with **no Spring imports** (a "domain firewall"), so the core logic can be reasoned about and tested on its own |
| Data access | `repository` | One interface per entity plus an in-memory implementation. The only place data is held |

### Domain model

![Domain model](docs/diagrams/domain-model.png)

A few modelling decisions:

- **`User` is abstract.** `Student` and `SystemAdministrator` extend it and report their `Role`, so code asks `getRole()` instead of using `instanceof`.
- **Leadership is a reference, not a subclass.** A `StudyGroup` holds its `leader`, so leadership can pass to another member.
- **`contactNumber` has no getter.** It can only be read through `StudentProfile.toConnectedView()`. The privacy rule from the brief is enforced by the type, not by remembering to hide a field.
- **Requests and connections are separate.** A `MatchRequest` (`PENDING → ACCEPTED / DECLINED`) records who asked; accepting it creates a `Connection`, which can later be ended without rewriting history.

### One flow end to end: accepting a buddy request

![Accepting a buddy request](docs/diagrams/buddy-request-sequence.png)

## Design principles and patterns

| Principle / pattern | Where it shows up |
|---|---|
| **Single Responsibility** | `MatchRequestService` owns the request lifecycle; `ConnectionService` owns connections; `AdminService` decides *whether* an account may be deleted while `AccountCleanupService` handles *what else must change* |
| **Open/Closed** | A new matching criterion, filter or strategy is a new class implementing `MatchCriterion`, `CandidateFilter` or `MatchingStrategy`. The engine and the admin config page pick it up without being edited |
| **Liskov Substitution** | Any `Student` or `SystemAdministrator` works wherever a `User` is expected (sign-in, account list, usage) |
| **Interface Segregation** | Small interfaces: each repository exposes only its own entity's finders; `MatchCriterion` has one scoring method |
| **Dependency Inversion** | Services depend on repository *interfaces*; Spring injects the in-memory implementations through constructors |
| **Strategy** | `MatchingStrategy` with `Balanced`, `AvailabilityFirst` and `CourseFirst`, chosen at runtime from the config page |
| **Template Method** | `AbstractMatchingStrategy.rank()` is the fixed algorithm; each subclass supplies only its comparator |
| **Repository** | `XRepository` interfaces with `InMemoryXRepository` implementations |
| **Post/Redirect/Get** | Every form action redirects with a toast, so a refresh never repeats an action |

Business rules are signalled with `BusinessRuleException` and its subclasses (`GroupFullException`, `RequestAlreadyHandledException`, …), each carrying a message written for people. Controllers catch the base type and show it as an error toast.

## The matching engine

![Matching engine classes](docs/diagrams/matching-engine.png)

`MatchingEngine.findMatches(...)` runs a seven-step pipeline:

| Step | What happens |
|---|---|
| 1. Filter | Six `CandidateFilter`s remove dealbreakers (yourself, inactive accounts, already connected, incompatible mode, too little shared time, …) |
| 2. Score | Each enabled `MatchCriterion` scores the pair from 0.0 to 1.0 with a reason |
| 3. Combine | A weighted fit is computed **both ways**, then combined with the harmonic mean, so a match must work for both people |
| 4. Adjust | `CongestionPenalty` gently lowers students already flooded with requests |
| 5. Rank | The active `MatchingStrategy` orders the results |
| 6. Cut | Drop results below the minimum score; keep the top *N* |
| 7. Explain | Attach the two strongest reasons shown on each result card |

The full rationale, criteria definitions and default weights are in [docs/MATCHING_ENGINE.md](docs/MATCHING_ENGINE.md).

## Configuration and seed data

Configuration lives outside the Java code:

| File | Controls |
|---|---|
| `src/main/resources/application.properties` | **Business limits** (group name and description length, group size range, year-of-study range, message and "looking for" lengths, the ranges an admin may choose for matching) and server settings (session cookies, upload size) |
| `src/main/resources/seed/matching-config.csv` | Default strategy, enabled criteria, weights, max results, minimum score, availability and congestion settings. The admin can change all of these at runtime |
| `src/main/resources/seed/*.csv` | Demo data: **10 courses, 50 students**, 1 administrator, 12 study groups, 7 buddy requests, 16 join requests |

Each service reads its limits once at startup through Spring's `Environment` (constructor injection), and the pages read the same values, so a form's labels, its browser-side checks and the server-side validation never disagree. Any limit can also be overridden at launch without rebuilding:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--studybuddy.group.max-size=8
```

What stays in code is deliberate: the bounds of the engine's 0-to-1 scoring scale, the shape of the availability grid, and each criterion's scoring rules are part of the algorithm, not settings.

The seed data is plain CSV, so a demo scenario can be changed without touching Java: lists use `|` and availability uses `DAY start-end;...`. Each CSV file has its own `Seeder` class, and errors name the file and line.

## Project structure

```
src/main/java/com/studybuddy/
├── controller/   Web layer: 10 controllers, AuthInterceptor, CurrentUser, Toasts
├── service/      Business rules and form objects
├── engine/       Matching engine: criteria/, filter/, strategy/  (no Spring)
├── importer/     .ics timetable import                             (no Spring)
├── model/        Entities, enums and rule exceptions               (no Spring)
├── repository/   Interfaces + in-memory implementations
└── seed/         Startup loader, one Seeder per CSV
src/main/resources/
├── templates/    Thymeleaf pages by area: shared/, student/, group/, admin/, fragments/
├── static/       app.css (the only stylesheet), one JS file per page that needs it
└── seed/         Demo data and default matching configuration
docs/
├── diagrams/     PlantUML sources (.puml) and rendered images
└── screenshots/  Images used in this README
```

## Open-source libraries

The stack is deliberately small, so the team could focus on object-oriented design rather than framework configuration.

| Library | Used for | Why this one |
|---|---|---|
| [Spring Boot 3.5](https://spring.io/projects/spring-boot) (`spring-boot-starter-web`) | HTTP, dependency injection, embedded Tomcat | Recommended by the brief; constructor injection fits our interface-based design |
| [Thymeleaf](https://www.thymeleaf.org/) (`spring-boot-starter-thymeleaf`) | Server-rendered HTML pages | Natural HTML templates that open in a browser as-is; no separate front-end build or JavaScript framework |
| [Maven Wrapper](https://maven.apache.org/wrapper/) | Builds | Anyone can build with only a JDK installed |
| [Lucide](https://lucide.dev/) icons (ISC licence) | A few icons on the admin pages | Inlined as SVG, so no CDN or extra dependency |
|

**Considered and not used:** a database with JPA (in-memory storage keeps the focus on the domain model and makes demos reproducible), CSS frameworks such as Bootstrap (one hand-written stylesheet keeps pages consistent with the team's UI guidelines), and Lombok (explicit getters and setters keep the code readable for the team).

## Team and work division

The work was split by **functional area of the brief**, with each group owning its feature's rules, pages and documentation. A shared foundation (model, repositories, seed loader, UI guidelines) was agreed first so the groups could work in parallel.

| Group | Members | Owns |
|---|---|---|
| 1 | Darren, Wei Lin | Student profiles, preferences and availability (including `.ics` import); the matching engine, search, filters, ranking and public profiles |
| 2 | Rachel, Karina, Kenneth | Study groups: create, update, close; group availability and size; join requests, acceptance and member removal |
| 3 | Hong Yi, Ela | Buddy requests and connections; administrator account management, status and usage; matching configuration |

Changes went through pull requests reviewed within the team.

## Use of AI

AI coding assistants were used as a pair programmer: to explain unfamiliar Java and Spring concepts, review designs against SOLID, suggest implementations, and draft documentation such as this README and its diagrams.

To keep that use responsible and the code our own:
- [`AGENTS.md`](AGENTS.md) sets rules every AI agent must follow: the fixed stack, the layering, Java conventions, the privacy rule on contact numbers, and no new dependencies.
- Changes were made in small steps that a team member read and understood before committing, and they were then reviewed in pull requests.
- Every feature was checked by running the app against the brief, not taken on trust.

## Known limitations

- **No persistence.** Data lives in memory and resets on every restart, by design for a demo app.
- **No real authentication.** Users are picked from a list; there are no passwords.
- **No automated test suite.** Features were verified by running the app end to end.
- Deleting an account keeps answered requests and ended connections as history; only live links (connections, pending requests, group seats) are cleaned up.

## Further documentation

| Document | Contents |
|---|---|
| [docs/FOUNDATION.md](docs/FOUNDATION.md) | Design spec: layers, shared classes, the worked Course example every feature follows |
| [docs/MATCHING_ENGINE.md](docs/MATCHING_ENGINE.md) | Matching pipeline, criteria and default weights |
| [docs/TIMETABLE_IMPORT.md](docs/TIMETABLE_IMPORT.md) | How `.ics` timetables become availability |
| [docs/UI_GUIDELINES.md](docs/UI_GUIDELINES.md) | Visual design tokens and feedback patterns |
| [AGENTS.md](AGENTS.md) | Coding rules for contributors and AI agents |
| [docs/diagrams/](docs/diagrams/) | PlantUML sources for every diagram here. Re-render with `java -jar plantuml.jar -tpng docs/diagrams/*.puml` |
