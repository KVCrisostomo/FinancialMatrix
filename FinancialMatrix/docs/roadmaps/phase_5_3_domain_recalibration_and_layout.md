# File Path: ./docs/roadmaps/phase_5_3_domain_recalibration_and_layout.md

# Phase 5.3: Domain Engine Recalibration & Layout Realignment

## Step 1: Credit Card Relative Due Date Engine Calibration
- **Task:** Transition the credit card tracking architecture from static day-of-the-month inputs to a dynamic, relative billing-cycle offset calculation.
- **Implementation:** Update the database layer schema/entity to swap out the absolute day field (`dueDateDay`) for a relative parameter (`daysAfterBillingDate`). Refactor `StatementCycleCalculator.kt` so that instead of clamping hard calendar days, the engine dynamically applies `.plusDays(daysAfterBillingDate)` to the designated statement date. This allows Java's native temporal system to automatically calculate accurate, rolling payment timelines regardless of whether a month contains 28, 29, 30, or 31 days.

## Step 2: Savings Dashboard Layout Migration
- **Task:** Relocate the Savings KPI summary dashboard block to clean up vertical space within the high-density transaction workspace.
- **Implementation:** Un-embed the Savings metric composable layer completely from the Expenses Ledger interface (`TransactionScreen.kt`). Re-route its state parameters and cleanly mount the structural summary card component at the top header area of the Incomes ledger layout (`IncomeLedgerScreen.kt`), allowing the Expenses dashboard to dedicate 100% of its vertical layout to row processing.

## Step 3: Expense Item Spatial Optimization
- **Task:** Maximize screen real estate within individual transaction listings by optimizing text positioning parameters.
- **Implementation:** Locate the composable layout item responsible for rendering individual expense rows. Shift the transaction timestamp/date text block from its legacy lower-left baseline alignment to the upper-right quadrant of the item card container. This modification allows long category string names to expand horizontally without causing multi-line word wrapping layout shifts.