package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.initLoading(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_LOADING, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.initError(
    content: @Composable LazyStaggeredGridItemScope.(Throwable) -> Unit,
) {
    val error = init as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_ERROR, span = StaggeredGridItemSpan.FullLine) { content(error.cause) }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.initEmpty(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (items.isEmpty() && init is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.INIT_EMPTY, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}
