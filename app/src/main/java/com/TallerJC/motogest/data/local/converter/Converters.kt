package com.TallerJC.motogest.data.local.converter

import androidx.room.TypeConverter

class Converters {

    companion object {
        private const val SEPARATOR = "||"
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString(separator = SEPARATOR)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(SEPARATOR)
    }
}
