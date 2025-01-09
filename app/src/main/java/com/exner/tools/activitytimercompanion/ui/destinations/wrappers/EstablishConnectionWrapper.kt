package com.exner.tools.activitytimercompanion.ui.destinations.wrappers

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.wrapper.DestinationWrapper

object EstablishConnectionWrapper : DestinationWrapper {
    @Composable
    override fun <T> @Composable DestinationScope<T>.Wrap(
        screenContent: @Composable (() -> Unit)
    ) {
        TODO("Not yet implemented")
    }
}