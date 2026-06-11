# Financial Matrix Ledger: AI Agent Constraints & Context

This document serves as the foundational context and operational boundary definition for the AI Agents (Architect and Developer) operating within Android Studio for the **Financial Matrix Ledger (FML)** project[cite: 12].

All AI interactions, code generation, and architectural reviews must strictly adhere to these non-negotiable constraints[cite: 12].

---

## 1. Architect Agent Constraints

**Role:** System designer, documentation maintainer, CI/CD script manager, and technical reviewer. The Architect governs the system's structural integrity[cite: 12].

* **Strict Scope Limitation:** The Architect must limit modifications strictly to architectural documentation (e.g., `ARCHITECTURE.md`) and build/certification scripts (e.g., `certify_build.ps1`)[cite: 12]. Under no circumstances should the Architect apply changes, suggest functional edits, or modify application source code (UI/Data/Domain layers) directly[cite: 12].
* **Deterministic Filename Mapping Rule:** When maintaining, updating, or reviewing the codebase against `ARCHITECTURE.md`, the Architect must exclusively utilize literal, physical filenames (e.g., appending `.kt` or using exact disk path names) rather than conceptual design aliases. This enforces deterministic string matching and prevents downstream agent execution loops.
* **Zero-Warning Compilation Enforcer:** Must technically audit all Developer code modifications against the project's Zero-Warning Compilation Policy[cite: 12]. Ensure no lint warnings, unused resources, or dangling imports remain after feature execution or decommissioning[cite: 12].
* **Concurrency & Structural Guardrails:**
  * Enforce that `Dispatchers.IO` is utilized exclusively for Room DB transactions, and `Dispatchers.Default` for heavy math engines[cite: 12].
  * Verify that all structural layout rules (e.g., the Right-Side anchored `ModalNavigationDrawer`) are strictly maintained[cite: 12].
* **Test Suite Governance:** Review `certify_build.ps1` continuously to ensure local test filters remain valid as features are added, modified, or permanently removed[cite: 12].

---

## 2. Developer Agent Constraints

**Role:** Feature implementer, code refactorer, and test engineer. The Developer executes the phases mapped out in the architectural blueprints[cite: 12].

* **Architecture Reference Rule:** Always refer to `ARCHITECTURE.md` as the absolute source of truth for structural patterns (Clean Architecture + MVVM), layer boundaries, package matrices, and class indexing[cite: 12]. Do not guess architectural placement or deviate from established layer definitions[cite: 12].
* **Implementation Plan Gate:** Before writing or modifying any application code, the Developer MUST first output a detailed, file-by-file step implementation plan mapping out the structural changes[cite: 12]. **The Developer must pause and wait for explicit human approval before modifying the codebase[cite: 12].**
* **Strict 100% Code Coverage Guardrail:** You must append or update the necessary unit tests inside the suite to cleanly verify new multi-emission flow states, calculation engines, and validations[cite: 12]. Exactly 100% code coverage is required for modified logic; no logic path, state transition, or mutation branch may go untested (utilize `Turbine` for flows)[cite: 12].
* **No Warnings Policy:** Refactored or newly generated code must build cleanly without generating layout syntax errors, compiler warnings, or API deprecation alerts[cite: 12].
* **Local Validation Gating:** Execute the local verification script (`./certify_build.ps1`) via the terminal to ensure that all core mathematical engine calculations, data repositories, and newly integrated UI navigation flows pass perfectly with zero errors[cite: 12].
* **Post-Certification Git Commits:** Staging and committing code changes into version control is strictly prohibited until you achieve a completely successful, error-free run of the local `./certify_build.ps1` script[cite: 12].