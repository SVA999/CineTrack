$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== CineTrack · prueba en dispositivo físico ===" -ForegroundColor Cyan
Write-Host "Activa Opciones de desarrollador + Depuración USB y acepta la huella RSA del PC." -ForegroundColor Gray

$adb = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adb) {
    throw "No se encontró adb en PATH. Ábrelo desde Android Studio o agrega platform-tools al PATH."
}

adb devices
$devices = (adb devices | Select-String "\tdevice$")
if (-not $devices) {
    throw "No hay un dispositivo Android autorizado conectado."
}

& .\gradlew.bat installDebug
if ($LASTEXITCODE -ne 0) { throw "No se pudo instalar el APK debug." }

Write-Host "`nApp instalada. Prueba manual obligatoria:" -ForegroundColor Green
@(
  "Registro / login / recuperar contraseña / logout",
  "Inicio, Buscar, Detalle, Mi lista y Perfil",
  "Guardar título, cambiar estado, favorito, rating y comentario",
  "Cerrar completamente la app y confirmar persistencia Room",
  "Reiniciar teléfono y repetir persistencia",
  "Modo oscuro y claro",
  "Compartir enlace y volver mediante deep link",
  "Eliminar una cuenta DE PRUEBA"
) | ForEach-Object { Write-Host " - $_" }
