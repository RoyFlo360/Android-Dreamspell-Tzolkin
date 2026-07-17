# Destiny Oracle + PSI Chrono — merged screen design

Decision: the **Oracle** and **PSI Chrono** tabs merge into one screen. PSI is
treated as a 6th, *external* seal — the "Akashic" reading that complements the
5-seal cross (Guide · Analog · Destiny · Antipode · Occult) without being part
of it. Uses the [mayan_magic_wisdom_palette](mayan_magic_wisdom_palette.md).

**Seal & tone artwork is NEVER recoloured.** Seals render the real
`glyph1..glyph20.png` assets (true Red/White/Blue/Yellow) and tones render
`tone1..tone13.png` (gold), exactly as today. The Mayan palette applies only to
chrome — backgrounds, bars, nav, stepper, cards, dividers, text. The
terracotta/turquoise/cream seals in the mockups are placeholder SVG shapes only
(the mockup can't embed the real PNGs); ignore their colour.

## What to build

Two layouts by screen size + one variant to A/B test:

| Target | Layout | Notes |
|--------|--------|-------|
| **Phones (standard)** | **Option C** — "Akashic crown" card | PSI shown as a distinct veiled card crowning the oracle; cross stays full size. This is the default. |
| **Tablets / large screens** | **Option A** — dotted circle + PSI tethered above | Cross inside a dotted circle; PSI seal tethered outside/above it. The expansive treatment. |
| **A/B variant (build too)** | **Option D** — collapsible PSI | Same as C but the PSI seal is hidden behind a bar labelled **"PSI Chrono (Akashic)"**; tap to expand/reveal the seal. |

**Both C and D must be runnable in the built app** so we can compare which
reads better for the user. Keep them toggleable (build flag / setting / two
layouts) — don't hard-delete one before the comparison.

## Layout notes

- **Option A** (tablet): PSI tone+seal centered above a dotted circle, dotted
  tether down to the circle; 5-seal cross centered inside; galactic name + Kin
  below. Roomy — only use where vertical space allows (`sw600dp`).
- **Option C** (phone default): PSI in a dashed-gold "✦ Akashic · PSI Chrono"
  card at top (tone+seal on the right, caption on the left); cross below in a
  faint dotted circle; galactic name + Kin.
- **Option D** (variant): collapsed = a single dashed-gold bar
  "▸ PSI Chrono (Akashic)"; cross gets full size. Expanded = "▾" bar opens a
  panel with the PSI tone+seal + its name/tone caption; cross shifts down and
  scales slightly (may scroll on small screens — acceptable for an opt-in reveal).

## Responsive mechanism

Android resource qualifiers: `res/layout/` (C or D) vs `res/layout-sw600dp/`
(A). Same fragment/data code, layout-only difference. Do **not** chase a
"large phone → A" tier via height qualifiers — unreliable; phone=C/D,
tablet=A.

## D — UX cautions (from review)

- Keep the chevron + label obviously tappable (discoverability).
- **Persist** the expanded/collapsed state so it doesn't reset every day.
- Expanding pushes the cross down; expect scroll on small screens.

## Data / implementation reality

- Oracle cross math: existing oracle reducers (`flower.xml` slots:
  guide/destiny/antipode/analog/occult, each tone+glyph).
- PSI tone+seal: separate source (the PSI Chrono table in `Dreamspell.kt`).
  Data for both already exists — merging is a **layout change, not a
  math change**.
- Glyph/tone art in the mockups are placeholders; wire the app's real
  seal/tone image assets into the slots.

Mockups: `scratchpad/oracle_psi_concepts.html`.
