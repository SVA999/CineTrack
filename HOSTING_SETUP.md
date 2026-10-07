# Firebase Hosting — CineTrack

## Estado actual

**DESPLEGADO** el 15 de septiembre de 2026 en el proyecto `cinetrack-9df50`.

URLs públicas:
- `https://cinetrack-9df50.web.app`
- `https://cinetrack-9df50.web.app/privacy`
- `https://cinetrack-9df50.web.app/support`
- `https://cinetrack-9df50.web.app/delete-account`

El despliegue se realizó correctamente con Firebase CLI 15.30.1 usando `firebase deploy --only hosting`.

> `assetlinks.json` se completará después de crear el certificado de firma de release y obtener su SHA-256.

---

# CineTrack — Firebase Hosting

Proyecto Firebase: `cinetrack-9df50`

Hosting publica las páginas estáticas necesarias para CineTrack:

- `/` — página base y fallback de enlaces compartidos;
- `/privacy` — política de privacidad;
- `/support` — soporte;
- `/delete-account` — instrucciones de eliminación de cuenta.

## Opción recomendada en Windows

1. Instala Node.js LTS si todavía no lo tienes.
2. Abre PowerShell.
3. Instala Firebase CLI:

```powershell
npm install -g firebase-tools
```

4. Desde la raíz del proyecto ejecuta:

```powershell
.\scripts\deploy-hosting.ps1
```

El script selecciona `cinetrack-9df50`, solicita `firebase login` si hace falta y ejecuta:

```text
firebase.cmd deploy --only hosting --project cinetrack-9df50 --project cinetrack-9df50
```

Después del deploy deben existir:

- https://cinetrack-9df50.web.app
- https://cinetrack-9df50.web.app/privacy
- https://cinetrack-9df50.web.app/support
- https://cinetrack-9df50.web.app/delete-account

## App Links

La verificación completa de Android App Links requiere publicar `/.well-known/assetlinks.json` con el SHA-256 del certificado de firma final. Eso se hace después de crear el keystore de release; no bloquea la publicación de las páginas anteriores.


### Windows con ExecutionPolicy restringida
Ejecuta `scripts\deploy-hosting.cmd`. No requiere cambiar la política de PowerShell.
