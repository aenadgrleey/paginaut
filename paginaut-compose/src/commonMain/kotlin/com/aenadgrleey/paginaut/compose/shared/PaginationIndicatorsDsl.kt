package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable

open class StateIndicatorScope {
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

class InitStateIndicatorScope : StateIndicatorScope() {
    internal var placeholderCount: Int = 0
    internal var placeholderContent: @Composable LazyItemScope.(Int) -> Unit = { _ -> }

    fun shouldShowPlaceholders(): Boolean = placeholderCount > 0

    fun placeholders(
        count: Int,
        content: @Composable LazyItemScope.(Int) -> Unit,
    ) {
        placeholderCount = count
        placeholderContent = content
    }
}

class PaginationIndicatorsScope internal constructor() {
    val initStateIndicator = InitStateIndicatorScope()
    val forwardStateIndicator = StateIndicatorScope()
    val backwardStateIndicator = StateIndicatorScope()

    fun initStateIndicator(block: InitStateIndicatorScope.() -> Unit) {
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
    val init: InitStateIndicatorScope,
    val forward: StateIndicatorScope,
    val backward: StateIndicatorScope,
)
