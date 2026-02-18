package com.englesoft.netspeedindicator.core.di

import com.englesoft.netspeedindicator.data.repository.SpeedRepositoryImpl
import com.englesoft.netspeedindicator.data.repository.UsageRepositoryImpl
import com.englesoft.netspeedindicator.domain.repository.SpeedRepository
import com.englesoft.netspeedindicator.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 * Binds repository interfaces to implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSpeedRepository(
        speedRepositoryImpl: SpeedRepositoryImpl
    ): SpeedRepository
    
    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        usageRepositoryImpl: UsageRepositoryImpl
    ): UsageRepository
}
