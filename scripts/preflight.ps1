$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== CineTrack preflight ===" -ForegroundColor Cyan

$problems = @()
if (-not (Test-Path "app/google-services.json")) {
    $problems += "Falta app/google-services.json (Firebase real todavía no estará activo)."
}
if (-not (Test-Path "local.properties")) {
    $problems += "Falta local.properties. Android Studio suele crearlo con sdk.dir; agrega además TMDB_READ_TOKEN."
} else {
    $text = Get-Content "local.properties" -Raw
    if ($text -notmatch "TMDB_READ_TOKEN\s*=\s*\S+") {
        $problems += "Falta TMDB_READ_TOKEN en local.properties; se usará el catálogo mock."
    }
}

if ($problems.Count -gt 0) {
    Write-Host "Advertencias:" -ForegroundColor Yellow
    $problems | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }
} else {
    Write-Host "Credenciales/configuración básica detectadas." -ForegroundColor Green
}

Write-Host "`nEjecutando tests unitarios..." -ForegroundColor Cyan
& .\gradlew.bat test
if ($LASTEXITCODE -ne 0) { throw "Fallaron los tests." }

Write-Host "`nGenerando APK debug..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) { throw "Falló assembleDebug." }

$apk = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apk) {
    Write-Host "OK: $apk" -ForegroundColor Green
}
Write-Host "=== Preflight completado ===" -ForegroundColor Green
