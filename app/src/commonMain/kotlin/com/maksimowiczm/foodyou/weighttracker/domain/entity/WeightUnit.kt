package com.maksimowiczm.foodyou.weighttracker.domain.entity

enum class WeightUnit {
    Kilograms,
    Pounds;

    companion object {
        const val KG_TO_LBS = 2.20462

        fun Double.kgToLbs(): Double = this * KG_TO_LBS

        fun Double.lbsToKg(): Double = this / KG_TO_LBS
    }
}
