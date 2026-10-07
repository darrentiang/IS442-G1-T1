# UI Guidelines

How the app looks and how it gives feedback. Every value here is a CSS variable in `src/main/resources/static/css/app.css`, the only stylesheet. Pages use the shared classes and fragments below instead of writing their own styling.

| Version | Date | Change |
|---|---|---|
| 1.0 | 19-09-2026 | Initial guidelines |

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
| Top bar | `.topbar` (fragment `nav`) | White, hairline bottom border; app name semibold left; links 14px grey, current one in accent; current user on the right |
| Primary button | `.btn .btn-primary` | Solid near-black, white text, 8px radius; hover lifts with `--shadow-md`; pressed scales to 0.98 |
| Secondary button | `.btn .btn-secondary` | White, hairline border; hover fills muted grey |
| Destructive button | `.btn .btn-danger` | Danger text on a light danger tint; hover goes solid danger with white text |
| Input / select / textarea | (styled by element) | White, hairline border, 8px radius; focus: accent border plus a soft accent ring |
| Field error | `.field-error` (fragment `field-error`) | 13px danger text directly under the field |
| Card | `.card` | White, hairline border, 12px radius, `--shadow-sm`, 24px padding |
| Table | `.table` inside a `.card` | Muted header row in the table-header style; hairline row dividers; row hover fill |
| Badge | `.badge .badge-<status>` (fragment `badge`) | Pill, 12px/600, status colour as text on a 10% tint of it |
| Banner | `.banner .banner-info` / `-warning` / `-danger` | 12px radius, tinted background, 3px coloured left edge |
| Toast | `.toast .toast-success` / `.toast-error` (fragment `toast`) | Fixed top-right card with `--shadow-md`; fades out by itself (success after 4s, error after 8s). Pure CSS, no JavaScript |
| Empty state | `.empty-state` (fragment `empty-state`) | Centred, 48px vertical padding, secondary text, optional action below |
| Score bar | `.score-bar` > `.score-fill` | 5px track in muted grey, accent fill; width set by the score |
| Availability grid | `.avail-grid` with `.cell` | Small rounded cells. `.cell-free` = light accent tint, `.cell-busy` = muted grey, `.cell-both` = solid accent. Group heatmap uses `.heat-1` to `.heat-4` (lighter to darker accent) |

## Status colours

One mapping for the whole app, so every team badges statuses the same way. In a template: `badge` fragment with the enum value; the class is `badge-` plus the lowercase value.

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
| Destructive action | **Destructive button**, and say what happens in its label | "End connection", "Close group" |

Toasts never carry field-level problems, and field errors never appear as toasts. On the admin config page (the one page using `fetch`), request failures show as an error toast created by `admin-config.js` with the same classes.

## Don't

Gradients. More than one accent. Coloured primary buttons. Shadows darker than `--shadow-md`. Borders thicker than 1px (except the banner and toast edge). Icon libraries, CSS frameworks, CDN links or web fonts. Text below 12px. `<style>` blocks or `style=""` attributes in templates (the one exception: a score bar's `width`, which depends on data). Hand-written button, input, table, badge, toast or banner styling where a shared class exists.
