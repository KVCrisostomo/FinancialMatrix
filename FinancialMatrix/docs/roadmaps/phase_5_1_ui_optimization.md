# File Path: ./docs/roadmaps/phase_5_1_ui_optimization.md

# Phase 5.1: Visual Brand Identity & UI Optimization

## Step 1: Pre-Authentication "Unlock Ledger" Page & Brand Alignment
- **Task:** Refactor the initial application entry screen (the biometric/PIN login gate) to display the application name and establish the official visual design identity from the very first interaction.
- **Implementation:** Modify the entry composable component to apply a solid `#0B1A30` midnight navy blue background to the surface layout. Integrate the application name "Financial Matrix Ledger" using stylized, premium gold typography accents, ensuring the "Unlock Ledger" interactive elements and PIN entry interfaces visually unify with our new brand palette.

## Step 2: Post-Authentication Transition Loading Stage
- **Task:** Introduce a dedicated, system-wide transaction loading view that fires sequentially right after application boot-up and authentication clearance.
- **Implementation:** Map a clean `Screen.Loading` destination within your Compose Navigation controller (`androidx.navigation.compose`). Ensure that once the user successfully clears the biometric security prompt (PIN `1236`), the pipeline redirects to this sleek intermediate layout showcasing the application name, centered official logo asset, and an indeterminate Material 3 progress circle before revealing the active ledger.

## Step 3: Credit Card Layout Transformation & Redundancy Removal
- **Task:** Refactor the layout configuration on the Credit Cards dashboard page to prioritize scannability for high-volume storage states.
- **Implementation:** Locate `CreditCardScreen.kt` and replace the horizontal container component (`LazyRow`) with a clean vertical scrolling structure (`LazyColumn`). Concurrently, completely excise the secondary inline "+" icon button from the profile headers, re-routing all card-creation intent exclusively through the main layout action button.