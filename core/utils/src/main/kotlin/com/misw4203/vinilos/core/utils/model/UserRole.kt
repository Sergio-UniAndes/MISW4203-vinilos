package com.misw4203.vinilos.core.utils.model

enum class UserRole {
    VISITOR,
    COLLECTOR;

    val displayName: String
        get() = when (this) {
            VISITOR -> "Visitor"
            COLLECTOR -> "Collector"
        }

    val description: String
        get() = when (this) {
            VISITOR -> "Solo lectura"
            COLLECTOR -> "Gestión completa"
        }
}

