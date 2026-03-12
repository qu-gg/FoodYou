package com.maksimowiczm.foodyou.weighttracker.infrastructure

import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightEntry
import com.maksimowiczm.foodyou.weighttracker.domain.repository.WeightRepository
import com.maksimowiczm.foodyou.weighttracker.infrastructure.room.WeightEntryDao
import com.maksimowiczm.foodyou.weighttracker.infrastructure.room.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

internal class RoomWeightRepository(private val dao: WeightEntryDao) : WeightRepository {

    override fun observeEntries(from: LocalDate, to: LocalDate): Flow<List<WeightEntry>> =
        dao.observeEntries(from.toEpochDays(), to.toEpochDays()).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeAllEntries(): Flow<List<WeightEntry>> =
        dao.observeAllEntries().map { entities -> entities.map { it.toDomain() } }

    override fun observeLatestEntry(): Flow<WeightEntry?> =
        dao.observeLatestEntry().map { it?.toDomain() }

    override fun observeEntry(date: LocalDate): Flow<WeightEntry?> =
        dao.observeEntryByDate(date.toEpochDays()).map { it?.toDomain() }

    override suspend fun upsertEntry(entry: WeightEntry) {
        dao.upsert(
            WeightEntryEntity(
                dateEpochDay = entry.date.toEpochDays(),
                weightKg = entry.weightKg,
            )
        )
    }

    override suspend fun deleteEntry(date: LocalDate) {
        dao.deleteByDate(date.toEpochDays())
    }
}

private fun WeightEntryEntity.toDomain() =
    WeightEntry(
        date = LocalDate.fromEpochDays(dateEpochDay),
        weightKg = weightKg,
    )
