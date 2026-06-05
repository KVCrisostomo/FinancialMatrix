# Financial Matrix Ledger (FML)

Financial Matrix Ledger is a high-precision, premium personal finance tracking application built for Android. Adhering to the strict principles of **Clean Architecture** and **MVVM**, the app decouples data, business logic, and presentation to ensure 100% testability, rock-solid data integrity, and fluid 60 FPS performance.

---

## 🚀 Core Functionalities

* **Reactive Data Flow:** Built entirely on top of Kotlin `StateFlow`. Local database modifications instantly propagate upstream from Room DAOs to the Jetpack Compose UI layer.
* **Precision Financial Math:** Eliminates floating-point inaccuracies by isolating currency calculations within pure domain math engines.
* **KPI Exclusion Logic:** Features intelligent internal transfer identification (`.isInternalTransfer()`) to prevent artificial inflation of income or expense metrics.
* **Defensive Statement Bounding:** Utilizes `TemporalAdjusters.lastDayOfMonth()` within the core domain logic to dynamically handle variable billing cycles (e.g., leap years, February ceilings).
* **Secure Local Storage:** Data is 100% localized using Room DB, Jetpack DataStore for preferences, and the Android Storage Access Framework (SAF) for raw CSV transactional exports.

---

## 🔒 Authentication & Entry Gate

To ensure strict financial privacy, entry is blocked at boot-up until local authorization clears:
1. **Pre-Auth Login Screen:** A premium `#0B1A30` Midnight Navy background with Gold typography requires a `PIN` or Biometric clearance.
2. **Intermediate Layout:** Displays an elegant application emblem, app name, and Material 3 progress indicator while initializing systems.
3. **Active Ledger:** Drops the user into the fully reactive dashboard hub.

---

## 📱 Main Screens & Navigation

The global interface features an ergonomic **Right-Side Navigation Drawer** (`ModalNavigationDrawer`). It is anchored to the top-right 3-bar menu icon, making one-handed dashboard switching highly intuitive.

### 1. Expenses Ledger (`TransactionScreen.kt`)
The primary system engine and navigation host. 
* Displays a reactive stream of all debit transactions consolidated via `TransactionUiState`.
* Offloads heavy calculation, sorting, and category filtering to `Dispatchers.Default` to guarantee UI thx`read responsiveness.

### 2. Income Ledger (`IncomeScreen.kt`)
A dedicated tracking space optimized for earnings, historical inputs, and financial growth analytics.
* Monitors monthly baseline earnings and maps individual savings KPIs over time.

### 3. Credit Cards (`CreditCardScreen.kt`)
A high-volume vertical workspace utilizing an optimized Compose `LazyColumn` to handle multi-card balances.
* **Strict Structural Rule:** All card creation configurations must route through the primary Floating Action Button (FAB). Inline "+" header icons are strictly prohibited to maintain layout uniformity.

### 4. Settings (`UserPreferencesRepository`)
Powered by Jetpack DataStore to store app-wide properties without database overhead.
* Manages user-selected global currencies, budget thresholds, and local theme profiles.

---

## 🛠️ Developer Verification & Quality Gates

The project maintains a zero-tolerance policy for technical debt. Before any Git commit is allowed, engineers must run the local certification script:

```powershell
./certify_build.ps1