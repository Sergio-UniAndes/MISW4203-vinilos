# Design System Strategy: The Sonic Editorial



## 1. Overview & Creative North Star

This design system is built upon the Creative North Star of **"The Digital Curator."**



Unlike standard music streaming platforms that feel like utility-heavy databases, this system treats music as high-art. We move away from the "grid-of-squares" template toward a high-end editorial layout. The aesthetic mimics a premium vinyl magazine—using intentional asymmetry, oversized typography, and layered depth to give album artwork the "breathing room" it deserves. We are not just building a player; we are building a digital gallery for sound.



## 2. Colors & Tonal Depth

The palette is anchored in a sophisticated "Midnight Ink" base (`#131313`), allowing the vibrant primary and secondary accents to feel like neon lights in a dark studio.



### The "No-Line" Rule

**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning.

Boundaries must be defined solely through background color shifts or tonal transitions. To separate a navigation rail from the main feed, use `surface_container_low` against the `surface` background. Structural integrity comes from color blocks, not "ink" lines.



### Surface Hierarchy & Nesting

Treat the UI as a series of physical layers—like stacked sheets of smoked glass.

- **Base Layer:** `surface` (`#131313`) – The foundation.

- **Secondary Sections:** `surface_container_low` (`#1c1b1b`) – Used for background groupings.

- **Interactive Cards:** `surface_container` (`#201f1f`) or `surface_container_high` (`#2a2a2a`) – Used to "lift" playable content.

- **Floating Elements:** `surface_bright` (`#393939`) – Reserved for active states or high-priority modals.



### The "Glass & Gradient" Rule

To elevate beyond the standard MD3 look, use **Glassmorphism** for floating players and navigation bars.

- **Token:** `surface_container_highest` at 70% opacity with a `24px` backdrop blur.

- **Signature Textures:** For Hero CTAs (e.g., "Play Radio"), use a subtle linear gradient from `primary` (`#cdbdff`) to `primary_container` (`#4400b5`) at a 135-degree angle. This adds "soul" and professional polish that flat fills lack.



## 3. Typography: Editorial Authority

We utilize a dual-font system to balance character with readability.



*   **Display & Headlines (Manrope):** Chosen for its geometric precision and modern "tech-chic" feel. Use `display-lg` for artist names and `headline-md` for album titles. Let these bleed off the grid or overlap imagery slightly to create an editorial vibe.

*   **Body & Labels (Inter):** A workhorse for metadata. Use `body-md` for tracklists and `label-sm` for timestamps.

*   **Hierarchy Tip:** Always pair a `display-sm` (Manrope) header with `label-md` (Inter, All Caps, Letter Spacing 0.05rem) for section titles like "RECENTLY SPUN" to create an authoritative contrast.



## 4. Elevation & Depth

We convey hierarchy through **Tonal Layering** rather than traditional drop shadows.



*   **The Layering Principle:** Place a `surface_container_lowest` card on a `surface_container_low` section to create a soft, natural lift.

*   **Ambient Shadows:** If a floating effect is required (e.g., a "Now Playing" bar), use a tinted shadow. The shadow should be `primary_fixed` at 5% opacity with a `32px` blur and `12px` Y-offset. This mimics natural light reflecting off a colored surface.

*   **The "Ghost Border" Fallback:** If accessibility requires a container edge, use `outline_variant` (`#4a4452`) at **15% opacity**. Never use 100% opaque borders.

*   **Roundedness:** Adhere to the `lg` (1rem / 16px) token for album art and main containers to maintain a soft, modern furniture-like feel. Use `full` for play buttons and chips.



## 5. Components & UI Patterns



### The "Vinilos" Signature Components

*   **Hero Album Card:** Use the `xl` (1.5rem) corner radius. The album art should feature a soft `primary` glow behind it, utilizing the `surface_tint` token to pull the art into the UI.

*   **Interactive Chips:** Use `secondary_container` for active filters. Forbid the use of outlines; instead, use a simple color shift from `surface_container_high` (inactive) to `secondary` (active).

*   **Progress Bars:** The track seeker should use a high-contrast transition from `primary` (elapsed) to `surface_variant` (remaining), with a `secondary` glow on the "scrubber" head.

*   **Lists (Tracklists):** **No dividers.** Separate tracks using the `4` (1rem) spacing token. On hover/active, change the background to `surface_container_lowest` with a `md` (0.75rem) corner radius.



### Input Fields

*   **Search:** Use a "Pill" shape (`full`). Background: `surface_container_high`. Leading icon: `on_surface_variant`. No border. When focused, shift background to `surface_bright` and change the icon to `primary`.



## 6. Do's and Don'ts



### Do

*   **DO** use white space as a structural element. If you think you need a line, try adding `16px` of padding instead.

*   **DO** allow artist imagery to use "Bleed Layouts" (extending to the edge of the screen) to increase immersion.

*   **DO** ensure a 4.5:1 contrast ratio for all metadata using the `on_surface_variant` and `on_surface` tokens.



### Don't

*   **DON'T** use pure black `#000000`. Use the `surface` scale to keep the dark mode feeling "deep" but not "dead."

*   **DON'T** use standard MD3 elevation shadows. Use tonal shifts between `surface_container` levels.

*   **DON'T** clutter the player view. If a piece of metadata isn't essential for the immediate listening experience, hide it in a "Details" sheet using `surface_container_highest`.



## 7. Spacing & Grid

*   **Gutter:** `6` (1.5rem) to provide an airy, high-end feel.

*   **Vertical Rhythm:** Use the `8` (2rem) scale to separate major sections (e.g., Artist Bio vs. Popular Tracks).

*   **Asymmetry:** Occasionally offset images by `12` (3rem) from the left margin to break the "standard app" grid and lean into the editorial aesthetic.