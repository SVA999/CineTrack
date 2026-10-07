@echo off
setlocal
cd /d "%~dp0\.."

echo === CineTrack - Firebase Hosting ===
where firebase.cmd >nul 2>nul
if errorlevel 1 (
  echo Firebase CLI no esta instalado.
  echo Instala con: npm.cmd install -g firebase-tools
  exit /b 1
)

echo Proyecto: cinetrack-9df50
echo Desplegando solo Hosting...
call firebase.cmd deploy --only hosting --project cinetrack-9df50
if errorlevel 1 (
  echo.
  echo Fallo el deploy de Firebase Hosting.
  exit /b 1
)

echo.
echo Hosting publicado correctamente.
echo Inicio: https://cinetrack-9df50.web.app
echo Privacidad: https://cinetrack-9df50.web.app/privacy
echo Soporte: https://cinetrack-9df50.web.app/support
echo Eliminar cuenta: https://cinetrack-9df50.web.app/delete-account
endlocal
