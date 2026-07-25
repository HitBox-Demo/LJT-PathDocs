$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent

Push-Location $projectRoot
try {
    Write-Host "LJT RouteFlow verification" -ForegroundColor Cyan
    Write-Host "=========================="

    if (Test-Path "src\src") {
        throw "Duplicate source tree found: src\src"
    }
    if (-not (Test-Path "pom.xml")) {
        throw "pom.xml was not found. Open the project root folder."
    }

    $createJsp = Get-Content "src\main\webapp\WEB-INF\views\documents\create.jsp" -Raw
    $editJsp = Get-Content "src\main\webapp\WEB-INF\views\documents\edit.jsp" -Raw
    foreach ($item in @(
        @{ Name = "create.jsp"; Text = $createJsp },
        @{ Name = "edit.jsp"; Text = $editJsp }
    )) {
        $collectorCount = ([regex]::Matches($item.Text, "\bdata-image-collector(?:\s|>|=)")).Count
        $inputCount = ([regex]::Matches($item.Text, "\bdata-image-input(?:\s|>|=)")).Count
        if ($collectorCount -ne 1 -or $inputCount -ne 1) {
            throw "$($item.Name) must contain exactly one image collector and one active image input."
        }
    }

    Get-Content "src\main\webapp\manifest.json" -Raw | ConvertFrom-Json | Out-Null
    [xml](Get-Content "src\main\webapp\WEB-INF\web.xml" -Raw) | Out-Null

    Write-Host "[1/2] Running Maven tests..." -ForegroundColor Yellow
    mvn clean test
    if ($LASTEXITCODE -ne 0) { throw "Maven tests failed." }

    Write-Host "[2/2] Packaging WAR..." -ForegroundColor Yellow
    mvn package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven package failed." }

    $war = "target\LJTRouteFlow.war"
    if (-not (Test-Path $war)) {
        throw "WAR was not created: $war"
    }

    Write-Host "Verification passed." -ForegroundColor Green
    Write-Host "WAR: $war" -ForegroundColor Green
    Write-Host "Complete docs\MANUAL_ACCEPTANCE_CHECKLIST.md after deployment." -ForegroundColor Yellow
} finally {
    Pop-Location
}
