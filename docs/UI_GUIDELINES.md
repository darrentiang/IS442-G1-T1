# UI Guidelines

How the app looks and how it gives feedback. Every value here is a CSS variable in `src/main/resources/static/css/app.css`, the only stylesheet. Pages use the shared classes and fragments below instead of writing their own styling.

| Version | Date | Change |
|---|---|---|
| 1.0 | 19-09-2026 | Initial guidelines |
| 1.1 | 04-11-2026 | Find Buddies: chips, match card, reason tags, "Why this match" details, week grid |
| 1.2 | 04-11-2026 | My profile: checkbox availability grid with disabled "class" cells, hint text, page actions |
| 1.3 | 09-11-2026 | UX clean-up: grouped nav with tabs and counts, home pages, page pattern, quiet buttons, Danger zone, icons, filter bar |

## Principles

1. **Quiet by default.** Near-black text on white and very light grey. Colour appears only where it means something: one accent for focus and the current page, and status colours.
2. **Whitespace does the grouping.** Space separates sections before borders do; borders are hairlines, shadows are barely there.
3. **One primary action per view.** It is the only solid dark button on the screen.
4. **Type carries hierarchy.** Large semibold titles, small grey secondary text. No decorative fonts, no icons.
5. **Everything feels immediate.** 150ms transitions on colour, border and shadow.

## Tokens

| Variable | Value | Use |
|---|---|---|
| `--color-text` | `#1d1d1f` | Body and headings |
| `--color-text-secondary` | `#6e6e73` | Labels, hints, table headers, empty states |
| `--color-bg` | `#fafafa` | Page background |
| `--color-surface` | `#ffffff` | Cards, tables, inputs, top bar |
| `--color-surface-muted` | `#f5f5f7` | Table header row, hover fills |
| `--color-border` | `#e5e5ea` | Hairlines around cards, inputs, tables |
| `--color-divider` | `#f0f0f2` | Lines between table rows |
| `--color-accent` | `#0071e3` | Focus ring, current nav item, links on hover, score bars. Nothing else |
| `--color-info` | `#0071e3` | Status: waiting on someone else |
| `--color-success` | `#1f9d55` | Status: accepted, confirmed, going; success toast |
| `--color-warning` | `#c77700` | Status: proposed, needs attention |
| `--color-danger` | `#d70015` | Status: declined, suspended; errors; destructive actions |
| `--color-neutral` | `#6e6e73` | Status: past, closed, inactive |
| `--font-sans` | `-apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif` | Everything (system fonts, so it works offline) |
| `--radius-control` | `8px` | Buttons, inputs |
| `--radius-card` | `12px` | Cards, tables, banners, toasts |
| `--radius-pill` | `999px` | Badges |
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,.04), 0 1px 3px rgba(0,0,0,.06)` | Cards, tables |
| `--shadow-md` | `0 2px 4px rgba(0,0,0,.04), 0 6px 16px rgba(0,0,0,.08)` | Primary button hover, toasts |

## Type and spacing

| Role | Size / weight |
|---|---|
| Page title (`h1`) | 28px / 600, letter-spacing −0.02em |
| Section title (`h2`) | 17px / 600 |
| Body | 15px / 400, line-height 1.5 |
| Label | 13px / 500, secondary colour |
| Table header | 12px / 600, uppercase, secondary colour |

4px grid. Card padding 24px; 32px between page sections; 16px between form fields; content max 1100px wide, centred, 24px side padding.

## Components

Class names are what pages use. Never restyle these in a page.

| Component | Class | Look |
|---|---|---|
| Top bar | `.topbar` > `.topbar-main` (fragment `nav`) | White, hairline bottom border; app name semibold left; area links 15px grey, current area in accent; current user and "Switch user" on the right. Students see Home · Buddies · Groups · Profile; admins see Overview · Accounts · Matching · Groups · Courses |
| Area tabs | `.subnav` > `.subnav-tabs` (fragment `nav`) | Second row for students, listing the pages of the current area (Find buddies · Requests · My buddies, and so on); the current tab has an accent underline. A page passes its own key to `nav(...)`; the fragment works out the area |
| Count | `.nav-count` | Small accent pill with a number, on an area link and its tab, for items waiting (buddy requests, join requests to groups you lead). Added by `AuthInterceptor` as `navCounts`; shown only when above 0 |
| Page header | `.page-header` | Title and one line of explanation on the left; the page's one primary action on the right. Every list page uses it |
| Primary button | `.btn .btn-primary` | Solid near-black, white text, 8px radius; hover lifts with `--shadow-md`; pressed scales to 0.98 |
| Secondary button | `.btn .btn-secondary` | White, hairline border; hover fills muted grey |
| Destructive button | `.btn .btn-danger` | Danger text on a light danger tint; hover goes solid danger with white text. Only inside a Danger zone |
| Quiet button | `.btn .btn-quiet` | No fill, secondary text; hover fills muted grey. A low-key action that is neither the main one nor destructive (Pass, Clear) |
| Quiet destructive button | `.btn .btn-quiet-danger` | No fill, danger text; hover a light danger tint. A refusal or removal in a row (Decline, Reject, Remove from group, End connection), so the accepting action next to it reads as the main one |
| Danger zone | `.card .danger-zone` > `.danger-row` (`.danger-title` + `.text-secondary`, then a `.btn-danger`) | Last card on the page, danger-tinted border and heading. One row per action that ends something (leave or close a group, suspend or delete an account), each with a sentence on what happens |
| Input / select / textarea | (styled by element) | White, hairline border, 8px radius; focus: accent border plus a soft accent ring |
| Field error | `.field-error` (fragment `field-error`) | 13px danger text directly under the field |
| Card | `.card` | White, hairline border, 12px radius, `--shadow-sm`, 24px padding |
| Table | `.table` inside a `.card` | Muted header row in the table-header style; hairline row dividers; row hover fill |
| Badge | `.badge .badge-<status>` (fragment `badge`) | Pill, 12px/600, status colour as text on a 10% tint of it |
| Banner | `.banner .banner-info` / `-warning` / `-danger` | 12px radius, tinted background, 3px coloured left edge |
| Toast | `.toast .toast-success` / `.toast-error` (fragment `toast`) | Fixed top-right card with `--shadow-md`; fades out by itself (success after 4s, error after 8s). Pure CSS, no JavaScript |
| Empty state | `.empty-state` (fragment `empty-state`, or `empty-state-action(message, label, url)`) | Centred, 48px vertical padding, secondary text. Use the `-action` version whenever there is a next step, so an empty list is never a dead end |
| Score bar | `.score-bar` > `.score-fill` | 5px track in muted grey, accent fill; width set by the score |
| Chips | `.chip-group` (a `fieldset`) > `label.chip` > `input[type=radio]` | Radio buttons drawn as pills; the checked one is solid near-black. Used for small filter choices (Mode, Free together) |
| Match card | `.card` with `.match-header` (`.match-rank`, `.match-who`, `.match-score` > `.match-percent`), then `.match-footer` (`.tag-list` left, `.match-actions` right) | Rank circle, name (a link to the profile) and meta line, big mutual % on the right; reasons and actions share one row underneath. Send request is the only filled button; Pass is quiet |
| Reason tag | `.tag`, `.tag-warning` | Quiet muted pill, 13px. `.tag-warning` (amber) only for the "N pending requests" note. Not a status badge |
| Expandable details | `<details class="match-details">` with `<summary>` | Native disclosure, no JavaScript: "Why this match" opens the fit line (`.fit-line`) and one `.score-row` per criterion (label, `.score-bar`, value). `<details class="request-details">` with a button-styled `<summary>` reveals an inline form and moves onto its own line when open |
| Week grid | `.avail-week` with `.avail-label` and `.cell`, plus `.avail-legend` | 7 rows (MON-SUN) x 15 hours (08-22) with a label column; same `.cell-both` / `.cell-free` / `.cell-busy` colours, and a legend underneath |
| Availability editor | `.avail-editor` table > `label.avail-toggle` > `input[type=checkbox]`, inside `.avail-editor-scroll` | The checkbox-grid pattern: 7 day rows x 15 hour columns, one checkbox per hour (`name="cells"`, `value="day-hourIndex"`). The checkbox is hidden; its label is the cell: muted grey when unticked, accent tint (`:has(input:checked)`) when free. Pure CSS, no JavaScript. A class hour from an imported timetable adds `.avail-toggle-class`: the checkbox is `disabled`, the cell is a darker grey with the word "class", and it cannot be ticked. Legend uses `.cell-free` / `.cell-busy` / `.cell-class` |
| Hint | `.hint` | 13px secondary text, one line, directly under a field or a card title ("Leave blank to keep your current number") |
| Page actions | `.page-actions` (in a `.page-header`) | Secondary buttons side by side on the right of a page title |
| Availability grid | `.avail-grid` with `.cell` | Small rounded cells. `.cell-free` = light accent tint, `.cell-busy` = muted grey, `.cell-both` = solid accent. Group heatmap uses `.heat-1` to `.heat-4` (lighter to darker accent) |
| Card heading | `.card-heading` (an `h2` plus a quiet link) | Card title on the left, a quiet "See all →" or "Manage accounts →" link on the right |
| List rows | `ul.list-rows` > `li` | Rows of people or groups: content (often a `.person`) on the left, an action or badge on the right, hairline between rows |
| Stat | `.stat-row` > `.stat` (`.stat-icon` + `.stat-label` / `.stat-value`) | A number as large type under a small label, with an icon in a tinted square. Tint follows meaning: default accent, `.stat-icon-success` good, `.stat-icon-warning` waiting, `.stat-icon-neutral` closed or suspended. `.stat-row-cards` makes each stat a clickable card |
| Filter bar | `form.card.filter-bar` > `.filter-field` (label + control), `.filter-actions` | Filters side by side in one row with the buttons at the end; stacks on a phone. A Clear link (`.btn-quiet`) appears only while filtering |
| Row link | `a.row-link` | A table row's name as the link that opens it, in the text colour so the table stays calm (replaces "View" buttons) |
| Icon | `<svg th:replace="~{fragments/icon :: icon('users')}">` | Lucide line icons (ISC licence) copied in as inline SVG: no icon library. 16px by default, current text colour, hidden from screen readers, so always next to a text label |

## Status colours

One mapping for the whole app, so every team badges statuses the same way. In a template: `badge` fragment with the enum value; the class is `badge-` plus the lowercase value. The badge shows the value in sentence case ("Active", not "ACTIVE"), done in CSS.

| Colour | Statuses |
|---|---|
| Info (blue) | `PENDING` |
| Success (green) | `ACCEPTED`, `CONFIRMED`, `ACTIVE`, `GOING` |
| Warning (amber) | `PROPOSED` |
| Danger (red) | `DECLINED`, `SUSPENDED`, `CANT` |
| Neutral (grey) | `PAST`, `PUBLIC`, `PRIVATE`, anything else |

## Feedback: which pattern when

| Situation | Pattern | Example |
|---|---|---|
| One field is invalid | **Field error** under that field; the form re-shows with values kept (no redirect) | "Max size must be between 2 and 10" |
| An action finished | **Toast** on the next page (flash message after redirect) | "Request sent to Nur Aisyah" / "This group is full" |
| A lasting state of the page | **Banner** at the top of the content | "Set your availability to get matched" / "This group is closed" |
| Nothing to show | **Empty state** with a hint or action | "No pending requests" |
| Ending something for good | **Danger zone** at the bottom of the page, one row per action with a sentence on what happens | "Close this group", "Delete this account" |
| Refusing or removing in a list | **Quiet destructive button** next to the accepting action | "Decline", "Remove from group" |

Toasts never carry field-level problems, and field errors never appear as toasts. On the admin config page (the one page using `fetch`), request failures show as an error toast created by `admin-config.js` with the same classes.

## Page pattern

Every page follows the same order, so people always know where to look:

1. **Navigation:** the area in the top bar, the page in the tabs (students).
2. **Page header:** title, one line saying what the page is for, and the one primary action on the right.
3. **What needs attention first:** a banner for a lasting state, or a "Needs your reply" list on Home.
4. **The content:** cards, tables, lists; each list has an empty state with a next step.
5. **Danger zone last,** only when the page can end something.

One filled (primary) button per view. Other actions are secondary (outline) or quiet (text). A row's name is its link, rather than a separate "View" button.

## Don't

Gradients. More than one accent. Coloured primary buttons. Shadows darker than `--shadow-md`. Borders thicker than 1px (except the banner and toast edge). Icon libraries or icon fonts (icons are inline Lucide SVGs in the `icon` fragment), CSS frameworks, CDN links or web fonts. Text below 12px. `<style>` blocks or `style=""` attributes in templates (the one exception: a score bar's `width`, which depends on data). Hand-written button, input, table, badge, toast or banner styling where a shared class exists.
