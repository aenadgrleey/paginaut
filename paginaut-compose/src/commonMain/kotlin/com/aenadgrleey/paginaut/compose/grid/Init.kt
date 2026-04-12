package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyGridScope)
fun PaginationState<*>.initLoading(
    viewport: GridViewportState? = null,
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_LOADING, span = { GridItemSpan(maxLineSpan) }) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.initError(
    viewport: GridViewportState? = null,
    content: @Composable LazyGridItemScope.(Throwable) -> Unit,
) {
    val error = init as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_ERROR, span = { GridItemSpan(maxLineSpan) }) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content(error.cause) }
            } else {
                content(error.cause)
            }
        }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.initEmpty(
    viewport: GridViewportState? = null,
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (items.isEmpty() && init is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.INIT_EMPTY, span = { GridItemSpan(maxLineSpan) }) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}
