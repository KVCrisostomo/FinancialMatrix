# File Path: ./docs/roadmaps/phase_5_1_ui_theme_and_navigation.md

# Phase 5.1.1: Global Brand Theme Convergence & Navigation Realignment

## Step 1: Global Material 3 Palette Overhaul & Category Contrast Safety
- **Task:** Update the application's central Material 3 theme and color system to apply the newly locked-in visual identity uniformly across all dashboard screens (Expenses, Income, Credit Cards, and Settings).
- **Implementation:** Modify `Color.kt` and `Theme.kt` to establish `#0B1A30` (Midnight Navy Blue) as the root system background and surface color token, utilizing premium gold tones (`#C5A059` and `#E5C17D`) for primary elements, typography, and accent borders.
- **Contrast Safeguard:** Audit all functional semantic color coding (e.g., transaction category tags, expenditure alerts, and the dynamic Green/Orange/Red colors of the Savings Rate KPI engine). Adjust these color hex values to desaturated, high-luminance variants to guarantee optimal readability and strict visual contrast against the dark midnight navy background without sacrificing their underlying analytical meaning.

## Step 2: Right-Side Navigation Drawer Realignment
- **Task:** Refactor the layout mechanics of the main multi-dashboard hub navigation menu to match its physical UI entry point.
- **Implementation:** Locate the central layout scaffolding file containing the `ModalNavigationDrawer` component. Shift the drawer's orientation so that it anchors to and slides open from the **Right side** of the screen instead of the default Left edge. This guarantees a native, ergonomically sound transition sequence when a user taps the 3-bar menu action icon positioned in the top-right corner of the top app bar.

## Step 3: Multi-Page Dashboard Convergence & Regression Pass
- **Task:** Perform a comprehensive style sweep across all active system screens to eliminate residual legacy styling tokens and verify layout integrity.
- **Implementation:** Systematically update `TransactionScreen.kt`, `CreditCardScreen.kt`, `IncomeLedgerScreen.kt`, and the Settings configuration interfaces to consume the new unified theme parameters. Ensure no hardcoded colors break layout boundaries, fix any hidden text element visibility issues, and run a localized compilation test pass to ensure the visual adjustments execute flawlessly with zero compilation warnings.