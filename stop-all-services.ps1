$ErrorActionPreference = 'SilentlyContinue'

$ports = 3000, 8080, 8081, 8082, 8083, 8084, 8085, 8761
$windowTitles = @(
    'Eureka Server (8761)',
    'ADMIN Service (8081)',
    'EVENT Service (8082)',
    'DEMP Identity Service (8083)',
    'PAYMENT Service (8084)',
    'TICKETS Service (8085)',
    'API Gateway (8080)',
    'Frontend (3000)'
)

Write-Host 'Stopping Digital Event Management services...' -ForegroundColor Cyan

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($connection in $connections) {
        $pid = $connection.OwningProcess
        if ($pid -and $pid -ne 0) {
            try {
                Stop-Process -Id $pid -Force
                Write-Host "Stopped process on port $port (PID $pid)" -ForegroundColor Green
            } catch {
            }
        }
    }
}

Get-Process powershell -ErrorAction SilentlyContinue |
    Where-Object { $windowTitles -contains $_.MainWindowTitle } |
    ForEach-Object {
        try {
            Stop-Process -Id $_.Id -Force
            Write-Host "Closed window: $($_.MainWindowTitle)" -ForegroundColor Green
        } catch {
        }
    }

Write-Host 'Stop commands completed.' -ForegroundColor Cyan