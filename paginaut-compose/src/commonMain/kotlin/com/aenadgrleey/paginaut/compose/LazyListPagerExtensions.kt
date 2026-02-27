package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyListScope)
fun <Item : Any> PaginationState<Item>.items(
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    scope.items(
        count = items.size,
        key = key?.let { keyFn -> { keyFn(items[it]) } },
        contentType = { contentType(items[it]) },
    ) {
        itemContent(items[it])
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.firstPageLoading(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (refresh is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_LOADING) { content() }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.firstPageError(
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = refresh as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_ERROR) { content(error.cause) }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.empty(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (items.isEmpty() && refresh is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.EMPTY) { content() }
    }
}

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
fun PaginationState<*>.endReached(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.END_REACHED) { content() }
    }
}
