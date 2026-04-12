package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyListScope)
fun PaginationState<*>.initLoading(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_LOADING) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.initError(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = init as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_ERROR) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content(error.cause) }
            } else {
                content(error.cause)
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.initEmpty(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (items.isEmpty() && init is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.INIT_EMPTY) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}
