package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
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

class GroupedItemsDsl<Item : Any, GroupKey : Any> internal constructor() {
    var key: ((Item) -> Any)? = null
    var contentType: (Item) -> Any? = { null }

    internal var groupByFn: ((Item) -> GroupKey)? = null
    internal var groupStartFn: (@Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit)? = null
    internal var groupEndFn: (@Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit)? = null

    fun groupBy(selector: (Item) -> GroupKey) {
        groupByFn = selector
    }

    fun groupStart(content: @Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit) {
        groupStartFn = content
    }

    fun groupEnd(content: @Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit) {
        groupEndFn = content
    }

}

context(scope: LazyListScope)
fun <Item : Any, GroupKey : Any> PaginationState<Item>.groupedItems(
    grouping: GroupedItemsDsl<Item, GroupKey>.() -> Unit,
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    if (items.isEmpty()) return

    val config = GroupedItemsDsl<Item, GroupKey>().apply(grouping)
    val groupBy = checkNotNull(config.groupByFn) {
        "groupBy { ... } must be provided for groupedItems."
    }
    val key = config.key
    val contentType = config.contentType

    val groupKeys = items.map(groupBy)

    val lastIndex = items.lastIndex
    for (index in 0..lastIndex) {
        val item = items[index]
        val groupKey = groupKeys[index]
        val prevGroupKey = groupKeys.getOrNull(index - 1)
        val nextGroupKey = groupKeys.getOrNull(index + 1)
        val isGroupStart = prevGroupKey != groupKey
        val isGroupEnd = nextGroupKey != groupKey
        val itemKey = key?.invoke(item)

        config.groupStartFn?.takeIf { isGroupStart }?.let {
            scope.item(
                key = GroupedListEntryKey(
                    type = "header",
                    index = index,
                    itemKey = itemKey,
                ),
            ) {
                it(groupKey, item)
            }
        }

        scope.item(
            key = itemKey,
            contentType = contentType(item),
        ) {
            itemContent(item)
        }

        config.groupEndFn?.takeIf { isGroupEnd }?.let {
            scope.item(
                key = GroupedListEntryKey(
                    type = "footer",
                    index = index,
                    itemKey = itemKey,
                ),
            ) {
                it(groupKey, item)
            }
        }
    }
}
