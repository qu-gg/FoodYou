package com.maksimowiczm.foodyou.weighttracker.infrastructure

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepositoryOf
import com.maksimowiczm.foodyou.weighttracker.domain.repository.WeightRepository
import com.maksimowiczm.foodyou.weighttracker.infrastructure.room.WeightTrackerDatabase
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.bind

private val Scope.database: WeightTrackerDatabase
    get() = get<WeightTrackerDatabase>()

internal fun Module.weightTrackerInfrastructureModule() {
    userPreferencesRepositoryOf(::DataStoreWeightTrackerPreferencesRepository)
    factory { database.weightEntryDao }
    factory { RoomWeightRepository(get()) }.bind<WeightRepository>()
}
