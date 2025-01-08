package com.exner.tools.activitytimercompanion.data.di

import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolder
import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TVConnectionStateHolderModule {

    @Binds
    abstract fun bindTVConnectionStateHolder(
        tvConnectionStateHolderImpl: TVConnectionStateHolderImpl
    ) : TVConnectionStateHolder
}