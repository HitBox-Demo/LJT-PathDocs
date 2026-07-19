$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
$appName = "LJTRouteFlow"
$tomcatPort = if ($env:DMS_TOMCAT_PORT) { $env:DMS_TOMCAT_PORT } else { "8082" }

Write-Host "LJT RouteFlow demo build" -ForegroundColor Cyan
Write-Host "========================"

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven was not found. Install Maven, reopen VS Code, and confirm with: mvn -version"
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java was not found. Install JDK 21 and confirm with: java -version"
}
if (-not $env:CATALINA_HOME) {
    throw 'CATALINA_HOME is not set. Example: $env:CATALINA_HOME="C:\apache-tomcat-9.0.XX"'
}
if (-not (Test-Path $env:CATALINA_HOME)) {
    throw "CATALINA_HOME does not exist: $env:CATALINA_HOME"
}

$env:DMS_DEMO_MODE = "true"

Push-Location $projectRoot
try {
    Write-Host "[1/3] Building WAR in demo mode..." -ForegroundColor Yellow
    mvn clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }

    $war = Join-Path $projectRoot "target\$appName.war"
    if (-not (Test-Path $war)) { throw "WAR was not created: $war" }

    Write-Host "[2/3] Deploying to Tomcat 9..." -ForegroundColor Yellow
    $webapps = Join-Path $env:CATALINA_HOME "webapps"
    $targetWar = Join-Path $webapps "$appName.war"
    $explodedFolder = Join-Path $webapps $appName

    if (Test-Path $targetWar) { Remove-Item $targetWar -Force }
    if (Test-Path $explodedFolder) { Remove-Item $explodedFolder -Recurse -Force }
    Copy-Item $war $targetWar -Force

    Write-Host "[3/3] Deployment complete." -ForegroundColor Green
    Write-Host "WAR: $targetWar"
    Write-Host "Start or restart Tomcat, then open: http://localhost:$tomcatPort/$appName/" -ForegroundColor Green
} finally {
    Pop-Location
}
