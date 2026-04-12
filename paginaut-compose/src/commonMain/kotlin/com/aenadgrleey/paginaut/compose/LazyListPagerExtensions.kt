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

private data class GroupedListEntryKey(
    val type: String,
    val index: Int,
    val itemKey: Any?,
)

class GroupedItemsDsl<Item : Any, GroupKey : Any> {
    var key: ((Item) -> Any)? = null
    var contentType: (Item) -> Any? = { null }

    internal var groupByFn: ((Item) -> GroupKey)? = null
    internal var groupHeaderFn: (@Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit)? = null
    internal var groupFooterFn: (@Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit)? = null

    fun groupBy(selector: (Item) -> GroupKey) {
        groupByFn = selector
    }

    fun groupHeader(content: @Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit) {
        groupHeaderFn = content
    }

    fun groupFooter(content: @Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit) {
        groupFooterFn = content
    }

    internal fun getGroupBy(): (Item) -> GroupKey = checkNotNull(groupByFn) {
        "groupBy { ... } must be provided for groupedItems."
    }

    internal fun getGroupHeader():
        (@Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit)? = groupHeaderFn

    internal fun getGroupFooter():
        (@Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit)? = groupFooterFn
}

context(scope: LazyListScope)
fun <Item : Any, GroupKey : Any> PaginationState<Item>.groupedItems(
    grouping: GroupedItemsDsl<Item, GroupKey>.() -> Unit,
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    if (items.isEmpty()) return

    val config = GroupedItemsDsl<Item, GroupKey>().apply(grouping)
    val groupBy = config.getGroupBy()
    val key = config.key
    val contentType = config.contentType
    val groupHeader = config.getGroupHeader()
    val groupFooter = config.getGroupFooter()

    val lastIndex = items.lastIndex
    for (index in 0..lastIndex) {
        val item = items[index]
        val groupKey = groupBy(item)
        val prevGroupKey = items.getOrNull(index - 1)?.let(groupBy)
        val nextGroupKey = items.getOrNull(index + 1)?.let(groupBy)
        val isGroupStart = prevGroupKey != groupKey
        val isGroupEnd = nextGroupKey != groupKey
        val itemKey = key?.invoke(item)

        groupHeader?.takeIf { isGroupStart }?.let { header ->
            scope.item(
                key = GroupedListEntryKey(
                    type = "header",
                    index = index,
                    itemKey = itemKey,
                ),
            ) {
                header(groupKey, item)
            }
        }

        scope.item(
            key = itemKey,
            contentType = contentType(item),
        ) {
            itemContent(item)
        }

        groupFooter?.takeIf { isGroupEnd }?.let { footer ->
            scope.item(
                key = GroupedListEntryKey(
                    type = "footer",
                    index = index,
                    itemKey = itemKey,
                ),
            ) {
                footer(groupKey, item)
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.firstPageLoading(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_LOADING) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.firstPageError(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = init as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.FIRST_PAGE_ERROR) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content(error.cause) }
            } else {
                content(error.cause)
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.firstPageEmpty(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (items.isEmpty() && init is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.EMPTY) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
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
fun PaginationState<*>.forwardEndReached(
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (forward is LoadStatus.EndReached && items.isNotEmpty()) {
        scope.item(key = PagerKeys.FORWARD_END_REACHED) { content() }
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
