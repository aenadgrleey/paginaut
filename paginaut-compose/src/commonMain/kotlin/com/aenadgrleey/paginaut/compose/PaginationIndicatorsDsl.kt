package com.aenadgrleey.paginaut.compose

import androidx.compose.runtime.Composable

class StateIndicatorScope {
    internal var loading: @Composable () -> Unit = {}
    internal var error: @Composable (Throwable) -> Unit = {}
    internal var empty: @Composable () -> Unit = {}

    fun loading(content: @Composable () -> Unit) {
        loading = content
    }

    fun error(content: @Composable (Throwable) -> Unit) {
        error = content
    }

    fun empty(content: @Composable () -> Unit) {
        empty = content
    }
}

class PaginationIndicatorsScope internal constructor() {
    val initStateIndicator = StateIndicatorScope()
    val forwardStateIndicator = StateIndicatorScope()
    val backwardStateIndicator = StateIndicatorScope()

    fun initStateIndicator(block: StateIndicatorScope.() -> Unit) {
        initStateIndicator.apply(block)
    }

    fun forwardStateIndicator(block: StateIndicatorScope.() -> Unit) {
        forwardStateIndicator.apply(block)
    }

    fun backwardStateIndicator(block: StateIndicatorScope.() -> Unit) {
        backwardStateIndicator.apply(block)
    }

    internal fun build(): PaginationIndicators = PaginationIndicators(
        init = initStateIndicator,
        forward = forwardStateIndicator,
        backward = backwardStateIndicator,
    )
}

internal data class PaginationIndicators(
    val init: StateIndicatorScope,
    val forward: StateIndicatorScope,
    val backward: StateIndicatorScope,
)
