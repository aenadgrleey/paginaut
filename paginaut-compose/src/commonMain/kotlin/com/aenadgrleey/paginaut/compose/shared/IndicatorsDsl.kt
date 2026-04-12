package com.aenadgrleey.paginaut.compose.shared

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

class InitStateIndicatorScope internal constructor() : StateIndicatorScope() {
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

class IndicatorsScope internal constructor() {
    internal val init = InitStateIndicatorScope()
    internal val forward = StateIndicatorScope()
    internal val backward = StateIndicatorScope()

    fun initStateIndicator(block: InitStateIndicatorScope.() -> Unit) {
        init.apply(block)
    }

    fun forwardStateIndicator(block: StateIndicatorScope.() -> Unit) {
        forward.apply(block)
    }

    fun backwardStateIndicator(block: StateIndicatorScope.() -> Unit) {
        backward.apply(block)
    }
}
