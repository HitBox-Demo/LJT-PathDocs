$ErrorActionPreference = "Continue"
Write-Host "LJT RouteFlow environment check" -ForegroundColor Cyan
Write-Host "--------------------------"

function Show-CommandVersion($Name, $Arguments) {
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $cmd) {
        Write-Host "[MISSING] $Name" -ForegroundColor Red
        return
    }
    Write-Host "[FOUND] $Name -> $($cmd.Source)" -ForegroundColor Green
    & $Name $Arguments 2>&1 | Select-Object -First 4
    Write-Host ""
}

Show-CommandVersion "java" "-version"
Show-CommandVersion "javac" "-version"
Show-CommandVersion "mvn" "-version"
Show-CommandVersion "git" "--version"

if ($env:CATALINA_HOME -and (Test-Path $env:CATALINA_HOME)) {
    Write-Host "[FOUND] CATALINA_HOME = $env:CATALINA_HOME" -ForegroundColor Green
} else {
    Write-Host "[MISSING] CATALINA_HOME is not set or the folder does not exist." -ForegroundColor Yellow
    Write-Host 'Example: $env:CATALINA_HOME = "C:\apache-tomcat-9.0.XX"'
}

Write-Host ""
Write-Host "Demo mode defaults to true when DMS_DEMO_MODE is not set."
