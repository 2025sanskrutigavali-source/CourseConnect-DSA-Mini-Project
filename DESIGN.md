---
name: Academic Modernism
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#45464d'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#4b41e1'
  on-secondary: '#ffffff'
  secondary-container: '#645efb'
  on-secondary-container: '#fffbff'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#001d31'
  on-tertiary-container: '#188ace'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#e2dfff'
  secondary-fixed-dim: '#c3c0ff'
  on-secondary-fixed: '#0f0069'
  on-secondary-fixed-variant: '#3323cc'
  tertiary-fixed: '#cce5ff'
  tertiary-fixed-dim: '#93ccff'
  on-tertiary-fixed: '#001d31'
  on-tertiary-fixed-variant: '#004b73'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.005em
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.03em
  code-num:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system embodies an authoritative, friction-free academic SaaS environment tailored for higher education students, faculty advisors, and institutional registrars. The visual philosophy blends modern corporate precision with academic prestige—avoiding playful triviality while eliminating bureaucratic intimidation.

Key brand tenets include:
- **Clarity & Decisiveness:** Course registration operates under strict time windows and high-stakes prerequisites. Information hierarchy must guarantee zero cognitive ambiguity regarding enrollment status, credit caps, conflict warnings, and deadlines.
- **Architectural Utility:** A structured, grid-governed workspace that favors tabular clarity, crisp division lines, and predictable spatial relationships.
- **Controlled Vibrancy:** Foundational stability driven by deep navy slate anchors the workspace, punctuated intentionally by academic royal indigo for focal actions and immediate guidance.

## Colors

The palette establishes an institutional balance of deep structural neutrals and tactical high-visibility accents:

- **Dominant Base:** `#0F172A` (Slate 900) anchors headers, key navigational containers, and high-emphasis typography, supported by `#1E293B` (Slate 800) for structural sidebars and dark surface elevation.
- **Brand Accent:** `#4F46E5` (Indigo 600) serves as the primary actionable anchor for calls-to-action, active tab indicators, and progress states. `#6366F1` acts as its hover state, with `#7C3AED` reserved for selective academic milestones or advanced degree pathways.
- **Canvas & Surface Tiering:**
  - Base canvas: `#F8FAFC` (Slate 50)
  - Secondary wells and toolbar fills: `#F1F5F9` (Slate 100)
  - Card & Modal surfaces: Pure `#FFFFFF`
  - Structural strokes: `#E2E8F0` (Border default) and `#CBD5E1` (Border strong)
- **Status & Governance Channels:**
  - Success (Enrolled, Degree Audit Cleared): `#10B981` text / `#ECFDF5` container
  - Warning (Waitlisted, Prerequisite Pending): `#F59E0B` text / `#FFFBEB` container
  - Danger (Course Conflict, Cap Exceeded, Dropped): `#EF4444` text / `#FEF2F2` container
  - Information (Advisory Note, Audit Metric): `#0284C7` text / `#F0F9FF` container
- **Text Hierarchy:** Primary headings and course codes use `#0F172A`, supportive copy uses `#475569`, and deactivated or placeholder text uses `#94A3B8`.

## Typography

The type system utilizes Plus Jakarta Sans for structural personality and editorial presence, paired with Inter for high-density labels, status badges, and critical operational readouts.

Key typographic rules:
- **Tabular Figures:** All credit tallies, course codes (`CS-4010`), section IDs, and GPA values must invoke `font-feature-settings: "tnum" 1` to ensure vertical alignment across dense schedules and tables.
- **Display Restraint:** Limit `display-lg` to global term summary views and high-level portal dashboard splash indicators.
- **Dense Data Clarity:** Use `body-sm` and `label-md` for subtext in course registration listings, ensuring metadata (e.g., *Instructor*, *Days/Times*, *Room*) remains sharply differentiated using weights rather than pure scale steps.

## Layout & Spacing

The layout is built upon an 8pt base grid with a responsive 12-column layout on desktop and tablet, collapsing to a unified single-column flow on mobile viewports.

- **Breakpoints:**
  - Desktop Large: `1440px` and up (Max content container capped at `1400px` centered, or full-width dashboard layout with `280px` fixed side navigation).
  - Desktop Standard: `1024px – 1439px` (12-column fluid grid, `1.5rem` gutters, `2rem` outer margins).
  - Tablet: `768px – 1023px` (8-column fluid grid, `1rem` gutters, `1.5rem` margins; sidebar collapses into an overlay drawer).
  - Mobile: `< 768px` (4-column grid, `1rem` margins, persistent bottom app bar or compact top app header).
- **Rhythm Rules:**
  - Stacked component intervals strictly adhere to `space-md` (`16px`) for standard card arrangements and `space-lg` (`24px`) between primary module sections.
  - Form field groupings and metric label-value stacks utilize `space-xs` (`4px`) and `space-sm` (`8px`) for immediate semantic grouping.

## Elevation & Depth

Visual hierarchy is primarily communicated through crisp borders and soft, daylight-inspired neutral drop shadows. Surface elevation reflects clear operational layers:

- **Level 0 (Canvas Base):** `#F8FAFC`. Background canvas, containing no drop shadow.
- **Level 1 (Card & Section Containers):** `#FFFFFF` surface with a `1px solid #E2E8F0` border and `box-shadow: 0 1px 3px 0 rgba(15, 23, 42, 0.05), 0 1px 2px -1px rgba(15, 23, 42, 0.05)`.
- **Level 2 (Hovered Cards & Interactive Flyouts):** `#FFFFFF` surface with a `1px solid #CBD5E1` border and `box-shadow: 0 4px 6px -1px rgba(15, 23, 42, 0.07), 0 2px 4px -2px rgba(15, 23, 42, 0.05)`.
- **Level 3 (Dropdown Menus & Popovers):** `#FFFFFF` surface with `1px solid #E2E8F0` and `box-shadow: 0 10px 15px -3px rgba(15, 23, 42, 0.08), 0 4px 6px -4px rgba(15, 23, 42, 0.04)`.
- **Level 4 (Modal Dialogs & Side Panels):** `#FFFFFF` surface elevated over an backdrop overlay (`rgba(15, 23, 42, 0.45)` with `backdrop-filter: blur(4px)`), with `box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.06)`.

## Shapes

The design system employs a balanced, contemporary rounded corner profile (Level 2):

- **Standard Inputs, Badges, and Buttons:** `0.5rem` (8px) radius to maintain an efficient, professional silhouette without harsh brutalist corners.
- **Containers & Course Detail Cards:** `0.75rem` to `1rem` (12px to 16px) radius (`rounded-lg`), delivering soft visual encapsulation for grouped academic content.
- **Modal Dialogs & Floating Panels:** `1rem` (16px) radius (`rounded-lg`) for structural prominence.
- **Pill Exceptions:** Filter toggle tags and status indicator badges optionally adopt full pill geometry (`9999px`) strictly when displaying atomic statuses (e.g., `Open`, `Waitlist`, `4.0 Credits`).

## Components

### Action Buttons
- **Primary:** Background `#4F46E5`, text `#FFFFFF`, border none. Hover: `#4338CA`. Active: `#3730A3`. Focus: ring `2px #EEF2FF`, outline `2px #4F46E5`.
- **Secondary / Outline:** Background `#FFFFFF`, text `#0F172A`, border `1px solid #E2E8F0`. Hover: background `#F8FAFC`, border `#CBD5E1`.
- **Ghost:** Background transparent, text `#475569`. Hover: background `#F1F5F9`, text `#0F172A`.
- **Destructive:** Background `#EF4444`, text `#FFFFFF`. Hover: `#DC2626`. Focus: ring `2px #FEF2F2`, outline `2px #EF4444`.

### Course Detail Cards
- Structured container using Level 1 elevation with a solid `#FFFFFF` fill and `#E2E8F0` perimeter border.
- Card Header: Features Course Code in tabular semibold typography alongside the department chip and credit counter.
- Card Body: Displays course title, professor name with availability indicator, meeting times, and prerequisite alerts.
- Card Footer: Sub-divided by a top divider `#F1F5F9`, hosting quick-action register/drop triggers and syllabus preview links.

### Academic Badge Chips
- Pill-shaped (`9999px`) or soft-radius (`6px`), padding `2px 8px`, typography `label-sm`.
- **Enrolled / Open:** Background `#ECFDF5`, text `#065F46`, border `1px solid #A7F3D0`.
- **Waitlisted:** Background `#FFFBEB`, text `#92400E`, border `1px solid #FDE68A`.
- **Full / Closed:** Background `#FEF2F2`, text `#991B1B`, border `1px solid #FECACA`.
- **Prerequisite Met:** Background `#F0F9FF`, text `#075985`, border `1px solid #BAE6FD`.

### Stats Metrics Cards
- Dashboard KPI cards displaying accumulated credits, degree completion percentage, and current GPA.
- Metric values styled with `headline-lg` utilizing tabular numbers.
- Integrated mini-progress bar track (`#E2E8F0`) with dynamic primary fill (`#4F46E5`).

### Input Fields & Search Bars
- Background `#FFFFFF`, border `1px solid #CBD5E1`, text `#0F172A`, placeholder `#94A3B8`.
- Height: `40px` (standard desktop form), padding `0 12px`.
- Focus state: Border `#4F46E5`, box-shadow `0 0 0 3px rgba(79, 70, 229, 0.12)`.
- Search bars include an integrated left-aligned search icon in `#94A3B8` and a keyboard shortcut badge (`⌘K`) right-aligned in `#F1F5F9`.

### Data Tables (Course Rosters & Degree Audits)
- Header row: Background `#F8FAFC`, text `#475569` in `label-md`, border-bottom `1px solid #E2E8F0`.
- Body rows: Height `52px`, text `#0F172A` in `body-sm`, border-bottom `1px solid #F1F5F9`. Alternate row hover: `#F8FAFC`.
- All quantitative columns (Credits, Enrolled/Capacity, Term) right-aligned or center-aligned with explicit tabular numbers.

### Modal Dialogs
- Fixed widths: `480px` for confirmation prompts (e.g., Course Drop Confirmation) and `720px` for Section Comparison modals.
- Header contains title in `headline-sm`, subtitle in `body-sm`, and explicit close button.
- Footer features right-aligned actions with the cancel/secondary button placed left of the primary confirm button.