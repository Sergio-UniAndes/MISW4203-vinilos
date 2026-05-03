# Cómo correr la app en local

Guía corta para levantar y probar la app Android en desarrollo local.

## 1) Pre-requisitos
- Java 21
- Android Studio
- Android SDK API 34
- Node.js + npm 18+ si vas a levantar el backend

## 2) Backend local
La app consume el backend del curso en el puerto `3000`.

Si todavía no lo tienes levantado, en otra terminal ejecuta:

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

## 3) URL que usa la app
La app ya apunta por defecto a:

```text
http://10.0.2.2:3000/
```

- **Emulador Android:** no debes cambiar nada si el backend corre en tu máquina.
- **Dispositivo físico:** cambia la URL base a `http://<IP-LAN-DE-TU-PC>:3000/`.

La constante está en `feature-home/src/main/kotlin/com/misw4203/vinilos/feature/home/data/repository/HomeRepositoryProvider.kt`.

## 4) Abrir y ejecutar desde Android Studio
1. Abre la carpeta del proyecto en Android Studio.
2. Espera a que Gradle sincronice.
3. Selecciona un emulador o dispositivo.
4. Ejecuta el módulo `app`.

## 5) Comandos útiles
Desde la raíz del proyecto:

```bash
# Compilar debug
./gradlew assembleDebug

# Instalar en un dispositivo o emulador conectado
./gradlew installDebug

# Ejecutar pruebas unitarias de todos los módulos
./gradlew test

# Ejecutar pruebas unitarias de un módulo puntual
./gradlew :feature-home:testDebugUnitTest

# Ejecutar pruebas instrumentadas del módulo app
./gradlew :app:connectedDebugAndroidTest
```

## 6) Si Gradle no encuentra Java 21 en macOS

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```



