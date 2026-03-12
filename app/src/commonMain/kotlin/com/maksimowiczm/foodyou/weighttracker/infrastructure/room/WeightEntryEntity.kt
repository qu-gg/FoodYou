package com.maksimowiczm.foodyou.weighttracker.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WeightEntry")
data class WeightEntryEntity(
    /** Date stored as epoch day (Long). */
    @PrimaryKey val dateEpochDay: Long,
    /** Weight in kilograms. */
    val weightKg: Double,
)
