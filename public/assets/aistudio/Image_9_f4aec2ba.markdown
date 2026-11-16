---
name: Serene Oasis
colors:
  surface: '#fcf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae7e7'
  surface-container-highest: '#e4e2e1'
  on-surface: '#1b1c1c'
  on-surface-variant: '#44483f'
  inverse-surface: '#303030'
  inverse-on-surface: '#f3f0f0'
  outline: '#75786e'
  outline-variant: '#c5c8bc'
  surface-tint: '#526442'
  primary: '#526442'
  on-primary: '#ffffff'
  primary-container: '#9caf88'
  on-primary-container: '#324224'
  inverse-primary: '#b9cda4'
  secondary: '#735c00'
  on-secondary: '#ffffff'
  secondary-container: '#fed65b'
  on-secondary-container: '#745c00'
  tertiary: '#615e57'
  on-tertiary: '#ffffff'
  tertiary-container: '#aca89f'
  on-tertiary-container: '#3f3d37'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d5e9bf'
  primary-fixed-dim: '#b9cda4'
  on-primary-fixed: '#111f05'
  on-primary-fixed-variant: '#3b4c2c'
  secondary-fixed: '#ffe088'
  secondary-fixed-dim: '#e9c349'
  on-secondary-fixed: '#241a00'
  on-secondary-fixed-variant: '#574500'
  tertiary-fixed: '#e7e2d8'
  tertiary-fixed-dim: '#cac6bd'
  on-tertiary-fixed: '#1d1c16'
  on-tertiary-fixed-variant: '#494740'
  background: '#fcf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e1'
typography:
  headline-xl:
    fontFamily: Playfair Display
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
  headline-lg:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Playfair Display
    fontSize: 24px
    fontWeight: '500'
    lineHeight: 32px
  headline-sm:
    fontFamily: Playfair Display
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
---

## Brand & Style

This design system embodies a tranquil, high-end sanctuary aesthetic. It targets discerning clientele seeking relaxation, self-care, and professional beauty treatments. The emotional response is deeply calming, reassuring, and luxurious, cultivating a sense of effortless elegance and personalized care from the first digital touchpoint.

The visual execution blends **Minimalism** with **Tactile** warmth—relying on expansive negative space, quiet typography contrasts, and soft organic tones to simulate the physical calm of a high-end spa environment.

## Colors

The palette grounds the interface in organic tranquility. A warm cream background (`#F5F0E6`) eliminates stark digital fatigue, while sage green (`#9CAF88`) signals organic health, active states, and primary actions. Refined gold (`#D4AF37`) acts as a precise accent for ratings, elite tiers, and premium badges. Deep charcoal (`#2C2C2C`) serves as the neutral text color, ensuring high legibility without the harshness of pure black.

## Typography

Typography pairs the literary, high-contrast elegance of Playfair Display for headlines with the clean, utilitarian legibility of Inter for body copy and UI labels. 

Headlines above 32px adapt gracefully on mobile viewports: reduce `headline-xl` to 32px and `headline-lg` to 28px to maintain balanced hierarchy and prevent awkward wrapping on narrow screens. Letter spacing on labels is slightly widened to enhance legibility at small sizes.

## Layout & Spacing

A fluid grid system governs the layout, prioritizing generous breathing room to reinforce the spa aesthetic. Content sits within a max-width container on desktop, transitioning to fluid edge-to-edge layouts on mobile with consistent 24px outer margins.

The spacing rhythm is intentionally expanded. Component padding and vertical stacks favor larger gaps (`space-lg` and `space-xl`) to prevent visual crowding and direct the user's focus effortlessly through booking flows.

## Elevation & Depth

Depth is communicated through ultra-soft, warm-tinted ambient shadows and subtle tonal layering against the cream canvas. Avoid harsh black drops; instead, use diffuse shadows tinted with the primary sage or deep neutral tones at very low opacities (e.g., `rgba(44, 44, 44, 0.06)`). Cards and elevated surfaces feel like physical, matte-finished paper lifted gently off the background.

## Shapes

The design system employs a soft, welcoming roundedness level (`2`). Standard UI elements feature 0.5rem (8px) to 1rem (16px) border radiuses, creating gentle, approachable contours that mirror organic forms found in nature and wellness environments. Large containers and feature cards utilize `rounded-xl` (up to 24px) for an enveloping, plush feel.

## Components

- **Buttons:** Primary buttons feature solid sage green (`#9CAF88`) fills, generous horizontal padding, and 16px border-radius, paired with medium weight Inter labels. Secondary actions use ghost styles with delicate sage outlines.
- **Chips & Tags:** Pill-shaped filter chips for treatment categories use cream backgrounds with subtle borders, shifting to solid sage when active.
- **Lists:** Clean vertical stacks with generous row-height, separating list items with soft, low-opacity dividers rather than hard lines.
- **Checkboxes & Radio Buttons:** Custom-styled with rounded geometries, featuring sage green fill states and smooth transition micro-interactions.
- **Input Fields:** Generous touch targets with soft cream-tinted backgrounds, subtle focus rings in sage green, and floating labels in Inter.
- **Cards:** Elevated white or light cream surfaces with 16px corner radii and ambient shadows, used for showcasing individual treatments, therapists, and package bundles.
- **Badges:** Gold (`#D4AF37`) accent badges with crisp label typography for ratings, VIP services, and exclusive member perks.