# Guia del codigo - MISW4203 vinilos

Esta guia explica el proyecto completo para alguien que viene de Java y esta empezando en Kotlin + Jetpack Compose.

## 1) Vista general del proyecto

El proyecto esta modularizado por responsabilidad:

- `app`: entrada de la app, navegacion principal y armado de dependencias.
- `core/ui`: tema y componentes reutilizables de Compose.
- `core/navigation`: rutas globales.
- `core/utils`: sesion, roles, permisos y casos de uso compartidos.
- `feature-auth`: seleccion de rol y guardado de sesion en memoria.
- `feature-home`: catalogo de albumes, filtros, tabs y acciones segun permisos.

## 2) Estructura de carpetas (que mirar y que ignorar)

Mira principalmente:

- `*/src/main/kotlin/**`: codigo Kotlin.
- `app/src/main/AndroidManifest.xml`: configuracion base Android.
- `*/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`: configuracion de modulos y dependencias.

Ignora para entender logica (se regeneran):

- `**/build/**`, `.gradle/`, `app/build/`, `feature-*/build/`, `core/*/build/`.

## 3) Flujo completo de la app (de inicio a pantalla principal)

1. Android inicia `MainActivity`.
2. `MainActivity` crea un `AppContainer` (inyeccion manual de dependencias).
3. `VinilosApp` monta el `NavHost` de Compose.
4. Ruta inicial: `bootstrap`.
5. `BootstrapViewModel` observa sesion:
   - si no hay sesion -> navega a `auth`
   - si hay sesion -> navega a `home`
6. En `auth` el usuario elige rol (`Visitor` o `Collector`).
7. `AuthViewModel` guarda rol en `SessionRepository`.
8. Se navega a `home`.
9. `HomeViewModel` combina:
   - sesion actual
   - items remotos de albumes
   - filtro seleccionado
   - tab seleccionada

## 4) Modulo `app` (arranque y orquestacion)

### `app/src/main/kotlin/com/misw4203/vinilos/app/MainActivity.kt`

- Entry point Android.
- Usa `setContent { ... }` para Compose.
- Crea `AppContainer` una sola vez con `lazy`.

### `app/src/main/kotlin/com/misw4203/vinilos/app/AppContainer.kt`

- Es un contenedor simple de dependencias (sin Hilt/Koin).
- Crea implementaciones concretas:
  - `DefaultPermissionsPolicy`
  - `InMemorySessionRepository`
  - `RemoteHomeRepository` (via `provideHomeRepository`)
- Crea use cases y `ViewModelProvider.Factory` para:
  - `BootstrapViewModel`
  - `AuthViewModel`
  - `HomeViewModel`

### `app/src/main/kotlin/com/misw4203/vinilos/app/VinilosApp.kt`

- Aplica `VinilosTheme`.
- Define el `NavHost` con rutas:
  - `AppRoute.Bootstrap`
  - `AppRoute.Auth`
  - `AppRoute.Home`
- Configura transiciones de navegacion con `popUpTo` para limpiar back stack.
- `BootstrapRoute` muestra spinner mientras decide ruta destino.

### `app/src/main/kotlin/com/misw4203/vinilos/app/BootstrapViewModel.kt`

- Observa `ObserveSessionUseCase`.
- Mapea estado de sesion a `targetRoute`.
- Expone `StateFlow<BootstrapUiState>`.

### `app/src/main/AndroidManifest.xml`

- Declara permiso `INTERNET`.
- Configura `MainActivity` como launcher.
- `usesCleartextTraffic=true` para permitir HTTP local en desarrollo.

### `app/src/main/res/values/themes.xml`

- Define tema `Theme.Vinilos` con tema base Android sin ActionBar.

## 5) Modulo `core/navigation`

### `core/navigation/src/main/kotlin/com/misw4203/vinilos/core/navigation/AppRoute.kt`

- Constantes de rutas de navegacion:
  - `Bootstrap`
  - `Auth`
  - `Home`

## 6) Modulo `core/utils` (reglas compartidas)

## Modelos

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/model/UserRole.kt`

- Enum de roles: `VISITOR`, `COLLECTOR`.
- Incluye propiedades derivadas para UI:
  - `displayName`
  - `description`

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/model/RolePermissions.kt`

- Estructura de permisos por rol:
  - `canView`, `canCreate`, `canEdit`, `canDelete`.

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/model/UserSession.kt`

- Representa sesion activa:
  - rol
  - permisos resueltos

## Permisos

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/permissions/PermissionsPolicy.kt`

- Interfaz para resolver permisos segun rol.

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/permissions/DefaultPermissionsPolicy.kt`

- Implementacion concreta.
- `VISITOR`: solo lectura.
- `COLLECTOR`: CRUD habilitado.

## Repositorio de sesion

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/repository/SessionRepository.kt`

- Contrato de sesion:
  - observar sesion
  - guardar rol
  - limpiar sesion

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/repository/InMemorySessionRepository.kt`

- Implementacion en memoria con `MutableStateFlow`.
- No persiste al cerrar app.
- Al guardar rol calcula permisos con `PermissionsPolicy`.

## Use cases

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/usecase/ObserveSessionUseCase.kt`

- Expone `Flow<UserSession?>`.

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/usecase/SaveSessionUseCase.kt`

- Guarda rol en repositorio.

### `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/usecase/ClearSessionUseCase.kt`

- Elimina sesion actual.

## 7) Modulo `core/ui` (componentes y tema)

## Tema

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/theme/Color.kt`

- Paleta de colores custom de la app.

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/theme/Type.kt`

- Tipografia Material3 personalizada.

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/theme/VinilosTheme.kt`

- Define `LightColors` y `DarkColors`.
- Aplica `MaterialTheme(colorScheme, typography)`.

## Componentes reutilizables

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/components/SelectableOptionCard.kt`

- Card seleccionable usada en Auth para elegir rol.
- Cambia estilo cuando `selected=true`.

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/components/VinilosChrome.kt`

- Elementos de layout/chrome:
  - `VinilosTopBar`
  - `VinilosFilterChip`
  - `VinilosBottomNavigationBar`
  - `VinilosBottomNavItem`

### `core/ui/src/main/kotlin/com/misw4203/vinilos/core/ui/components/VinylItemCard.kt`

- Card generica para item con botones `Edit`/`Delete` condicionales.

## 8) Modulo `feature-auth`

## Domain

### `feature-auth/src/main/kotlin/com/misw4203/vinilos/feature/auth/domain/SelectRoleUseCase.kt`

- Caso de uso para guardar rol en sesion.

## UI state/effects

### `feature-auth/src/main/kotlin/com/misw4203/vinilos/feature/auth/ui/AuthUiState.kt`

- Estado de pantalla auth:
  - `selectedRole`
  - `isSubmitting`
  - `canContinue` (derivado)

### `feature-auth/src/main/kotlin/com/misw4203/vinilos/feature/auth/ui/AuthUiEffect.kt`

- Efecto one-shot de navegacion:
  - `NavigateHome`

## ViewModel

### `feature-auth/src/main/kotlin/com/misw4203/vinilos/feature/auth/ui/AuthViewModel.kt`

- Maneja eventos de UI:
  - seleccionar rol
  - continuar
- Ejecuta `SelectRoleUseCase` en coroutine.
- Emite `AuthUiEffect.NavigateHome` al completar.

## Pantalla

### `feature-auth/src/main/kotlin/com/misw4203/vinilos/feature/auth/ui/AuthScreen.kt`

- Dibuja pantalla de seleccion de perfil.
- Escucha `effects` con `LaunchedEffect` para navegar.
- Reutiliza `SelectableOptionCard`.
- Incluye componentes privados visuales (`AuthBackdrop`, `VinilosHeroHeader`, `GradientActionButton`).

## 9) Modulo `feature-home`

## Domain

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/domain/model/HomeItem.kt`

- Modelo de album usado por UI.

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/domain/repository/HomeRepository.kt`

- Contrato para obtener items del home como `Flow<List<HomeItem>>`.

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/domain/usecase/ObserveHomeItemsUseCase.kt`

- Envuelve el repositorio para capa de dominio.

## Data

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/remote/dto/AlbumDto.kt`

- DTO flexible para parsear respuesta JSON de albums.

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/mapper/AlbumMapper.kt`

- Convierte `AlbumDto` -> `HomeItem`.
- Define valores por defecto cuando faltan campos.
- Extrae anio desde `releaseDate` con regex `\\d{4}`.

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/repository/HomeRepositoryProvider.kt`

- Factory simple para crear `HomeRepository`.
- Base URL default: `http://10.0.2.2:3000/` (emulador Android -> localhost host).

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/repository/RemoteHomeRepository.kt`

- Implementacion concreta que consume `GET /albums` con `HttpURLConnection`.
- Flujo de datos:
  1. Hace request HTTP.
  2. Si status no es 2xx devuelve lista vacia.
  3. Parsea JSON (`JSONArray`) a `AlbumDto`.
  4. Mapea DTOs a `HomeItem`.
- Manejo de errores: captura excepciones y retorna vacio.

## UI state/effects

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/ui/HomeUiState.kt`

- Estado de pantalla Home:
  - sesion
  - items
  - filtro seleccionado
  - tab seleccionada
- Computados:
  - `permissions`
  - `filteredItems`
  - `featuredItem`
  - `gridItems`
  - `totalCount`

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/ui/HomeUiEffect.kt`

- Efectos one-shot:
  - `ShowMessage`
  - `NavigateAuth`

## ViewModel

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/ui/HomeViewModel.kt`

- Combina flujos de sesion + items + estado UI local (filtro/tab).
- Emite `HomeUiState` con `combine(...).stateIn(...)`.
- Acciones:
  - seleccionar filtro/tab
  - editar/eliminar/crear (hoy solo muestran mensaje)
  - cambiar perfil (limpia sesion y navega a auth)

## Pantalla

### `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/ui/HomeScreen.kt`

- `Scaffold` con:
  - top bar
  - snackbar
  - bottom navigation
- Render por tab:
  - `ALBUMS`: feed editorial con grid.
  - `ARTISTS` y `COLLECTORS`: placeholder `ComingSoonSection`.
- Controla visibilidad de acciones segun permisos (`canEdit`, `canDelete`, `canCreate`).

## 10) Build y dependencias

### `settings.gradle.kts`

- Declara los modulos incluidos del proyecto.

### `build.gradle.kts` (raiz)

- Registra plugins Android/Kotlin con alias del catalogo de versiones.

### `gradle/libs.versions.toml`

- Version catalog centralizado (AGP, Kotlin, Compose, Lifecycle, Navigation, Coroutines).

### `app/build.gradle.kts`

- Configura aplicacion Android.
- Depende de todos los modulos funcionales.
- Define Java/Kotlin 21 y Compose.

### `core/ui/build.gradle.kts`

- Modulo Android library con Compose (componentes/tema).

### `core/navigation/build.gradle.kts`

- Modulo Kotlin/JVM puro (sin Android) para rutas.

### `core/utils/build.gradle.kts`

- Modulo Kotlin/JVM puro para sesion/permisos/usecases.

### `feature-auth/build.gradle.kts`

- Android library con Compose para autenticacion por rol.
- Depende de `core:ui` y `core:utils`.

### `feature-home/build.gradle.kts`

- Android library con Compose para home/catalogo.
- Depende de `core:ui` y `core:utils`.

### `gradle.properties`

- Ajustes globales Gradle/AndroidX y estilo Kotlin.

## 11) Arquitectura en una frase por capa

- `data`: sabe de API/JSON/infra.
- `domain`: sabe de reglas de negocio y contratos.
- `ui`: sabe de estado de pantalla y renderizado Compose.

## 12) Mapa de lectura recomendado (orden sugerido)

1. `settings.gradle.kts`
2. `app/src/main/kotlin/com/misw4203/vinilos/app/MainActivity.kt`
3. `app/src/main/kotlin/com/misw4203/vinilos/app/VinilosApp.kt`
4. `app/src/main/kotlin/com/misw4203/vinilos/app/AppContainer.kt`
5. `core/utils/**` (roles, permisos, sesion)
6. `feature-auth/ui/AuthViewModel.kt` -> `feature-auth/ui/AuthScreen.kt`
7. `feature-home/data/repository/RemoteHomeRepository.kt`
8. `feature-home/ui/HomeViewModel.kt` -> `feature-home/ui/HomeScreen.kt`

## 13) Como agregar una funcionalidad nueva (plantilla)

Ejemplo: "Detalle de album".

1. Crear modelo dominio en `feature-home/domain/model` (si aplica).
2. Agregar contrato en `feature-home/domain/repository`.
3. Implementar llamada HTTP en `feature-home/data/repository`.
4. Mapear DTOs en `feature-home/data/mapper`.
5. Crear use case en `feature-home/domain/usecase`.
6. Extender `HomeViewModel` con estado/eventos.
7. Crear composable de pantalla en `feature-home/ui`.
8. Agregar ruta en `core/navigation` y wiring en `VinilosApp.kt`.
9. Proteger acciones con `RolePermissions` si corresponde.

## 14) Glosario rapido Kotlin/Compose (pensando en Java)

- `data class`: POJO con `equals/hashCode/toString/copy` automaticos.
- `object`: singleton.
- `sealed interface`: jerarquia cerrada (util para efectos/estados).
- `Flow`: stream reactivo asincrono.
- `StateFlow`: flow con ultimo valor para UI.
- `viewModelScope.launch`: coroutine ligada al ciclo de vida del ViewModel.
- `@Composable`: funcion que pinta UI declarativa.
- `LaunchedEffect`: bloque coroutine para side effects en Compose.

## 15) Limitaciones actuales detectables en codigo

- Sesion solo en memoria (se pierde al cerrar app).
- `RemoteHomeRepository` no expone errores detallados (retorna vacio).
- Tabs `Artists` y `Collectors` aun en "Coming soon".
- Acciones crear/editar/eliminar actualmente muestran mensajes y no llamadas reales.

---

Si quieres, la siguiente iteracion puede ser una `GUIA_PASO_A_PASO_HU02.md` para implementar "detalle de album" con cambios exactos archivo por archivo.

