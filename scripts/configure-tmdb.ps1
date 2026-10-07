$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== CineTrack · configurar TMDB ===" -ForegroundColor Cyan
Write-Host "Pega el API Read Access Token de TMDB (Bearer)." -ForegroundColor Gray
$secureToken = Read-Host "TMDB token" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureToken)
try {
    $token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr).Trim()
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}
if ([string]::IsNullOrWhiteSpace($token)) { throw "El token no puede quedar vacío." }

$localPath = "local.properties"
$lines = @()
if (Test-Path $localPath) {
    $lines = Get-Content $localPath | Where-Object {
        $_ -notmatch '^TMDB_READ_TOKEN=' -and $_ -notmatch '^CINETRACK_WEB_BASE_URL='
    }
}
$lines += "TMDB_READ_TOKEN=$token"
$lines += "CINETRACK_WEB_BASE_URL=https://cinetrack-9df50.web.app"
Set-Content -Path $localPath -Value $lines -Encoding UTF8

Write-Host "TMDB configurado en local.properties." -ForegroundColor Green
Write-Host "No subas local.properties a Git ni lo compartas públicamente." -ForegroundColor Yellow
