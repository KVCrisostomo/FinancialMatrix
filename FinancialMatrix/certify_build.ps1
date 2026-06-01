# Financial Matrix Ledger - Build Certification Script
# This script ensures that all unit and performance tests pass before a build is certified.

Write-Host "Starting Build Certification..." -ForegroundColor Cyan

# 1. Run Core Unit Tests
Write-Host "Running Core Unit Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.core.util.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Core Unit Tests Failed!" -ForegroundColor Red
    exit 1
}

# 2. Run Data Layer Tests
Write-Host "Running Data Layer Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.creditcards.data.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Data Layer Tests Failed!" -ForegroundColor Red
    exit 1
}

# 3. Run ViewModel Tests
Write-Host "Running ViewModel Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.features.transactions.ui.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "ViewModel Tests Failed!" -ForegroundColor Red
    exit 1
}

# 4. Run Performance Load Tests
Write-Host "Running Performance Load Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest --tests com.karlvcrisostomo.financialmatrix.performance.*
if ($LASTEXITCODE -ne 0) {
    Write-Host "Performance Tests Failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build Certified! All tests passed." -ForegroundColor Green
