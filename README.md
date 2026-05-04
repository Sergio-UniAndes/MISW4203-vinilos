# MISW4203 — Vinilos

Aplicación Android del curso **MISW4203 — Ingeniería de Software para Aplicaciones Móviles** (Maestría en Ingeniería de Software, Uniandes). Construida con **Kotlin + Jetpack Compose**, **MVVM** y **Clean Architecture** modularizada por features.

> **Estado al cierre del Sprint 2:** 5 HU entregadas — HU01/HU02 (álbumes, _Visitor_), HU03/HU04 (artistas, _Visitor_), HU07 (crear álbum, _Collector_). Mejoras de desempeño: corrutinas + cache en memoria + persistencia local con Room (TTL 5 min). 112 tests automatizados en verde — 90 unitarias JVM + 14 E2E `:app` + 8 instrumentadas Compose `:feature-home`.

---

## Tabla de contenidos

1. [Pre-requisitos](#1-pre-requisitos)
2. [Clonar el repositorio](#2-clonar-el-repositorio)
3. [Levantar el backend (microservicio)](#3-levantar-el-backend-microservicio)
4. [Configurar la URL del backend en la app](#4-configurar-la-url-del-backend-en-la-app)
5. [Compilar y ejecutar la app](#5-compilar-y-ejecutar-la-app)
6. [Ejecutar las pruebas](#6-ejecutar-las-pruebas)
7. [Estructura del proyecto](#7-estructura-del-proyecto)
8. [Solución de problemas comunes](#8-solución-de-problemas-comunes)
9. [Documentación adicional](#9-documentación-adicional)

---

## 1. Pre-requisitos

Instala lo siguiente en tu máquina antes de continuar:

| Herramienta        | Versión                   | Notas                                                                                                                             |
| ------------------ | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **JDK**            | 21                        | Obligatorio. El proyecto compila con `sourceCompatibility = VERSION_21` y `jvmTarget = "21"`.                                     |
| **Android Studio** | Koala (2024.1) o superior | Trae JDK 21 embebido en `Android Studio/jbr`. Recomendado para abrir el proyecto.                                                 |
| **Android SDK**    | API 34 (`compileSdk`)     | El emulador objetivo es API 34 (Android 14). `minSdk` = 24 (Android 7.0).                                                         |
| **Git**            | Cualquiera reciente       | Para clonar el repo.                                                                                                              |
| **Node.js + npm**  | 18+                       | **Opcional.** Solo si vas a levantar el backend del curso localmente; por defecto la app apunta al backend desplegado en Railway. |

### Verificar la versión de Java

```bash
java -version
```

Debe reportar una versión `21.x`. Si tienes varios JDK, apunta `JAVA_HOME` a JDK 21:

**macOS / Linux:**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
# Linux: export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

**Windows (Git Bash):**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

> Android Studio trae JDK 21 dentro de `<Android Studio>/jbr`. Es la opción más cómoda en Windows.

---

## 2. Clonar el repositorio

```bash
git clone https://github.com/Sergio-UniAndes/MISW4203-vinilos.git
cd MISW4203-vinilos
```

> Si tienes acceso por SSH usa `git@github.com:Sergio-UniAndes/MISW4203-vinilos.git`.

---

## 3. Levantar el backend (microservicio)

La app consume un microservicio REST con los endpoints:

- `GET /albums`, `GET /albums/{id}`, `POST /albums` (HU01, HU02, HU07)
- `GET /musicians`, `GET /musicians/{id}` (HU03, HU04)

**Por defecto la app apunta al backend desplegado en Railway**, así que no necesitas levantar nada local para ejecutarla. La URL viene cableada en `core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/config/BackendConfig.kt`:

```kotlin
object BackendConfig {
    const val BASE_URL: String = "https://back-vynils-production.up.railway.app/"
}
```

### (Opcional) Levantar el backend localmente

Si quieres correr el microservicio en tu equipo (por ejemplo, sin internet o para depurar payloads), el curso provee un Express que escucha en el puerto `3000`:

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils.git
cd BackVynils
npm install
npm start
```

Verifica que responde:

```bash
curl http://localhost:3000/albums | head -c 200
```

Luego ajusta `BackendConfig.BASE_URL` según el paso 4.

---

## 4. Configurar la URL del backend en la app

La URL base es una única constante en `core:utils`:

**`core/utils/src/main/kotlin/com/misw4203/vinilos/core/utils/config/BackendConfig.kt`**

```kotlin
object BackendConfig {
    const val BASE_URL: String = "https://back-vynils-production.up.railway.app/"
}
```

`HomeRepositoryProvider.kt` y `ArtistsRepositoryProvider.kt` la consumen como default; cambiar este valor afecta a toda la app.

Casos típicos:

| Entorno                                   | URL a usar                                       |
| ----------------------------------------- | ------------------------------------------------ |
| **Producción (default)**                  | `https://back-vynils-production.up.railway.app/` |
| **Emulador Android + backend local**      | `http://10.0.2.2:3000/`                          |
| **Dispositivo físico + backend en tu PC** | `http://<IP-LAN-de-tu-PC>:3000/`                 |

> `10.0.2.2` es la dirección que el emulador de Android usa para alcanzar `localhost` del host. No funciona desde un dispositivo físico.

> El `AndroidManifest.xml` ya tiene `android:usesCleartextTraffic="true"` para permitir HTTP simple en desarrollo (backend local). Para producción se usa HTTPS.

---

## 5. Compilar y ejecutar la app

### Opción A — Android Studio (recomendado)

1. Abre Android Studio → **Open** → selecciona la carpeta `MISW4203-vinilos`.
2. Espera a que Gradle sincronice (la primera vez baja dependencias, 5–10 min).
3. Configura Gradle JDK:
   **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** → `jbr-21` (embedded) o tu JDK 21.
4. Crea/abre un emulador desde **Tools → Device Manager** (Pixel + API 34 recomendado).
5. Asegúrate de que el backend del paso 3 esté corriendo.
6. Pulsa **Run ▶** con el módulo `app` seleccionado.

### Opción B — Línea de comandos (Gradle Wrapper)

Desde la raíz del repositorio:

```bash
# Compilar APK debug
./gradlew assembleDebug

# Instalar en un emulador o dispositivo conectado (verifica con `adb devices`)
./gradlew installDebug

# Compilar e instalar en un solo paso
./gradlew installDebug && adb shell am start -n com.misw4203.vinilos/.app.MainActivity
```

> En Windows usa `gradlew.bat` en CMD o `./gradlew` desde Git Bash / PowerShell.

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 6. Ejecutar las pruebas

El proyecto tiene **112 pruebas automatizadas**: 90 unitarias JVM (rápidas, sin emulador) + 22 instrumentadas (Espresso + Compose UI Test, requieren emulador o dispositivo) repartidas en 14 E2E `:app` y 8 de pantallas Compose `:feature-home`.

### Pruebas unitarias JVM (rápidas, sin emulador)

#### Opción A — Android Studio (interfaz gráfica)

**Ejecutar todas las pruebas de un módulo:**

1. En la vista **Project** (panel izquierdo), cambia el filtro a **Project** o **Android** para ver la carpeta `src/test`.
2. Click derecho sobre la carpeta `feature-home/src/test/kotlin` (o el módulo que quieras) → **Run 'Tests in ...'**.
3. Los resultados aparecen en la pestaña **Run** abajo, con el árbol de paquetes/clases/métodos en verde o rojo.

**Ejecutar una clase de prueba específica:**

1. Abre la clase, p. ej. `HomeViewModelTest.kt`.
2. Click en el icono ▶️ verde junto al nombre de la clase (en el _gutter_ a la izquierda) → **Run 'HomeViewModelTest'**.

**Ejecutar un solo test (`@Test`):**

1. Click en el icono ▶️ verde junto al nombre del método.
2. Útil para iterar rápido cuando estás depurando un caso específico. También puedes usar **Debug** ▶️🐞 para poner breakpoints.

#### Opción B — Línea de comandos (Gradle Wrapper)

```bash
# Todas
./gradlew test

# Solo un módulo
./gradlew :feature-home:testDebugUnitTest
./gradlew :feature-auth:testDebugUnitTest
./gradlew :core:utils:test
./gradlew :app:testDebugUnitTest

# Una clase específica
./gradlew :feature-home:testDebugUnitTest --tests "*HomeViewModelTest"

# Un solo método
./gradlew :feature-home:testDebugUnitTest --tests "*HomeViewModelTest.onFilterSelected_appliesGenreFilter"
```

Reportes HTML en `<módulo>/build/reports/tests/testDebugUnitTest/index.html` (en `:core:utils` la tarea es `test`, sin variant suffix).

Cobertura actual: **90 tests JVM** — ViewModels (HU01/HU02/HU03/HU04), `CreateAlbumUseCase` (HU07), mappers, repositorios con fakes, `HttpHomeService` y `HttpArtistsService` con `MockWebServer`, cache local Room (`RoomAlbumsLocalCacheTest`/`RoomArtistsLocalCacheTest` corren con Robolectric, sin emulador), permisos y sesión.

### Pruebas E2E instrumentadas (requieren emulador o dispositivo)

> **Pre-requisito**: tener un emulador corriendo o un dispositivo físico conectado con depuración USB activa. Verifica con `adb devices` que aparezca al menos uno.

#### Opción A — Android Studio (interfaz gráfica)

1. Abre el dropdown del **Device Manager** (esquina superior derecha o a veces en alguna de las toolbars) y selecciona el emulador/dispositivo donde correr las pruebas. Si no hay ninguno, créalo desde **Tools → Device Manager → Create Device**.
2. En la vista **Project**, navega a `app/src/androidTest/kotlin/com/misw4203/vinilos/app`.
3. Click derecho sobre la carpeta → **Run 'Tests in ...'**, o sobre una clase específica como `AlbumDetailNavigationEspressoTest`.
4. Android Studio compila el APK de tests, lo instala en el dispositivo y muestra el progreso en tiempo real en la pestaña **Run**. Verás el emulador ejecutando los gestos automáticos.
5. Como con los unitarios, también puedes correr una clase o un solo `@Test` desde los iconos ▶️ del _gutter_.

> Los tests instrumentados están repartidos en dos módulos:
>
> - **`:app/src/androidTest`** — flujos completos de la app (necesitan `MainActivity` y el `NavHost` real). Sprint 1: `AuthHomeFlowTest`, `AuthHomeFlowEspressoTest`, `CollectorPermissionsEspressoTest`, `AlbumDetailNavigationEspressoTest`. Sprint 2: `ArtistsEspressoTest` (HU03), `ArtistDetailNavigationEspressoTest` (HU04), `CreateAlbumNavigationEspressoTest` (HU07).
> - **`:feature-home/src/androidTest`** — tests de pantalla Compose con repositorios fake (no necesitan backend ni navegación). Sprint 2: `ArtistsScreenTest` (HU03), `ArtistDetailScreenTest` (HU04).

#### Opción B — Línea de comandos

```bash
# Verifica que haya un dispositivo conectado
adb devices

# Ejecuta las 14 E2E :app + las 8 instrumentadas Compose :feature-home (22 totales)
./gradlew :app:connectedDebugAndroidTest :feature-home:connectedDebugAndroidTest

# Solo un módulo
./gradlew :app:connectedDebugAndroidTest
./gradlew :feature-home:connectedDebugAndroidTest

# Una clase específica
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.misw4203.vinilos.app.AlbumDetailNavigationEspressoTest
```

Reportes en `app/build/reports/androidTests/connected/debug/index.html` y `feature-home/build/reports/androidTests/connected/debug/index.html`.

Cobertura actual: **14 tests E2E `:app`** (Auth, Home, navegación a detalle de álbum/artista, formulario _Create Album_, visibilidad por rol) + **8 tests Compose `:feature-home`** (`ArtistsScreen` y `ArtistDetailScreen`).

#### Helper: capturas de evidencia para la wiki

Para juntar los reportes HTML en un solo lugar y abrirlos automáticamente en el navegador (útil para tomar screenshots de evidencia), corre desde PowerShell:

```powershell
.\scripts\collect-test-reports.ps1
```

Copia los 6 reportes (4 JVM + 2 instrumentados) a `screenshots-input/` (carpeta gitignored) con los mismos nombres que la wiki espera para los PNG.

> **Tip Windows (CLI):** si Gradle no encuentra JDK 21 con error `Cannot find a Java installation … languageVersion=21`, exporta `JAVA_HOME` apuntando al JBR de Android Studio antes de invocar el wrapper. **Desde Android Studio no aplica** porque ya usa su JBR embebido.
>
> **PowerShell:**
>
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
> $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
> ./gradlew test
> ```
>
> **Git Bash:**
>
> ```bash
> export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
> ./gradlew test
> ```

---

## 7. Estructura del proyecto

```text
MISW4203-vinilos/
├── app/                       # Entry point, MainActivity, NavHost, AppContainer (DI manual con Context)
├── core/
│   ├── ui/                    # Tema + componentes Compose reutilizables
│   ├── navigation/            # Rutas compartidas (AppRoute)
│   └── utils/                 # Sesión (in-memory), permisos, modelos compartidos, BackendConfig
├── feature-auth/              # Selección de rol (Visitor / Collector)
├── feature-home/              # HU01 catálogo + HU02 detalle de álbum + HU03 listado de artistas + HU04 detalle de artista + HU07 crear álbum
│   ├── ui/                    # Composables + ViewModels (Home/AlbumDetail/Artists/ArtistDetail/CreateAlbum)
│   ├── domain/                # Repositorios «interface», UseCases (Observe…/CreateAlbum/UploadCover), modelos
│   └── data/
│       ├── repository/        # RemoteHomeRepository, RemoteArtistsRepository + Providers (DI)
│       ├── mapper/            # AlbumMapper, ArtistMapper (DTO → dominio)
│       ├── remote/            # HomeService / ArtistsService «Service Adapter» + Http…Service + JsonExtensions
│       │   └── dto/           # AlbumDto, MusicianDto, TrackDto, PerformerDto, CommentDto
│       └── cache/             # VinilosDatabase (Room), AlbumDao/ArtistDao, RoomAlbums/ArtistsLocalCache (TTL 5 min)
└── scripts/                   # Helpers PowerShell (collect-test-reports.ps1)
```

**Patrones aplicados:**

- **MVVM + UDF**: ViewModel expone `StateFlow<UiState>` + `SharedFlow<UiEffect>`.
- **Clean Architecture por feature**: `ui → domain ← data`.
- **Service Adapter**: cada repositorio remoto delega en una interfaz de transporte (`HomeService`, `ArtistsService`). Las implementaciones HTTP usan `HttpURLConnection` + `org.json`. Cambiar a Retrofit/Ktor toca solo las clases `Http…Service`.
- **Cache en dos niveles** (Sprint 2): `MutableStateFlow<List<…>?>` en memoria como render instantáneo + Room (`VinilosDatabase`) como persistencia entre sesiones, con TTL de 5 minutos. Detrás de `AlbumsLocalCache` / `ArtistsLocalCache` (interfaces) — los repositorios no conocen Room directamente, lo que permite inyectar `NoopAlbumsLocalCache` en tests JVM puros.
- **DI manual** vía `AppContainer(context)` (sin Hilt/Koin). Recibe el `applicationContext` desde `MainActivity` para construir Room.
- **Permisos centralizados** en `core:utils.PermissionsPolicy`; la UI nunca hardcodea reglas por rol.
- **Configuración del backend centralizada** en `core:utils.BackendConfig` — un único punto de cambio para toda la app.

### Roles soportados

| Rol         | Ver | Crear | Editar | Eliminar |
| ----------- | :-: | :---: | :----: | :------: |
| `Visitor`   |  ✓  |       |        |          |
| `Collector` |  ✓  |   ✓   |   ✓    |    ✓     |

El rol vive solo en memoria y se limpia al cerrar la app (no hay autenticación persistente).

---

## 8. Solución de problemas comunes

### Gradle no encuentra JDK 21

```
Cannot find a Java installation on your machine matching: {languageVersion=21, …}
```

Apunta `JAVA_HOME` a un JDK 21 (o al JBR de Android Studio) y vuelve a ejecutar Gradle. Ver [§1](#1-pre-requisitos).

### Tests JVM fallan con "Method length in org.json.JSONArray not mocked"

Las stubs de Android no implementan `org.json` en unit tests. El proyecto ya incluye `testImplementation("org.json:json:20240303")` en `feature-home/build.gradle.kts` para resolverlo. Si replicas ese patrón en otro módulo nuevo, recuerda añadir esa dependencia.

### La app abre pero el catálogo está vacío

- Verifica conectividad al backend que estás usando: `curl https://back-vynils-production.up.railway.app/albums | head -c 200` (Railway, default) o `curl http://localhost:3000/albums` (local).
- Si usas backend local con **emulador**, `BackendConfig.BASE_URL` debe ser `http://10.0.2.2:3000/`.
- Si usas backend local con **dispositivo físico**, cambia `BackendConfig.BASE_URL` en `core/utils/.../config/BackendConfig.kt` por la IP LAN de tu PC y abre el firewall en el puerto 3000.
- Si la app abrió antes con datos pero ahora se quedó "fría": Room mantiene el último snapshot por 5 minutos; pasado ese TTL, si la red sigue caída no hay datos para mostrar. Borra los datos de la app desde Settings o reinstala para resetear el cache.
- Revisa `Logcat` filtrando por `vinilos` para ver si hay errores HTTP.

### Error de tema en el manifest

```
resource style/Theme.Material3.DayNight.NoActionBar not found
```

El proyecto usa `@android:style/Theme.Material.Light.NoActionBar` para evitar dependencias adicionales. Si ves este error tras un sync, haz `./gradlew clean` y resincroniza.

### Clases duplicadas tras refactor (`is defined multiple times`)

```bash
./gradlew clean
./gradlew assembleDebug
```

### El emulador no responde a `installDebug`

```bash
adb kill-server
adb start-server
adb devices         # debe listar tu emulador/dispositivo
./gradlew installDebug
```

---

## 9. Documentación adicional

- **Wiki del proyecto** (en GitHub): planes de sprint (`Sprint-1`, `Sprint-2`), `Estrategia-de-Pruebas`, retrospectivas y evidencia de ejecución de tests por sprint.
- **Diagramas UML** en la raíz del repo (editables con [draw.io](https://app.diagrams.net/)):
  - `package_diagram.drawio` / `.md` — paquetes y dependencias entre módulos.
  - `component_diagram.drawio` / `.md` — componentes en tiempo de ejecución.
  - `class_diagram_feature_home.drawio` / `.md` — clases de `feature-home` (HU01–HU04 + HU07 + Service Adapter + Cache Room).
  - `class_diagram_feature_auth.drawio` / `.md` — clases de `feature-auth`.
- **`DESING.md`** y **`GUIA_CODIGO.md`** — decisiones arquitectónicas detalladas y guía de estilo.

---

## Versiones del stack

| Componente                     | Versión       |
| ------------------------------ | ------------- |
| Android Gradle Plugin          | 8.5.2         |
| Kotlin                         | 1.9.24        |
| KSP                            | 1.9.24-1.0.20 |
| Jetpack Compose BOM            | 2024.06.00    |
| Compose Compiler               | 1.5.14        |
| Room                           | 2.6.1         |
| Coroutines                     | 1.8.1         |
| Coil                           | 2.7.0         |
| desugar_jdk_libs               | 2.1.2         |
| Robolectric (tests Room JVM)   | 4.10.3        |
| MockWebServer (tests HTTP JVM) | 4.12.0        |
| Gradle Wrapper                 | 9.0.0         |
| Java/Kotlin target             | 21            |
| `compileSdk`                   | 34            |
| `minSdk`                       | 24            |

> Las versiones están centralizadas en `gradle/libs.versions.toml` — para actualizar una dependencia, edita ahí y los módulos la consumen vía `libs.<alias>`.
