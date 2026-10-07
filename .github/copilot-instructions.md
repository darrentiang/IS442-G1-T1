# Copilot instructions

Follow `AGENTS.md` in the repository root. It is the full rule set for this project; the design spec is `docs/FOUNDATION.md` and the UI rules are `docs/UI_GUIDELINES.md`.

The rules most often broken, repeated here in case AGENTS.md is not loaded:

- Java 21 + Spring Boot (web, thymeleaf) only. No database, JPA, Lombok, validation starter, CSS/JS frameworks, CDN links, or new dependencies.
- Layers: controller → service → repository → model. Controllers are thin; only repositories hold data; services never build HTML.
- `com.studybuddy.model` and `com.studybuddy.engine` must not import anything from Spring.
- No `Optional`, streams or lambdas in model, engine, repository or service. Return `null` and use plain loops.
- Constructor injection only, never field `@Autowired`.
- `StudentProfile.contactNumber` has no getter. Never add one.
- Pages go in `templates/<area>/`, use the classes in `static/css/app.css`, and never use `<style>` or `style=""`.
- Javadoc on every class and public method.
