---
name: Cetak Presisi
colors:
  surface: '#fcf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae7e7'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1c1b1b'
  on-surface-variant: '#444748'
  inverse-surface: '#313030'
  inverse-on-surface: '#f3f0ef'
  outline: '#747878'
  outline-variant: '#c4c7c7'
  surface-tint: '#5f5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1c1b1b'
  on-primary-container: '#858383'
  inverse-primary: '#c8c6c5'
  secondary: '#676000'
  on-secondary: '#ffffff'
  secondary-container: '#f3e659'
  on-secondary-container: '#6e6600'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1d1b1a'
  on-tertiary-container: '#868381'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c8c6c5'
  on-primary-fixed: '#1c1b1b'
  on-primary-fixed-variant: '#474646'
  secondary-fixed: '#f3e659'
  secondary-fixed-dim: '#d6ca3f'
  on-secondary-fixed: '#1f1c00'
  on-secondary-fixed-variant: '#4e4800'
  tertiary-fixed: '#e6e1df'
  tertiary-fixed-dim: '#cac6c3'
  on-tertiary-fixed: '#1d1b1a'
  on-tertiary-fixed-variant: '#484645'
  background: '#fcf9f8'
  on-background: '#1c1b1b'
  surface-variant: '#e5e2e1'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '800'
    lineHeight: 38px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.005em
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system delivers an editorial, high-craft mobile experience tailored for textile creators, apparel brands, and bespoke merchandise makers. It merges modern Scandinavian minimalism with the graphic grit and tactile precision of traditional screen printing (sablon presisi).

The visual identity is anchored by high-contrast typographic discipline, crisp structural dividers, expansive warm-toned negative space, and deliberate hits of vibrant emulsion yellow. The emotional resonance is focused, industrial yet elevated, dependable, and meticulously crafted. Micro-interactions should feel immediate, solid, and tactile—reminiscent of squeegee passes, precise registration marks, and heavy textile swatches.

## Colors
The palette balances raw substrate tones against ink-dense graphic values.

- **Background Canvas (`#FAFAF7`):** An organic, unbleached cotton base that softens contrast without reducing legibility.
- **Surface Cards & Containers (`#F1F1ED`):** Subtle tactile separation for interactive groupings, media plates, and nested order cards.
- **Primary Ink / Contrast (`#111111`):** Deep, dense plastisol black reserved for primary buttons, leading graphics, and key framing structures.
- **Primary Text (`#171717`):** Razor-sharp typographical contrast for body copy, titles, and active interface markers.
- **Secondary Muted Text (`#737373`):** Neutral half-tone for secondary labels, technical specifications, and supporting metadata.
- **Structural Borders (`#E5E5E0`):** Fine, hairline definitions echoing registration gridlines and print bed demarcations.
- **Accent Yellow (`#F5E85B`):** Vibrant print-shop yellow utilized sparingly for high-impact notifications, badges, active progress states, and key callout tags.

## Typography
Typographic hierarchy is strictly engineered with **Inter** across all roles, highlighting geometric regularity and neutral balance.

- Use tightened letter spacing (`-0.02em` to `-0.03em`) on larger weights for an editorial, ink-stamped presence.
- Small indicators, registration tags, and order status pills leverage `label-sm` with upper-case styling and wide letter-spacing (`+0.06em`) for mechanical readability.
- All interface copy utilizes Indonesian nomenclature (`Lacak Pesanan`, `Pilih Jenis Kain`, `Spesifikasi Sablon`, `Unggah Desain`).

## Layout & Spacing
A fluid single-column mobile-first layout built with a standard 4px/8px rhythm.

- **Outer Canvas Margins:** Exactly 24px (`1.5rem`) on both horizontal edges to preserve clean whitespace.
- **Card Internals:** 16px to 20px padding (`space-md` to `1.25rem`) to frame textile imagery and production parameters.
- **Section Rhythm:** Vertical stack gap between distinct functional blocks is strictly 24px to 32px (`space-lg` to `space-xl`), allowing high-density order configurations to remain organized.

## Elevation & Depth
Depth in this system avoids blurred, artificial drop shadows in favor of a clean tactile architecture:

- **Surface Level 0 (Canvas):** Pure `#FAFAF7` background substrate.
- **Surface Level 1 (Cards & Groups):** `#F1F1ED` surfaces bound by a single 1px solid hairline border in `#E5E5E0`.
- **Surface Level 2 (Modals & Bottom Sheets):** `#FAFAF7` surface with a top border in `#111111` (1.5px) and a restrained atmospheric ambient shadow: `0 8px 32px rgba(17, 17, 17, 0.08)`.
- **Accent Pop:** Important banners and badges utilize solid fills of `#F5E85B` with `#111111` hairline containment to create flat, high-contrast presence without elevation blur.

## Shapes
Geometry is disciplined, modern, and structural:

- **Cards & Plates:** Exactly 16px border radius (`rounded-lg`), producing framed content cards that sit comfortably within the 24px viewport inset.
- **Interactive Controls (Buttons, Inputs, Selectors):** Uniform 12px radius, delivering a distinct, modern handheld grip.
- **Badges & Swatch Toggles:** 6px to 8px radius for technical elements, retaining a semi-rectilinear, utilitarian print-shop identity.

## Components

### Tombol (Buttons)
- **Tinggi Elemen (Height):** Fixed 52px across all standard actionable states.
- **Varian Primer (Primary):** Solid background `#111111`, label `#FAFAF7` (`label-lg`), border radius 12px. Active press scales down slightly (`0.98`) with zero elevation changes.
- **Varian Aksen (Accent):** Background `#F5E85B`, label `#111111`, border 1px solid `#111111`. Ideal for primary checkout or finalize triggers (`Selesaikan Pesanan`).
- **Varian Sekunder (Secondary / Outline):** Background transparent, 1px border `#E5E5E0`, label `#171717`. Active state fills surface to `#F1F1ED`.

### Input & Formulir (Form Elements)
- **Bidang Input (Fields):** Height 52px, background `#FAFAF7`, 1px solid border `#E5E5E0`, border radius 12px, horizontal padding 16px.
- **Status Fokus (Focus State):** Border switches directly to 1.5px `#111111`.
- **Ikon Status:** Sleek geometric indicators (16px) placed on the trailing edge. Success is indicated by `#111111` checkmarks; input validation alerts render in `#111111` with an accent underline `#F5E85B`.
- **Label & Petunjuk:** Floating or top-stacked using `label-md` (`#737373`).

### Kartu Layanan & Pesanan (Cards)
- **Gaya Dasar:** Surface `#F1F1ED`, border 1px solid `#E5E5E0`, radius 16px, padding 20px.
- **Grid Registrasi:** Subtly marked metadata footers divided by 1px solid `#E5E5E0` lines, displaying production details (`Mesh 77T`, `Plastisol Doff`, `Cotton Combed 24s`).

### Keripik & Penyeleksi (Chips & Selectors)
- **Filter & Ukuran:** Height 36px, background `#FAFAF7`, border 1px solid `#E5E5E0`, radius 8px, font `label-md`.
- **Status Terpilih (Selected):** Background `#111111`, text `#FAFAF7`, border 1px solid `#111111`.
- **Koleksi Warna (Swatches):** Circular/rounded 28px swatches surrounded by a 2px offset border when active.

### Kotak Centang & Radio (Checkboxes & Radio Buttons)
- **Dimensi:** 20px x 20px, radius 4px (checkbox) or 20px (radio).
- **Status Aktif:** Solid fill `#111111` with crisp white inner glyphs; inactive states feature 1.5px border `#E5E5E0`.

### Bilah Status Sablon (Production Status Indicator)
- A specialized progress tracker showing printing stages (`Pemisahan Warna` -> `Afdruk Film` -> `Proses Sablon` -> `Pengecekan Kualitas` -> `Siap Kirim`).
- Uses high-contrast `#111111` segmented rules with accent `#F5E85B` indicator pins.