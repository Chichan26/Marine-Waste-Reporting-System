# BantayDagat Design System — "Ocean"

A three-layer token system for the BantayDagat JavaFX app, built on deep ocean blues.

| File | Role |
|------|------|
| [`design/design-tokens.json`](../design/design-tokens.json) | Source of truth for all tokens (W3C design-token format) |
| [`src/main/resources/ph/bantaydagat/styles.css`](../src/main/resources/ph/bantaydagat/styles.css) | The tokens implemented as JavaFX looked-up colors, plus all component styles |

## 1. Token architecture

```
Primitive   -bd-ocean-600          raw values        → only place a hex code may appear
    ↓
Semantic    -bd-color-primary      what it's for     → change these to re-theme
    ↓
Component   -bd-button-primary-bg  where it's used   → per-component tweaks
```

JavaFX CSS has no `var()`. Instead, **looked-up colors** declared on `.root` are inherited by every node and referenced by name (`-fx-text-fill: -bd-color-text;`). They also work inside `linear-gradient(...)`, `dropshadow(...)` and `derive(...)`.

**Naming:** `primitive.color.ocean.600` → `-bd-ocean-600` · `semantic.color.primary` → `-bd-color-primary` · `component.button.primary.bg` → `-bd-button-primary-bg`.

> Looked-up values only work for colors and paints. Spacing, radius and type sizes are fixed scales (§3) and are written as literal numbers.

## 2. Color

### Ocean (brand)
| Token | Hex | Name | Typical use |
|-------|-----|------|-------------|
| `ocean-50`  | `#F0F7FC` | Sea foam   | Tinted surfaces, hover rows, info banners |
| `ocean-100` | `#DCEEF9` | Shallows   | User pill, selected rows, "Under review" badge |
| `ocean-200` | `#B9DCF2` | Lagoon     | Info border |
| `ocean-300` | `#85C2E8` | —          | Reserved |
| `ocean-400` | `#4AA1D9` | —          | Reserved |
| `ocean-500` | `#2183C4` | Open water | Accent, focus ring, links, bar chart, hero gradient end |
| `ocean-600` | `#1468A6` | Reef blue  | **Primary**: buttons, section titles |
| `ocean-700` | `#105486` | —          | Primary hover |
| `ocean-800` | `#0E446D` | —          | Primary pressed, headings, sidebar top |
| `ocean-900` | `#0C3657` | Deep sea   | Login hero start |
| `ocean-950` | `#072238` | Abyss      | Sidebar bottom |

**Aqua** (`#D5F3FA` · `#7AD7EE` · `#1BAFD6` · `#0B7A99`) is for wave-crest highlights, such as the selected nav indicator on the dark sidebar.

### Mist (blue-tinted neutrals)
`0 #FFFFFF` · `50 #F5F9FC` (app bg) · `100 #EAF1F7` · `200 #DAE5EE` (border) · `300 #C3D3E0` · `400 #94A9BB` · `500 #64798C` (muted text) · `600 #4A5E70` · `700 #364857` · `800 #233340` · `900 #15222D` (ink)

### Feedback
| Hue | 50 (bg) | 200 (border) | 500 (solid) | 800 (text) | Meaning |
|-----|---------|--------------|-------------|------------|---------|
| Kelp  | `#E6F6EE` | `#B5E0C7` | `#2E9E6B` | `#1F6B47` | Success, resolved, low severity |
| Sand  | `#FFF6E0` | `#F5DDA6` | `#E9A23B` | `#8A5A00` | Warning, dispatched, medium severity, active |
| Reef  | `#FDEBDD` | `#F6CBA9` | `#E8782E` | `#A3470D` | High severity |
| Coral | `#FDEDEC` | `#F5C2C0` | `#D64545` | `#8E2724` | Danger, critical severity |

### Semantic tokens
| Token | → Primitive | Token | → Primitive |
|-------|-------------|-------|-------------|
| `-bd-color-bg` | mist-50 | `-bd-color-primary` | ocean-600 |
| `-bd-color-surface` | mist-0 | `-bd-color-primary-hover` | ocean-700 |
| `-bd-color-surface-tint` | ocean-50 | `-bd-color-primary-pressed` | ocean-800 |
| `-bd-color-border` | mist-200 | `-bd-color-primary-subtle` | ocean-100 |
| `-bd-color-border-subtle` | mist-100 | `-bd-color-on-primary` | mist-0 |
| `-bd-color-border-strong` | mist-300 | `-bd-color-accent` / `-focus` | ocean-500 |
| `-bd-color-text` | mist-900 | `-bd-color-highlight` | aqua-300 |
| `-bd-color-text-muted` | mist-500 | `-bd-color-{info,success,warning,danger}-{bg,border,text}` | see Feedback |

**Contrast:** ink on white is about 16:1, muted text on white is about 4.6:1, white on primary is about 6:1, and every badge text/background pair is at least 4.5:1 (WCAG AA).

## 3. Scales

| Scale | Values |
|-------|--------|
| Spacing (4-px grid) | 4 · 8 · 12 · 16 · 20 · 24 · 32 |
| Radius | sm 6 (chart bars) · md 8 (buttons, inputs, nav) · lg 10 (banners, thumbnails) · xl 12 (cards, tables) · full 999 (pills, badges) |
| Type | xs 11 nav section, step labels, hero badge · sm 12 badges, field labels, table headers, captions · **base 14 body** · md 15 prose descriptions, empty-state title · lg 16 section/chart titles, top bar, hero text · xl 18 brand, detail title, stat chip · 2xl 22 hotline number · 3xl 24 page title · 4xl 30 KPI value · 5xl 40 hero *(expressed in `em` relative to `.root`)* |
| Elevation | `card`: dropshadow(gaussian, ocean-900 @ 8 %, 14, 0, 0, 3) |

## 4. Component specs

### Button
| Variant | Property | Default | Hover | Pressed | Disabled |
|---------|----------|---------|-------|---------|----------|
| `.button` | bg | surface | surface-tint | primary-subtle | opacity 0.45 |
| | text / border | primary / border | — | — | — |
| `.btn-primary` | bg + border | primary | primary-hover | primary-pressed | opacity 0.45 |
| | text | on-primary | on-primary | on-primary | — |
| `.btn-ghost` | bg | transparent | ocean-500 @ 10 % | ocean-500 @ 20 % | opacity 0.45 |
| | text | accent | accent | accent | — |

Radius 8, padding 8 × 16, bold, hand cursor.

### Input (`.text-field`, `.password-field`, `.text-area`, `.combo-box-base`)
| | Default | Hover | Focused | Disabled |
|---|---|---|---|---|
| Border | border | border-strong | focus, 1.5 px | opacity 0.55 |
| Bg | surface | surface | surface | — |

Radius 8, padding 7 × 10, prompt text mist-400.

### Navigation (`.sidebar`, `.nav-button`)
Sidebar is a vertical gradient from ocean-800 to ocean-950. Nav item text is white @ 82 %. On hover, the background is white @ 8 %. When selected, the background is white @ 16 %, text turns bold white, and a 3 px aqua-300 left indicator appears.

### Card / Table
Cards and tables use a surface background, a 1 px border, radius 12 and the card shadow. Table rows are 40 px with a border-subtle divider. Hover uses surface-tint and selected uses primary-subtle.

### Badges (`.badge` + modifier)
| Severity | bg / text | Status | bg / text |
|----------|-----------|--------|-----------|
| `low` | kelp-50 / kelp-800 | `submitted` | mist-100 / mist-600 |
| `medium` | sand-50 / sand-800 | `under-review` | ocean-100 / ocean-800 |
| `high` | reef-50 / reef-800 | `dispatched` | sand-50 / sand-800 |
| `critical` | coral-50 / coral-800 | `resolved` | kelp-50 / kelp-800 |

### Banners
`.banner-info | -success | -warning | -danger` use the matching `-bg`, `-border` and `-text` semantic triplet.

### KPI cards & charts
KPI cards have a 4 px left accent: `.kpi-accent-ocean` (accent), `-amber` (warning), `-red` (danger), `-green` (success). The bar series is ocean-500. In the pie chart, Active = warning and Resolved = success. Grid lines use border-subtle.

## 5. Motion

Motion is subtle and professional. Every animation lives in [`Motion.java`](../src/main/java/ph/bantaydagat/Motion.java). Only the login waves loop.

| Token | Value | Used for |
|-------|-------|----------|
| `FAST` | 150 ms | Hover lift, button press |
| `SLOW` | 300 ms | Screen, card and banner entrance; error shake |
| `COUNT` | 700 ms | KPI count-up |
| `STAGGER` | 60 ms | Delay between items in a group |
| `EASE_OUT` | cubic-bezier(0.16, 1, 0.3, 1) | Default easing |
| `ENTER_OFFSET` | 10 px | Distance an entering element rises |

| Helper | Where it's applied |
|--------|--------------------|
| `Motion.enter(node)` | Every sidebar screen switch (`Main.showScreen`), every banner, the login card |
| `Motion.stagger(nodes)` | Login hero headline, then text, then bullet points |
| `Motion.waves()` | Three layered waves drifting across the bottom of the login hero (18 s / 13 s / 9 s; middle wave runs in reverse). Pauses automatically when the login screen is hidden. |
| `Motion.countUp(label, from, to, fmt)` | Dashboard KPIs: count up from 0 each time the dashboard opens, and from the old value when data changes |
| `Motion.hoverLift(node)` | KPI cards, hotline cards (rise 3 px) |
| `Motion.pressable(button)` | All primary buttons (scale to 0.97 while pressed) |
| `Motion.shake(node)` | Login card after a wrong password |

**Reduced motion:** start the JVM with `-Dbantaydagat.motion=off` to turn off every animation. Content then appears in its final state immediately. With Maven, add `<options><option>-Dbantaydagat.motion=off</option></options>` inside the `javafx-maven-plugin` `<configuration>` in `pom.xml`.

Wave colors are component tokens: `-bd-wave-back` (white @ 6 %), `-bd-wave-mid` (aqua-300 @ 16 %), `-bd-wave-front` (white @ 10 %).

## 6. Rules

1. **Never put hex or rgba below the `.root` block.** Add a primitive, then alias it.
2. Component rules should reference **semantic or component** tokens, not primitives (alpha primitives such as `-bd-white-16` on the dark sidebar are the exception).
3. **Dark mode (`.root.dark`)** overrides only the semantic layer using deep `abyss` surfaces (`#0D1824` – `#1F374F`) and dark-safe feedback `900`/`200` pairs. Toggle it at runtime via `ViewSupport.themeToggle(darkMode)`.
4. Update `design-tokens.json` and `styles.css` together.

### Quick check
```bash
# Should print nothing: raw colors outside the .root token block
awk '/^\.root/{r=1} r&&/^}/{r=0;next} !r' src/main/resources/ph/bantaydagat/styles.css | grep -nE "#[0-9A-Fa-f]{3,6}|rgba?\("
```
