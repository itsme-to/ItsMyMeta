package ru.oftendev.itsmymeta.meta.enum

import com.willfp.eco.core.data.keys.PersistentDataKeyType

enum class StatType(val id: String, val type: PersistentDataKeyType<out Any>) {
    STRING("string", PersistentDataKeyType.INT),
    INTEGER("integer", PersistentDataKeyType.STRING),
    DOUBLE("double", PersistentDataKeyType.DOUBLE);

    companion object {
        @JvmStatic
        fun getById(id: String): StatType? {
            return values().firstOrNull { it.id.equals(id, true) }
        }
    }
}