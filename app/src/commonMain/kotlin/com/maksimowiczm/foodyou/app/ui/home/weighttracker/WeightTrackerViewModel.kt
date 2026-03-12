package com.maksimowiczm.foodyou.app.ui.home.weighttracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightEntry
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightTrackerPreferences
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit.Companion.kgToLbs
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit.Companion.lbsToKg
import com.maksimowiczm.foodyou.weighttracker.domain.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

enum class ChartRange(val days: Int?) {
    Week(7),
    Month(30),
    ThreeMonths(90),
    AllTime(null);
}

internal class WeightTrackerViewModel(
    private val weightRepository: WeightRepository,
    private val preferencesRepository: UserPreferencesRepository<WeightTrackerPreferences>,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private val _chartRange = MutableStateFlow(ChartRange.Month)
    val chartRange: StateFlow<ChartRange> = _chartRange

    private val _preferences = preferencesRepository.observe()
    val preferences: StateFlow<WeightTrackerPreferences> =
        _preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _preferences.first() },
        )

    val entries: StateFlow<List<WeightEntry>> =
        _chartRange
            .flatMapLatest { range ->
                val days = range.days
                if (days != null) {
                    val today = LocalDate.now()
                    val from = today.minus(days, DateTimeUnit.DAY)
                    weightRepository.observeEntries(from, today)
                } else {
                    weightRepository.observeAllEntries()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = emptyList(),
            )

    val latestEntry: StateFlow<WeightEntry?> =
        weightRepository.observeLatestEntry()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    /** Entry for the currently selected date (reactive). */
    val selectedDateEntry: StateFlow<WeightEntry?> =
        _selectedDate
            .filterNotNull()
            .flatMapLatest { date -> weightRepository.observeEntry(date) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun setChartRange(range: ChartRange) {
        _chartRange.value = range
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { preferencesRepository.update { copy(weightUnit = unit) } }
    }

    fun setGoalWeight(weightInDisplayUnit: Double?) {
        viewModelScope.launch {
            preferencesRepository.update {
                val kg = when {
                    weightInDisplayUnit == null -> null
                    weightUnit == WeightUnit.Pounds -> weightInDisplayUnit.lbsToKg()
                    else -> weightInDisplayUnit
                }
                copy(goalWeightKg = kg)
            }
        }
    }

    /**
     * Record weight for the selected date. [weightInDisplayUnit] is in the current display unit.
     */
    fun recordWeight(weightInDisplayUnit: Double) {
        val date = _selectedDate.value ?: return
        viewModelScope.launch {
            val prefs = preferencesRepository.observe().first()
            val kg = when (prefs.weightUnit) {
                WeightUnit.Pounds -> weightInDisplayUnit.lbsToKg()
                WeightUnit.Kilograms -> weightInDisplayUnit
            }
            weightRepository.upsertEntry(
                WeightEntry(date = date, weightKg = kg)
            )
        }
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch { weightRepository.deleteEntry(date) }
    }
}
