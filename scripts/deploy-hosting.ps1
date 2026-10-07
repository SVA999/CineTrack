$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== CineTrack · Firebase Hosting ===" -ForegroundColor Cyan

$firebase = $null
if (Get-Command firebase.cmd -ErrorAction SilentlyContinue) {
    $firebase = "firebase.cmd"
} elseif (Get-Command firebase -ErrorAction SilentlyContinue) {
    $firebase = "firebase"
}

if (-not $firebase) {
    Write-Host "Firebase CLI no está instalado." -ForegroundColor Yellow
    Write-Host "Instálalo con: npm.cmd install -g firebase-tools" -ForegroundColor White
    throw "Instala Firebase CLI y vuelve a ejecutar este script."
}

Write-Host "Proyecto: cinetrack-9df50" -ForegroundColor Gray
Write-Host "Comprobando sesión..." -ForegroundColor Gray
& $firebase projects:list | Out-Host
if ($LASTEXITCODE -ne 0) {
    Write-Host "No hay sesión válida. Abriendo login..." -ForegroundColor Yellow
    & $firebase login
    if ($LASTEXITCODE -ne 0) { throw "No se pudo iniciar sesión en Firebase CLI." }
}

Write-Host "Desplegando únicamente Firebase Hosting..." -ForegroundColor Cyan
& $firebase deploy --only hosting --project cinetrack-9df50
if ($LASTEXITCODE -ne 0) { throw "Falló el deploy de Firebase Hosting." }

Write-Host "Hosting publicado correctamente." -ForegroundColor Green
Write-Host "Inicio: https://cinetrack-9df50.web.app" -ForegroundColor White
Write-Host "Privacidad: https://cinetrack-9df50.web.app/privacy" -ForegroundColor White
Write-Host "Soporte: https://cinetrack-9df50.web.app/support" -ForegroundColor White
Write-Host "Eliminar cuenta: https://cinetrack-9df50.web.app/delete-account" -ForegroundColor White
