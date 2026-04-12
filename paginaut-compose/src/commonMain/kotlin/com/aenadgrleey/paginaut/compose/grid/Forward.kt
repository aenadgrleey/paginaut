package com.aenadgrleey.paginaut.compose.grid

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState
import com.aenadgrleey.paginaut.compose.shared.PagerKeys

context(scope: LazyGridScope)
fun PaginationState<*>.forwardLoading(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.FORWARD_LOADING, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.forwardError(
    content: @Composable LazyGridItemScope.(Throwable) -> Unit,
) {
    val error = forward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.FORWARD_ERROR, span = { GridItemSpan(maxLineSpan) }) { content(error.cause) }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.forwardEndReached(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.FORWARD_END_REACHED, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}
