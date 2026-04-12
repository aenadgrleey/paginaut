package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.backwardLoading(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (backward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.BACKWARD_LOADING, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.backwardError(
    content: @Composable LazyStaggeredGridItemScope.(Throwable) -> Unit,
) {
    val error = backward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.BACKWARD_ERROR, span = StaggeredGridItemSpan.FullLine) { content(error.cause) }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.backwardEndReached(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (backward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.BACKWARD_END_REACHED, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}
