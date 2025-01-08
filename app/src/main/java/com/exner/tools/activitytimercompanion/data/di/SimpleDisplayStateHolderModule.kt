package com.exner.tools.activitytimercompanion.data.di

import com.exner.tools.activitytimercompanion.state.SimpleDisplayStateHolder
import com.exner.tools.activitytimercompanion.state.SimpleDisplayStateHolderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SimpleDisplayStateHolderModule {

    @Binds
    abstract fun bindSimpleDisplayStateHolder(
        simpleDisplayStateHolderImpl: SimpleDisplayStateHolderImpl
    ) : SimpleDisplayStateHolder
}