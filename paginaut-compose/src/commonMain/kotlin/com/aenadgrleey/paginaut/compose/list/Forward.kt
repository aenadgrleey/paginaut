package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyListScope)
fun PaginationState<*>.forwardLoading(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (forward is LoadStatus.Loading) {
        scope.item(key = PagerKeys.FORWARD_LOADING) { content() }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.forwardError(
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = forward as? LoadStatus.Error
    if (error != null) {
        scope.item(key = PagerKeys.FORWARD_ERROR) { content(error.cause) }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.forwardEndReached(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.FORWARD_END_REACHED) { content() }
    }
}
