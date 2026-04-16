pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MISW4203-vinilos"
include(":app")
include(":core:ui")
include(":core:navigation")
include(":core:utils")
include(":feature-auth")
include(":feature-home")

