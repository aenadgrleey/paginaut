package com.aenadgrleey.paginaut.compose.grid

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState
import com.aenadgrleey.paginaut.compose.shared.PagerKeys

context(scope: LazyGridScope)
fun PaginationState<*>.backwardLoading(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (backward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.BACKWARD_LOADING, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.backwardError(
    content: @Composable LazyGridItemScope.(Throwable) -> Unit,
) {
    val error = backward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.BACKWARD_ERROR, span = { GridItemSpan(maxLineSpan) }) { content(error.cause) }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.backwardEndReached(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (backward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.BACKWARD_END_REACHED, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}
