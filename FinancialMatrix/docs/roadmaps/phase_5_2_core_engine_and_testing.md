# File Path: ./docs/roadmaps/phase_5_2_core_engine_and_testing.md

# Phase 5.2: Core Engine Repair & Validation Suite Synchronization

## Step 4: Storage Access Framework (SAF) CSV Export Pipeline Rehabilitation
- **Task:** Rebuild the broken ledger export routine to turn the inert TopAppBar menu click into a real, data-writing system invocation.
- **Implementation:** Bridge the UI trigger in `TransactionScreen.kt` to the backend processing logic inside `ExportUtils.kt`. Ensure the `rememberLauncherForActivityResult(CreateDocument())` action properly captures the chosen destination URI, resolves the system `ContentResolver` stream, and serializes raw transaction arrays into functional comma-separated values.

## Step 5: Testing Suite Calibration & Coverage Synchronization
- **Task:** Expand the local unit testing frameworks to capture the architectural delta introduced by the UI layout adjustments, transition screens, and file stream logic.
- **Implementation:** Update `FinancialMatrixViewModelTest.kt` to explicitly assert the new sequential flow states (Biometric Success → Loading View Lifecycle → Active Dashboard Emission). Build isolated handler checks verifying that empty ledger values gracefully output headers inside the CSV generator without triggering a file crash.

## Step 6: Local Automation Certification & Git Consolidation
- **Task:** Perform final build validation passes to confirm compliance with our warning-free, zero-regression software delivery rules.
- **Implementation:** Execute the native terminal automation check via `./certify_build.ps1` to run all 26+ existing unit scenario calculations alongside the newly appended coverage modules. Stage and commit the certified modifications to git only when the local execution environment reports a green status.