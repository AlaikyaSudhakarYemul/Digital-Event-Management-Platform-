$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

function Start-ServiceWindow {
    param(
        [string]$Title,
        [string]$WorkingDir,
        [string]$Command
    )

    if (-not (Test-Path $WorkingDir)) {
        Write-Host "Skipping $Title. Directory not found: $WorkingDir" -ForegroundColor Yellow
        return
    }

    $psCommand = "`$Host.UI.RawUI.WindowTitle='$Title'; Set-Location '$WorkingDir'; $Command"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $psCommand | Out-Null
    Write-Host "Started $Title" -ForegroundColor Green
}

Write-Host "Launching Digital Event Management microservices..." -ForegroundColor Cyan

$eurekaDir = Join-Path $repoRoot "backend/eureka-server"
$adminDir = Join-Path $repoRoot "backend/ADMIN"
$eventDir = Join-Path $repoRoot "backend/EVENT"
$dempDir = Join-Path $repoRoot "backend/DEMP"
$paymentDir = Join-Path $repoRoot "backend/PAYMENT"
$gatewayDir = Join-Path $repoRoot "backend/api-gateway"
$frontendDir = Join-Path $repoRoot "frontend/demp-app"

# 1) Start discovery first
Start-ServiceWindow -Title "Eureka Server (8761)" -WorkingDir $eurekaDir -Command "& ./mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 8

# 2) Start core business services
Start-ServiceWindow -Title "ADMIN Service (8081)" -WorkingDir $adminDir -Command "& ./mvnw.cmd spring-boot:run"
Start-ServiceWindow -Title "EVENT Service (8082)" -WorkingDir $eventDir -Command "& ./mvnw.cmd spring-boot:run"
Start-ServiceWindow -Title "DEMP Identity Service (8083)" -WorkingDir $dempDir -Command "& ./mvnw.cmd spring-boot:run"
Start-ServiceWindow -Title "PAYMENT Service (8084)" -WorkingDir $paymentDir -Command "& ./mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 8

# 3) Start API Gateway after services register
Start-ServiceWindow -Title "API Gateway (8080)" -WorkingDir $gatewayDir -Command "mvn spring-boot:run"

# 4) Start frontend
Start-ServiceWindow -Title "Frontend (3000)" -WorkingDir $frontendDir -Command "$env:REACT_APP_API_BASE_URL='http://localhost:8080'; npm start"

Write-Host "All launch commands sent." -ForegroundColor Cyan
Write-Host "Check Eureka at: http://localhost:8761" -ForegroundColor Cyan
Write-Host "Use Ctrl+C in each service window to stop." -ForegroundColor Cyan
