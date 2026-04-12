package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.forwardLoading(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.FORWARD_LOADING, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.forwardError(
    content: @Composable LazyStaggeredGridItemScope.(Throwable) -> Unit,
) {
    val error = forward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.FORWARD_ERROR, span = StaggeredGridItemSpan.FullLine) { content(error.cause) }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.forwardEndReached(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.FORWARD_END_REACHED, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}
