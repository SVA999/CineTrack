@echo off
setlocal
cd /d "%~dp0\.."

echo === CineTrack - Cloud Firestore ===
where firebase.cmd >nul 2>nul
if errorlevel 1 (
  echo Firebase CLI no esta instalado.
  echo Instala con: npm.cmd install -g firebase-tools
  exit /b 1
)

echo Proyecto: cinetrack-9df50
echo Desplegando reglas e indices de Firestore...
call firebase.cmd deploy --only firestore --project cinetrack-9df50
if errorlevel 1 (
  echo.
  echo Fallo el deploy de Firestore.
  exit /b 1
)

echo.
echo Firestore publicado correctamente.
echo Se incluyen los indices compuestos para consultas de reviews por usuario y por titulo.
endlocal
