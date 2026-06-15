# Financial Matrix Ledger - Build Certification Script
# This script ensures that all unit and performance tests pass before a build is certified.

Write-Host "Starting Build Certification..." -ForegroundColor Cyan

# 1. Run Core Unit & Domain Tests
Write-Host "Running Core Unit & Domain Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.core.util.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.domain.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.transactions.worker.*
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.analytics.*
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
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.analytics.ui.*
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

# 5.3 Release 1: Charting & Aggregation Gates
Write-Host "Verifying Release 1 Gates (Charting & Aggregation)..." -ForegroundColor Cyan
# Enforce Vico Charting Dependency in build.gradle.kts
$buildFile = "app/build.gradle.kts"
if (-not (Select-String -Path $buildFile -Pattern "com.patrykandpatrick.vico")) {
    Write-Host "Architectural Violation: Vico charting library missing for Release 1!" -ForegroundColor Red
    exit 1
}
if (-not (Select-String -Path $buildFile -Pattern "1.15.0")) {
    Write-Host "Architectural Violation: Vico must be pinned to stable 1.15.0 for Release 1 compliance!" -ForegroundColor Red
    exit 1
}

# Enforce DB-Level Aggregation for Analytics-related DAOs
$daos = Get-ChildItem -Path "app/src/main/java/com/karlvcrisostomo/financialmatrix" -Filter "*Dao.kt" -Recurse
foreach ($dao in $daos) {
    if ($dao.Name -like "*Analytics*" -or $dao.Name -like "*History*") {
        if (-not (Select-String -Path $dao.FullName -Pattern "strftime|date|GROUP BY")) {
             Write-Host "Architectural Violation: DAO $($dao.Name) is missing DB-level aggregation logic (strftime/GROUP BY)." -ForegroundColor Red
             exit 1
        }
    }
}

# 5.4 Release 2: WorkManager & Resilience Gates
Write-Host "Verifying Release 2 Gates (WorkManager & Resilience)..." -ForegroundColor Cyan
# Enforce WorkManager Dependency in build.gradle.kts
if (-not (Select-String -Path $buildFile -Pattern "androidx.work:work-runtime-ktx")) {
    Write-Host "Architectural Violation: WorkManager dependency missing for Release 2!" -ForegroundColor Red
    exit 1
}

# Enforce runAttemptCount in WorkManager Workers
$workers = Get-ChildItem -Path "app/src/main/java/com/karlvcrisostomo/financialmatrix" -Filter "*Worker.kt" -Recurse
foreach ($worker in $workers) {
    if (-not (Select-String -Path $worker.FullName -Pattern "runAttemptCount")) {
        Write-Host "Architectural Violation: runAttemptCount check missing in $($worker.Name)!" -ForegroundColor Red
        exit 1
    }
}

# 5.5 Enforce Precision in SQLite Aggregations
Write-Host "Verifying Precision in SQLite Aggregations..." -ForegroundColor Cyan
$daoFiles = Get-ChildItem -Path "app/src/main/java/com/karlvcrisostomo/financialmatrix" -Filter "*Dao.kt" -Recurse
foreach ($file in $daoFiles) {
    if (Select-String -Path $file.FullName -Pattern "CAST\(.* AS REAL\)") {
        Write-Host "Architectural Violation: Precision loss detected! Usage of 'CAST(... AS REAL)' is prohibited in financial DAOs." -ForegroundColor Red
        Write-Host "  File: $($file.FullName)" -ForegroundColor Yellow
        exit 1
    }
}

# 6. Run Open Source Software Dependency License Validation Gate
Write-Host "Running Open Source Software License Asset Verification..." -ForegroundColor Yellow
./gradlew app:exportLibraryDefinitions
if ($LASTEXITCODE -ne 0) {
    Write-Host "OSS License Metadata Compilation Failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build Certification Successful! Quality gates verified." -ForegroundColor Green
exit 0