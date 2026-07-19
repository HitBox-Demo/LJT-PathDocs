$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
$warName = "LJTRouteFlow.war"

Push-Location $projectRoot
try {
    mvn clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }
    Write-Host "WAR created: target\$warName" -ForegroundColor Green
} finally {
    Pop-Location
}
