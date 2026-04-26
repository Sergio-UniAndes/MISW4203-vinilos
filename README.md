# MISW4203 — Vinilos

Aplicación Android del curso **MISW4203 — Ingeniería de Software para Aplicaciones Móviles** (Maestría en Ingeniería de Software, Uniandes). Construida con **Kotlin + Jetpack Compose**, **MVVM** y **Clean Architecture** modularizada por features.

> **Estado al cierre del Sprint 1:** HU01 (catálogo de álbumes) y HU02 (detalle de álbum) implementadas. 61 tests automatizados (10 E2E + 51 unitarias JVM) en verde.

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

| Herramienta        | Versión                   | Notas                                                                                         |
| ------------------ | ------------------------- | --------------------------------------------------------------------------------------------- |
| **JDK**            | 21                        | Obligatorio. El proyecto compila con `sourceCompatibility = VERSION_21` y `jvmTarget = "21"`. |
| **Android Studio** | Koala (2024.1) o superior | Trae JDK 21 embebido en `Android Studio/jbr`. Recomendado para abrir el proyecto.             |
| **Android SDK**    | API 34 (`compileSdk`)     | El emulador objetivo es API 34 (Android 14). `minSdk` = 24 (Android 7.0).                     |
| **Git**            | Cualquiera reciente       | Para clonar el repo.                                                                          |
| **Node.js + npm**  | 18+                       | Solo si vas a levantar el backend del curso localmente (es Express).                          |

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

La app consume un microservicio REST que expone `GET /albums` y `GET /albums/{id}`. El curso provee un backend en Express que escucha en el puerto `3000`.

Pasos (en otra terminal, fuera de este repo):

```bash
git clone https://github.com/MISW-4104-Web/vinyls-backend.git
cd vinyls-backend
npm install
npm start
```

Verifica que responde:

```bash
curl http://localhost:3000/albums | head -c 200
```

> Si tu equipo usa otro backend (otra rama del curso, un fork, o el que corre el profesor en la nube), reemplaza la URL en el paso 4.

---

## 4. Configurar la URL del backend en la app

La URL base se define en una sola constante:

**`feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/repository/HomeRepositoryProvider.kt`**

```kotlin
private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"
```

Casos típicos:

| Entorno                                   | URL a usar                       |
| ----------------------------------------- | -------------------------------- |
| **Emulador Android + backend local**      | `http://10.0.2.2:3000/`          |
| **Dispositivo físico + backend en tu PC** | `http://<IP-LAN-de-tu-PC>:3000/` |
| **Backend remoto (nube)**                 | `https://<host>/`                |

> `10.0.2.2` es la dirección que el emulador de Android usa para alcanzar `localhost` del host. No funciona desde un dispositivo físico.

> El `AndroidManifest.xml` ya tiene `android:usesCleartextTraffic="true"` para permitir HTTP simple en desarrollo. Para producción usar HTTPS.

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

### Pruebas unitarias JVM (rápidas, sin emulador)

```bash
# Todas
./gradlew test

# Solo un módulo
./gradlew :feature-home:testDebugUnitTest
./gradlew :feature-auth:testDebugUnitTest
./gradlew :core:utils:test
./gradlew :app:testDebugUnitTest
```

Reportes HTML en `<módulo>/build/reports/tests/testDebugUnitTest/index.html`.

Cobertura actual: **51 tests JVM** (ViewModels, mapper, repositorio con fake `HomeService`, `HttpHomeService` con `MockWebServer`, permisos, sesión).

### Pruebas E2E instrumentadas (requieren emulador o dispositivo)

```bash
# Verifica que haya un dispositivo conectado
adb devices

# Ejecuta los 10 tests E2E
./gradlew connectedDebugAndroidTest
```

Reporte en `app/build/reports/androidTests/connected/index.html`.

Cobertura actual: **10 tests E2E** (Bootstrap → Auth → Home, navegación HU02, visibilidad por rol).

> **Tip Windows:** si Gradle no encuentra JDK 21 con error `Cannot find a Java installation … languageVersion=21`, exporta `JAVA_HOME` apuntando al JBR de Android Studio antes de invocar el wrapper:
>
> ```bash
> export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
> ./gradlew test
> ```

---

## 7. Estructura del proyecto

```text
MISW4203-vinilos/
├── app/                       # Entry point, MainActivity, NavHost, AppContainer (DI manual)
├── core/
│   ├── ui/                    # Tema + componentes Compose reutilizables
│   ├── navigation/            # Rutas compartidas (AppRoute)
│   └── utils/                 # Sesión (in-memory), permisos, modelos compartidos
├── feature-auth/              # Selección de rol (Visitor / Collector)
└── feature-home/              # HU01 catálogo + HU02 detalle de álbum
    ├── ui/                    # Composables + ViewModels
    ├── domain/                # HomeRepository «interface», UseCases, modelos
    └── data/
        ├── repository/        # RemoteHomeRepository (impl) + AlbumMapper
        ├── mapper/            # DTO → dominio
        └── remote/            # HomeService «Service Adapter» + HttpHomeService
```

**Patrones aplicados:**

- **MVVM + UDF**: ViewModel expone `StateFlow<UiState>` + `SharedFlow<UiEffect>`.
- **Clean Architecture por feature**: `ui → domain ← data`.
- **Service Adapter**: `RemoteHomeRepository` no conoce HTTP; delega en `HomeService` (interfaz). `HttpHomeService` es la implementación con `HttpURLConnection` + `org.json`. Cambiar a Retrofit/Ktor toca solo una clase.
- **DI manual** vía `AppContainer` (sin Hilt/Koin).
- **Permisos centralizados** en `core:utils.PermissionsPolicy`; la UI nunca hardcodea reglas por rol.

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

- Verifica que el backend esté corriendo: `curl http://localhost:3000/albums`.
- Si usas **emulador**, la URL base debe ser `http://10.0.2.2:3000/`.
- Si usas **dispositivo físico**, cambia `DEFAULT_BASE_URL` en `HomeRepositoryProvider.kt` por la IP LAN de tu PC y abre el firewall en el puerto 3000.
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

- **Wiki del proyecto** (en GitHub): planes de sprint, estrategia de pruebas, retrospectivas.
- **Diagramas UML** en la raíz del repo (editables con [draw.io](https://app.diagrams.net/)):
  - `package_diagram.drawio` / `.md` — paquetes y dependencias entre módulos.
  - `component_diagram.drawio` / `.md` — componentes en tiempo de ejecución.
  - `class_diagram_feature_home.drawio` / `.md` — clases de `feature-home` (HU01 + HU02 + Service Adapter).
  - `class_diagram_feature_auth.drawio` / `.md` — clases de `feature-auth`.
- **`DESING.md`** y **`GUIA_CODIGO.md`** — decisiones arquitectónicas detalladas y guía de estilo.

---

## Versiones del stack

| Componente            | Versión    |
| --------------------- | ---------- |
| Android Gradle Plugin | 8.5.2      |
| Kotlin                | 1.9.24     |
| Jetpack Compose BOM   | 2024.06.00 |
| Compose Compiler      | 1.5.14     |
| Gradle Wrapper        | 9.0.0      |
| Java/Kotlin target    | 21         |
| `compileSdk`          | 34         |
| `minSdk`              | 24         |
