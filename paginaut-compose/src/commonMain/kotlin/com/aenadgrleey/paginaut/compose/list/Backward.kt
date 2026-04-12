package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyListScope)
fun PaginationState<*>.backwardLoading(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (backward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.BACKWARD_LOADING) { content() }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.backwardError(
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = backward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.BACKWARD_ERROR) { content(error.cause) }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.backwardEndReached(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (backward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.BACKWARD_END_REACHED) { content() }
    }
}
