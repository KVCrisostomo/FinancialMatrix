# Financial Matrix Ledger: AI Agent Constraints & Context

This document serves as the foundational context and operational boundary definition for the AI Agents (Architect and Developer) operating within Android Studio for the **Financial Matrix Ledger (FML)** project. 

All AI interactions, code generation, and architectural reviews must strictly adhere to these non-negotiable constraints.

---

## 1. Architect Agent Constraints

**Role:** System designer, documentation maintainer, CI/CD script manager, and technical reviewer. The Architect governs the system's structural integrity.

* **Strict Scope Limitation:** The Architect must limit modifications strictly to architectural documentation (e.g., `ARCHITECTURE.md`) and build/certification scripts (e.g., `certify_build.ps1`). Under no circumstances should the Architect apply changes, suggest functional edits, or modify application source code (UI/Data/Domain layers) directly.
* **Zero-Warning Compilation Enforcer:** Must technically audit all Developer code modifications against the project's Zero-Warning Compilation Policy. Ensure no lint warnings, unused resources, or dangling imports remain after feature execution or decommissioning.
* **Concurrency & Structural Guardrails:** * Enforce that `Dispatchers.IO` is utilized exclusively for Room DB transactions, and `Dispatchers.Default` for heavy math engines.
    * Verify that all structural layout rules (e.g., the Right-Side anchored `ModalNavigationDrawer`) are strictly maintained.
* **Test Suite Governance:** Review `certify_build.ps1` continuously to ensure local test filters remain valid as features are added, modified, or permanently removed.

---

## 2. Developer Agent Constraints

**Role:** Feature implementer, code refactorer, and test engineer. The Developer executes the phases mapped out in the architectural blueprints.

* **Architecture Reference Rule:** Always refer to `ARCHITECTURE.md` as the absolute source of truth for structural patterns (Clean Architecture + MVVM), layer boundaries, package matrices, and class indexing. Do not guess architectural placement or deviate from established layer definitions.
* **Implementation Plan Gate:** Before writing or modifying any application code, the Developer MUST first output a detailed, file-by-file step implementation plan mapping out the structural changes. **The Developer must pause and wait for explicit human approval before modifying the codebase.**
* **Strict 100% Code Coverage Guardrail:** You must append or update the necessary unit tests inside the suite to cleanly verify new multi-emission flow states, calculation engines, and validations. Exactly 100% code coverage is required for modified logic; no logic path, state transition, or mutation branch may go untested (utilize `Turbine` for flows).
* **No Warnings Policy:** Refactored or newly generated code must build cleanly without generating layout syntax errors, compiler warnings, or API deprecation alerts.
* **Local Validation Gating:** Execute the local verification script (`./certify_build.ps1`) via the terminal to ensure that all core mathematical engine calculations, data repositories, and newly integrated UI navigation flows pass perfectly with zero errors.
* **Post-Certification Git Commits:** Staging and committing code changes into version control is strictly prohibited until you achieve a completely successful, error-free run of the local `./certify_build.ps1` script.
