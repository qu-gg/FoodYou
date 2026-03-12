package com.maksimowiczm.foodyou.weighttracker.infrastructure.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WeightEntryDao {

    @Upsert
    abstract suspend fun upsert(entry: WeightEntryEntity)

    @Query("DELETE FROM WeightEntry WHERE dateEpochDay = :dateEpochDay")
    abstract suspend fun deleteByDate(dateEpochDay: Long)

    @Query(
        """
        SELECT * FROM WeightEntry
        WHERE dateEpochDay >= :fromEpochDay AND dateEpochDay <= :toEpochDay
        ORDER BY dateEpochDay ASC
        """
    )
    abstract fun observeEntries(fromEpochDay: Long, toEpochDay: Long): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM WeightEntry ORDER BY dateEpochDay ASC")
    abstract fun observeAllEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM WeightEntry ORDER BY dateEpochDay DESC LIMIT 1")
    abstract fun observeLatestEntry(): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM WeightEntry WHERE dateEpochDay = :epochDay LIMIT 1")
    abstract fun observeEntryByDate(epochDay: Long): Flow<WeightEntryEntity?>
}
