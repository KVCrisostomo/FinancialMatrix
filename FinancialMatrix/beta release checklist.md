# PRE_APK_CHECKLIST.md - Pre-Flight Build Verifications for FML v0.1.0-beta

## 1. App Signing Configuration (`build.gradle.kts`)
- [x] Confirm a dedicated `beta` or `release` signing config is mapped inside the Gradle build file. Real Android hardware will automatically reject an unsigned APK or a raw unsigned App Bundle.
- [x] Ensure the signing properties are pulled securely from local environment parameters or a hidden `local.properties` file—never commit keystore passwords to version control.

## 2. R8/ProGuard Rules Review (`proguard-rules.pro`)
- [x] Explicitly add keep rules for Room runtime objects and SQLCipher support files (`-keep class net.sqlcipher.** { *; }`).
- [x] Preserve our database entity models and `BigDecimal` TypeConverters. If R8 strips out or renames the fields, SQLCipher initialization or serialization will trigger immediate structural crashes.

## 3. Platform Manifest & Permissions Verification (`AndroidManifest.xml`)
- [x] Ensure the biometric API permission is explicitly declared: `<uses-permission android:name="android.permission.USE_BIOMETRIC" />`.
- [x] Confirm that `android:exported="true"` is declared on the root launcher `Activity` context to allow the host system to instantiate the application cleanly.
- [x] Validate that `targetSdk` is aligned with current platform standards (API level 35 or 36) to prevent immediate deprecation overrides by the device operating system.

## 4. Hardware Window Isolation Flag
- [x] Audit the root Compose lifecycle entry point to guarantee `WindowManager.LayoutParams.FLAG_SECURE` is active before the main ledger screen is exposed. This blocks the hardware from writing system snapshots when the app is placed in the background.

## 5. Automated Validation Trigger
- [x] Execute `powershell ./certify_build.ps1` locally to guarantee all static analysis checks pass and all custom `BigDecimal` accounting unit tests execute clean under dynamic locale conditions.