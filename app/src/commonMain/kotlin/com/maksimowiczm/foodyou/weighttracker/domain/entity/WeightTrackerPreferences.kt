package com.maksimowiczm.foodyou.weighttracker.domain.entity

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

data class WeightTrackerPreferences(
    val weightUnit: WeightUnit,
    val goalWeightKg: Double?,
) : UserPreferences {
    companion object {
        val default = WeightTrackerPreferences(
            weightUnit = WeightUnit.Kilograms,
            goalWeightKg = null,
        )
    }
}
