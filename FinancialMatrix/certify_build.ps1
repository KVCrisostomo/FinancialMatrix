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

# 5. Run Open Source Software Dependency License Validation Gate
Write-Host "Running Open Source Software License Asset Verification..." -ForegroundColor Yellow
./gradlew app:exportLibraryDefinitions
if ($LASTEXITCODE -ne 0) {
    Write-Host "OSS License Metadata Compilation Failed!" -ForegroundColor Red
    exit 1
}

# 6. Precision & Async Flow Auditing
Write-Host "Running Precision & Async Flow Auditing..." -ForegroundColor Yellow

# Check for Double/Float in Domain and Data layers (excluding generated code)
# This enforces the use of BigDecimal for financial precision as per ARCHITECTURE.md
$invalidTypes = Get-ChildItem -Path "app/src/main/java" -Recurse -Include "*.kt" |
    Where-Object { ($_.FullName -match "\\domain\\" -or $_.FullName -match "\\data\\") -and -not ($_.FullName -match "build\\generated") } |
    Select-String -Pattern "\b(Double|Float)\b" -CaseSensitive

if ($invalidTypes) {
    Write-Host "Precision Audit Failed! Found illegal use of Double/Float in Domain or Data layers:" -ForegroundColor Red
    $invalidTypes | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber) -> $($_.Line.Trim())" -ForegroundColor Red }
    exit 1
}

# Check for Turbine usage in ViewModel tests where StateFlow/Flow is present
$vmTests = Get-ChildItem -Path "app/src/test/java" -Recurse -Filter "*ViewModelTest.kt"
foreach ($testFile in $vmTests) {
    $content = Get-Content $testFile.FullName -Raw
    if ($content -match "StateFlow|Flow") {
        if (-not ($content -match "app\.cash\.turbine" -or $content -match "\.test\s*\{")) {
            Write-Host "Async Flow Audit Failed! ViewModel test '$($testFile.Name)' handles Flows but is missing Turbine verification." -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host "Build Certification Successful! Quality gates verified." -ForegroundColor Green
exit 0