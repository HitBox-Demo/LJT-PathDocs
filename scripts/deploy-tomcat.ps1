$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
$appName = "LJTRouteFlow"
$tomcatPort = if ($env:DMS_TOMCAT_PORT) { $env:DMS_TOMCAT_PORT } else { "8082" }

if (-not $env:CATALINA_HOME) {
    throw 'CATALINA_HOME is not set. Example: $env:CATALINA_HOME="C:\apache-tomcat-9.0.XX"'
}
if (-not (Test-Path $env:CATALINA_HOME)) {
    throw "CATALINA_HOME does not exist: $env:CATALINA_HOME"
}

Push-Location $projectRoot
try {
    mvn clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }

    $war = Join-Path $projectRoot "target\$appName.war"
    if (-not (Test-Path $war)) { throw "WAR was not created: $war" }

    $webapps = Join-Path $env:CATALINA_HOME "webapps"
    $targetWar = Join-Path $webapps "$appName.war"
    $explodedFolder = Join-Path $webapps $appName

    # Remove the current deployment and the legacy DocRoute deployment.
    @(
        $targetWar,
        (Join-Path $webapps "docroute.war")
    ) | ForEach-Object {
        if (Test-Path $_) { Remove-Item $_ -Force }
    }
    @(
        $explodedFolder,
        (Join-Path $webapps "docroute")
    ) | ForEach-Object {
        if (Test-Path $_) { Remove-Item $_ -Recurse -Force }
    }

    Copy-Item $war $targetWar -Force
    Write-Host "Deployed to $targetWar" -ForegroundColor Green
    Write-Host "Start or restart Tomcat, then open http://localhost:$tomcatPort/$appName/" -ForegroundColor Green
} finally {
    Pop-Location
}
