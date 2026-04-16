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

## Notas de implementación

- No se usa XML.
- No existe autenticación tradicional ni consumo de backend.
- La solución está preparada para reemplazar los repositorios mock por implementaciones reales en el futuro.

## Siguiente paso recomendado

Crear un grafo de navegación más completo, separar interfaces de repositorios por feature y añadir tests unitarios para:

- `DefaultPermissionsPolicy`
- `AuthViewModel`
- `HomeViewModel`


