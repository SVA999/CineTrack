$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== CineTrack · AAB release ===" -ForegroundColor Cyan

$required = @("app/google-services.json", "local.properties", "keystore.properties")
foreach ($file in $required) {
    if (-not (Test-Path $file)) { throw "Falta $file" }
}

$text = Get-Content "local.properties" -Raw
if ($text -notmatch "TMDB_READ_TOKEN\s*=\s*\S+") {
    throw "Falta TMDB_READ_TOKEN en local.properties. No generes el release final usando mocks."
}

& .\gradlew.bat clean test bundleRelease
if ($LASTEXITCODE -ne 0) { throw "Falló el build release." }

$aab = "app/build/outputs/bundle/release/app-release.aab"
if (-not (Test-Path $aab)) { throw "No se encontró $aab" }

Write-Host "`nAAB generado:" -ForegroundColor Green
Write-Host (Resolve-Path $aab)
Write-Host "`nAntes de Play Console: prueba release, revisa versionCode/versionName, privacidad, Data Safety y URLs de eliminación." -ForegroundColor Yellow
