package com.ahn.ggriggri.di

import com.ahn.ggriggri.screen.ui.main.worker.AppWorkScheduler
import com.ahn.ggriggri.screen.ui.main.worker.WorkSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkSchedulerModule {

    @Binds
    abstract fun bindAppWorkScheduler(
        workSchedulerImpl: WorkSchedulerImpl
    ): AppWorkScheduler
}
