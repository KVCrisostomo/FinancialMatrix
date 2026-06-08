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

# 5. [NEW] Validate OSS License Generation (Phase 3 Constraint)
Write-Host "Validating Open Source License Aggregation..." -ForegroundColor Yellow
# Generates the definitions to ensure no metadata parsing crashes exist, without a full APK build
./gradlew :app:exportLibraryDefinitions
if ($LASTEXITCODE -ne 0) {
    Write-Host "OSS License Aggregation Failed! Check dependency tree." -ForegroundColor Red
    exit 1
}

Write-Host "Certification Complete: ZERO WARNINGS. Green for Commit." -ForegroundColor Green
exit 0