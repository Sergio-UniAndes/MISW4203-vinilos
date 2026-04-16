# MISW4203-vinilos

Aplicación Android en **Kotlin + Jetpack Compose** con **MVVM** y enfoque de **Clean Architecture**, modularizada por features.

## Estructura de módulos

```text
app
core/
  ui
  navigation
  utils
feature-auth
feature-home
```

## Capas por feature

Cada feature se organiza en:

```text
data
domain
ui
```

## Flujo funcional mínimo

1. `app` inicia con una pantalla bootstrap.
2. Se consulta la sesión local persistida.
3. Si no hay rol guardado, se navega a `feature-auth`.
4. El usuario selecciona un rol:
   - `Visitor`: solo lectura.
   - `Collector`: gestión completa.
5. Se persiste el rol en DataStore.
6. Al reabrir la app, si existe sesión, se va directo a `feature-home`.

## Control de permisos

La lógica de permisos está centralizada en `core:utils` mediante `PermissionsPolicy`.

- `Visitor`
  - ver contenido
  - no crear
  - no editar
  - no eliminar

- `Collector`
  - ver contenido
  - crear
  - editar
  - eliminar

La UI consume `RolePermissions` y no decide permisos por hardcode.

## Módulos y responsabilidades

- `app`: arranque, contenedor de dependencias y navegación principal.
- `core:ui`: componentes Compose reutilizables y tema.
- `core:navigation`: rutas comunes.
- `core:utils`: sesión, permisos, modelos compartidos y casos de uso base.
- `feature-auth`: selección de rol y persistencia de sesión.
- `feature-home`: pantalla principal con lista mock y acciones condicionadas por permisos.

## Requisitos de ejecución

- **JDK 21** instalado localmente.
- Android Studio configurado para usar **Gradle JDK 21**.
- Si ejecutas desde terminal, asegúrate de que `JAVA_HOME` apunte a JDK 21.
- El proyecto está unificado para compilar con **Java/Kotlin 21**.

### Verificar la versión de Java

```bash
java -version
```

Debe reportar una versión 21.x.

Si tienes más de un JDK instalado en macOS, puedes apuntar la terminal a Java 21 con:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

### Ejecutar el proyecto

Si tu entorno tiene Gradle instalado globalmente:

```bash
cd /Users/italonovoa/Documents/maestria/mobile/MISW4203-vinilos
gradle assembleDebug
```

Si no tienes Gradle global, abre el proyecto en Android Studio y ejecútalo desde el botón **Run** usando **Gradle JDK 21**.

En Android Studio:

1. Abre el proyecto.
2. Ve a **Settings > Build, Execution, Deployment > Build Tools > Gradle**.
3. Selecciona **Gradle JDK 21**.
4. Ejecuta la app desde `app` o presiona **Run**.

## Notas de implementación

- No se usa XML.
- No existe autenticación tradicional ni consumo de backend.
- La solución está preparada para reemplazar los repositorios mock por implementaciones reales en el futuro.

## Siguiente paso recomendado

Crear un grafo de navegación más completo, separar interfaces de repositorios por feature y añadir tests unitarios para:

- `DefaultPermissionsPolicy`
- `AuthViewModel`
- `HomeViewModel`


