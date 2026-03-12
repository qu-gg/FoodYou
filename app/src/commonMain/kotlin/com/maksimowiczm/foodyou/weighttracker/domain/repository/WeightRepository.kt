package com.maksimowiczm.foodyou.weighttracker.domain.repository

import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface WeightRepository {
    fun observeEntries(from: LocalDate, to: LocalDate): Flow<List<WeightEntry>>

    fun observeAllEntries(): Flow<List<WeightEntry>>

    fun observeLatestEntry(): Flow<WeightEntry?>

    fun observeEntry(date: LocalDate): Flow<WeightEntry?>

    suspend fun upsertEntry(entry: WeightEntry)

    suspend fun deleteEntry(date: LocalDate)
}
