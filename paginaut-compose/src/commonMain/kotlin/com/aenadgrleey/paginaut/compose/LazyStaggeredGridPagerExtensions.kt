package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyStaggeredGridScope)
fun <Item : Any> PaginationState<Item>.items(
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyStaggeredGridItemScope.(Item) -> Unit,
) {
    scope.items(
        count = items.size,
        key = key?.let { keyFn -> { keyFn(items[it]) } },
        contentType = { contentType(items[it]) },
    ) {
        itemContent(items[it])
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.firstPageLoading(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (refresh is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_LOADING, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.firstPageError(
    content: @Composable LazyStaggeredGridItemScope.(Throwable) -> Unit,
) {
    val error = refresh as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_ERROR, span = StaggeredGridItemSpan.FullLine) { content(error.cause) }
    }
}

context(scope: LazyStaggeredGridScope)
fun PaginationState<*>.empty(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (items.isEmpty() && refresh is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.EMPTY, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}

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
fun PaginationState<*>.endReached(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.END_REACHED, span = StaggeredGridItemSpan.FullLine) { content() }
    }
}
