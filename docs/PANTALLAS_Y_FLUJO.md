# CineTrack — Pantallas y flujo de usuario 0.8

La navegación principal queda alineada con las capturas de Figma: **Inicio**, **Buscar**, **Mi lista** y **Perfil**. Las funciones sociales permanecen como rutas secundarias y enlaces compartibles.

## Pantallas principales

| # | Pantalla | Objetivo | Acciones principales |
|---|---|---|---|
| 01 | Splash | Verificar sesión Firebase | Ir a Login o Inicio |
| 02 | Login | Acceder con Firebase Authentication | Login, recuperar contraseña, Registro |
| 03 | Registro | Crear cuenta Firebase | Nombre, correo, contraseña, términos |
| 04 | Recuperar contraseña | Restablecer acceso | Enviar instrucciones por correo |
| 05 | Inicio | Descubrir contenido | Tendencias, filtros rápidos, catálogo, buscar |
| 06 | Buscar | Encontrar películas/series | Texto, tipo, género, resultados |
| 07 | Detalle | Información completa | Guardar, compartir, estado, favorito, reparto, reseña, relacionados |
| 08 | Mi lista | Organizar guardados persistentes con Room | Filtrar, estado, favorito, eliminar |
| 09 | Mi registro | Registro personal | Estado, estrellas, comentario, review pública opcional |
| 10 | Perfil | Identidad y estadísticas | Editar, compartir, Configuración, Créditos, Ayuda |
| 11 | Configuración | Cuenta y apariencia | Editar perfil, cambiar contraseña, tema, privacidad, acerca de |
| 12 | Créditos | Información académica/técnica | Equipo, versión, stack, TMDB |
| 13 | Eliminar cuenta | Privacidad y cumplimiento Play | Borrar perfil/reseñas públicas, Auth y datos Room |

## Vistas secundarias

| Vista | Uso |
|---|---|
| Editar perfil | Nombre, avatar/foto, género y hasta 5 películas favoritas |
| Perfil público | Vista compartible sin correo; favoritas y reseñas públicas |
| Reseña compartida | Review pública propia abierta desde un enlace |
| Relacionadas | Continuación de Detalle por género, director/creador, actor o afinidad |

## Flujo resumido

```text
Inicio de la app
  ↓
Splash
  ├─ sin sesión → Login ↔ Registro
  │                ├─ Recuperar contraseña
  │                └─ acceso correcto
  └─ con sesión ──────────────────┐
                                  ↓
         Inicio ↔ Buscar ↔ Mi lista ↔ Perfil
           │        │         │        ├─ Editar perfil
           └────────┴─────────┴→ Detalle├─ Configuración
                              │         │   └─ Eliminar cuenta → Login
                              │         ├─ Créditos
                              │         └─ Cerrar sesión
                              ├─ reparto
                              ├─ relacionados → otro Detalle
                              ├─ compartir película/serie
                              └─ Mi registro
                                  ├─ guardar privado en Room
                                  └─ reseña pública → compartir enlace

Enlaces externos
  ├─ /profile/{uid} → Perfil público
  ├─ /media/{type}/{tmdbId} → Login si hace falta → Detalle
  ├─ /review/{reviewId} → Reseña compartida
  ├─ /privacy → Política de privacidad
  └─ /delete-account → Recurso externo de eliminación
```

## Arquitectura para la presentación

```text
Jetpack Compose UI
       ↓
    ViewModel
       ↓
   Repository
   ├─ TMDB → catálogo, búsqueda, detalle, reparto, relacionados
   ├─ Room → Mi lista, estados, favoritos, ratings/comentarios privados
   ├─ DataStore → tema/preferencias locales
   └─ Firebase → Auth + perfil/reviews públicas opcionales
```

## Referencia visual

Las capturas recibidas están congeladas en `docs/figma-reference/`. Las decisiones y diferencias se documentan en `FIGMA_UI_AUDIT.md`.
