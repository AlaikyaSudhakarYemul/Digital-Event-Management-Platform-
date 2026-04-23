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

function Get-MavenWrapperCommand {
    param(
        [string]$WorkingDir,
        [string]$RepoRoot
    )

    $localWrapper = Join-Path $WorkingDir "mvnw.cmd"
    if (Test-Path $localWrapper) {
        return "& ./mvnw.cmd spring-boot:run"
    }

    $sharedWrapper = Join-Path $RepoRoot "backend/DEMP/mvnw.cmd"
    if (Test-Path $sharedWrapper) {
        return "& '$sharedWrapper' spring-boot:run"
    }

    throw "No Maven wrapper found for $WorkingDir"
}

Write-Host "Launching Digital Event Management microservices..." -ForegroundColor Cyan

$eurekaDir = Join-Path $repoRoot "backend/eureka-server"
$adminDir = Join-Path $repoRoot "backend/ADMIN"
$eventDir = Join-Path $repoRoot "backend/EVENT"
$dempDir = Join-Path $repoRoot "backend/DEMP"
$paymentDir = Join-Path $repoRoot "backend/PAYMENT"
$ticketsDir = Join-Path $repoRoot "backend/TICKETS"
$gatewayDir = Join-Path $repoRoot "backend/api-gateway"
$frontendDir = Join-Path $repoRoot "frontend/demp-app"

# Check which services have pom.xml files
$availableServices = @()
foreach ($serviceDir in @($eurekaDir, $adminDir, $eventDir, $dempDir, $paymentDir, $ticketsDir, $gatewayDir)) {
    if (Test-Path (Join-Path $serviceDir "pom.xml")) {
        $availableServices += $serviceDir
    } else {
        $serviceName = Split-Path -Leaf $serviceDir
        Write-Host "⚠ Skipping $serviceName - pom.xml or mvnw not found" -ForegroundColor Yellow
    }
}

if ($availableServices.Count -eq 0) {
    Write-Host "ERROR: No valid microservices found. Exiting." -ForegroundColor Red
    exit 1
}

# 1) Start discovery first if available
if ($eurekaDir -in $availableServices) {
    Start-ServiceWindow -Title "Eureka Server (8761)" -WorkingDir $eurekaDir -Command (Get-MavenWrapperCommand -WorkingDir $eurekaDir -RepoRoot $repoRoot)
    Start-Sleep -Seconds 8
}

# 2) Start core business services (in parallel)
if ($adminDir -in $availableServices) {
    Start-ServiceWindow -Title "ADMIN Service (8081)" -WorkingDir $adminDir -Command (Get-MavenWrapperCommand -WorkingDir $adminDir -RepoRoot $repoRoot)
}
if ($eventDir -in $availableServices) {
    Start-ServiceWindow -Title "EVENT Service (8082)" -WorkingDir $eventDir -Command (Get-MavenWrapperCommand -WorkingDir $eventDir -RepoRoot $repoRoot)
}
if ($dempDir -in $availableServices) {
    Start-ServiceWindow -Title "DEMP Identity Service (8083)" -WorkingDir $dempDir -Command (Get-MavenWrapperCommand -WorkingDir $dempDir -RepoRoot $repoRoot)
}
if ($paymentDir -in $availableServices) {
    Start-ServiceWindow -Title "PAYMENT Service (8084)" -WorkingDir $paymentDir -Command (Get-MavenWrapperCommand -WorkingDir $paymentDir -RepoRoot $repoRoot)
}
if ($ticketsDir -in $availableServices) {
    Start-ServiceWindow -Title "TICKETS Service (8085)" -WorkingDir $ticketsDir -Command (Get-MavenWrapperCommand -WorkingDir $ticketsDir -RepoRoot $repoRoot)
}
Start-Sleep -Seconds 8

# 3) Start API Gateway after services register
if ($gatewayDir -in $availableServices) {
    Start-ServiceWindow -Title "API Gateway (8080)" -WorkingDir $gatewayDir -Command (Get-MavenWrapperCommand -WorkingDir $gatewayDir -RepoRoot $repoRoot)
}

# 4) Start frontend
Start-ServiceWindow -Title "Frontend (3000)" -WorkingDir $frontendDir -Command "$env:REACT_APP_API_BASE_URL='http://localhost:8080'; npm start"

Write-Host "All launch commands sent." -ForegroundColor Cyan
Write-Host "Check Eureka at: http://localhost:8761" -ForegroundColor Cyan
Write-Host "Use Ctrl+C in each service window to stop." -ForegroundColor Cyan
