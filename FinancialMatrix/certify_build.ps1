# Financial Matrix Ledger - Build Certification Script
# This script ensures that all unit and performance tests pass before a build is certified.

Write-Host "Starting Build Certification..." -ForegroundColor Cyan

# 1. Run Core Unit & Domain Tests
Write-Host "Running Core Unit & Domain Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.core.util.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.domain.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.transactions.worker.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Core/Domain Unit Tests Failed!" -ForegroundColor Red
    exit 1
}

# 2. Run Data Layer Tests
Write-Host "Running Data Layer Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.creditcards.data.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.income.data.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Data Layer Tests Failed!" -ForegroundColor Red
    exit 1
}

# 3. Run ViewModel & UI State Tests
Write-Host "Running ViewModel & UI State Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.transactions.ui.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.creditcards.ui.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.income.ui.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "ViewModel Tests Failed!" -ForegroundColor Red
    exit 1
}

# 4. Run Performance Load Tests
Write-Host "Running Performance Load Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.performance.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Performance Load Tests Failed!" -ForegroundColor Red
    exit 1
}

# 5. Architectural Integrity Gate (Static Analysis)
Write-Host "Running Architectural Integrity Checks..." -ForegroundColor Yellow

# 5.1 Enforce BigDecimal for Financial Entities
$violation = Get-ChildItem -Path "app/src/main/java/com/karlvcrisostomo/financialmatrix" -Filter "*Entity.kt" -Recurse | Select-String -Pattern "Double", "Float"
if ($violation) {
    Write-Host "Architectural Violation: Double or Float detected in Financial Entities!" -ForegroundColor Red
    $violation | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) - $($_.Line)" }
    exit 1
}

# 5.2 Enforce Turbine for Asynchronous Flow Testing
$testFiles = Get-ChildItem -Path "app/src/test/java/com/karlvcrisostomo/financialmatrix" -Filter "*Test.kt" -Recurse
$missingTurbine = $true
foreach ($file in $testFiles) {
    if (Select-String -Path $file.FullName -Pattern "app.cash.turbine") {
        $missingTurbine = $false
        break
    }
}
if ($missingTurbine) {
    Write-Host "Quality Gate Failure: Turbine library usage not detected in test suite!" -ForegroundColor Red
    exit 1
}

# 5.6 Security: Unencrypted Cache Leak Check
Write-Host "Verifying cache encryption policies..." -ForegroundColor Cyan
$leaks = Get-ChildItem -Path "app/src/main/java/com/karlvcrisostomo/financialmatrix" -Recurse | Select-String -Pattern "getCacheDir\(\)", "getExternalCacheDir\(\)"
if ($leaks) {
    Write-Host "Security Violation: Potential unencrypted cache leak detected!" -ForegroundColor Red
    $leaks | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) - $($_.Line)" }
    exit 1
}

# 6. Zero-Warning Compilation Audit
Write-Host "Auditing for Compiler Warnings..." -ForegroundColor Yellow
./gradlew assembleDebug "-Pandroid.keepWarnings=true" > build_output.txt
if (Select-String -Path "build_output.txt" -Pattern "warning:") {
    Write-Host "Quality Gate Failure: Compiler warnings detected!" -ForegroundColor Red
    exit 1
}

# 7. Run Open Source Software Dependency License Validation Gate
Write-Host "Running Open Source Software License Asset Verification..." -ForegroundColor Yellow
./gradlew app:exportLibraryDefinitions
if ($LASTEXITCODE -ne 0) {
    Write-Host "OSS License Metadata Compilation Failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build Certification Successful! Quality gates verified." -ForegroundColor Green
exit 0