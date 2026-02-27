package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyGridScope)
fun <Item : Any> PaginationState<Item>.items(
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(Item) -> Unit,
) {
    scope.items(
        count = items.size,
        key = key?.let { keyFn -> { keyFn(items[it]) } },
        contentType = { contentType(items[it]) },
    ) {
        itemContent(items[it])
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.firstPageLoading(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (refresh is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_LOADING, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.firstPageError(
    content: @Composable LazyGridItemScope.(Throwable) -> Unit,
) {
    val error = refresh as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_ERROR, span = { GridItemSpan(maxLineSpan) }) { content(error.cause) }
    }
}

context(scope: LazyGridScope)
fun PaginationState<*>.empty(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (items.isEmpty() && refresh is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.EMPTY, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}

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
fun PaginationState<*>.endReached(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.END_REACHED, span = { GridItemSpan(maxLineSpan) }) { content() }
    }
}
