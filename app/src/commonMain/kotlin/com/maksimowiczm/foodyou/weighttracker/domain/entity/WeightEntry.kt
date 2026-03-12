package com.maksimowiczm.foodyou.weighttracker.domain.entity

import kotlinx.datetime.LocalDate

data class WeightEntry(
    val date: LocalDate,
    /** Weight stored in kilograms. */
    val weightKg: Double,
)
