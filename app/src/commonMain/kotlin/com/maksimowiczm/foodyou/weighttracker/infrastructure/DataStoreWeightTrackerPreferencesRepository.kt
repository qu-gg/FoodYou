package com.maksimowiczm.foodyou.weighttracker.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightTrackerPreferences
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit

internal class DataStoreWeightTrackerPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<WeightTrackerPreferences>(dataStore) {

    override fun Preferences.toUserPreferences(): WeightTrackerPreferences =
        WeightTrackerPreferences(
            weightUnit = this[Keys.weightUnit]
                ?.let { runCatching { WeightUnit.entries[it] }.getOrNull() }
                ?: WeightTrackerPreferences.default.weightUnit,
            goalWeightKg = this[Keys.goalWeightKg],
        )

    override fun MutablePreferences.applyUserPreferences(updated: WeightTrackerPreferences) {
        this[Keys.weightUnit] = updated.weightUnit.ordinal
        when (val goal = updated.goalWeightKg) {
            null -> remove(Keys.goalWeightKg)
            else -> this[Keys.goalWeightKg] = goal
        }
    }

    private object Keys {
        val weightUnit = intPreferencesKey("weight_tracker:unit")
        val goalWeightKg = doublePreferencesKey("weight_tracker:goal_weight_kg")
    }
}
