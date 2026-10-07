param(
    [Parameter(Mandatory = $true)]
    [string[]]$Sha256Fingerprints
)
$ErrorActionPreference = 'Stop'
$normalized = @($Sha256Fingerprints | ForEach-Object {
    $value = $_.Trim().ToUpperInvariant()
    if ($value -notmatch '^([0-9A-F]{2}:){31}[0-9A-F]{2}$') {
        throw 'Cada huella debe ser SHA-256 de 32 bytes separados por dos puntos.'
    }
    $value
})
$root = Split-Path $PSScriptRoot -Parent
$destination = Join-Path $root 'hosting/.well-known/assetlinks.json'
$entry = @{
    relation = @('delegate_permission/common.handle_all_urls')
    target = @{
        namespace = 'android_app'
        package_name = 'com.cinetrack.app'
        sha256_cert_fingerprints = $normalized
    }
}
ConvertTo-Json -InputObject @($entry) -Depth 6 | Set-Content -LiteralPath $destination -Encoding utf8
Write-Output "Archivo generado: $destination"
Write-Output 'Revisar la huella del certificado de la APK instalada antes de publicar Hosting.'
