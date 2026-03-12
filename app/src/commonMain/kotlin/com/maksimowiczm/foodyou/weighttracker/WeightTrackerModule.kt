package com.maksimowiczm.foodyou.weighttracker

import com.maksimowiczm.foodyou.weighttracker.infrastructure.weightTrackerInfrastructureModule
import org.koin.dsl.module

val weightTrackerModule = module { weightTrackerInfrastructureModule() }
