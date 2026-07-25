package com.kabroedu.app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Convertisseur Room pour stocker LocalDate en SQLite (format ISO : "2026-07-24").
 * À ajouter dans votre @Database avec @TypeConverters(LocalDateConverter::class).
 */
class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
