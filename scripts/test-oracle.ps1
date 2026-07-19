param(
    [Parameter(Mandatory=$true)][string]$HostName,
    [int]$Port = 1521
)

Write-Host "Testing Oracle listener at ${HostName}:${Port}" -ForegroundColor Cyan
$result = Test-NetConnection -ComputerName $HostName -Port $Port
$result | Format-List ComputerName, RemoteAddress, RemotePort, TcpTestSucceeded

if (-not $result.TcpTestSucceeded) {
    Write-Host "Connection failed. Check IP, listener, firewall, network and Docker/WSL port publishing." -ForegroundColor Red
    exit 1
}

Write-Host "Oracle listener port is reachable." -ForegroundColor Green
