package com.aenadgrleey.paginaut.compose.list

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState
import com.aenadgrleey.paginaut.compose.shared.PagerKeys

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

internal data class GroupStartSpec<Item : Any, GroupKey : Any>(
    val sticky: Boolean,
    val content: @Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit,
)

class GroupedItemsDsl<Item : Any, GroupKey : Any> internal constructor() {
    var key: ((Item) -> Any)? = null
    var contentType: (Item) -> Any? = { null }

    internal var groupByFn: ((Item) -> GroupKey)? = null
    internal var groupStartFn: GroupStartSpec<Item, GroupKey>? = null
    internal var groupEndFn: (@Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit)? =
        null

    fun groupBy(selector: (Item) -> GroupKey) {
        groupByFn = selector
    }

    fun groupStart(
        sticky: Boolean = false,
        content: @Composable LazyItemScope.(groupKey: GroupKey, firstItem: Item) -> Unit,
    ) {
        groupStartFn = GroupStartSpec(
            sticky = sticky,
            content = content,
        )
    }

    fun groupEnd(content: @Composable LazyItemScope.(groupKey: GroupKey, lastItem: Item) -> Unit) {
        groupEndFn = content
    }

}

class ExternalItemsScope<Item : Any> internal constructor() {
    internal var backwardExternal: (LazyListScope.(LoadStatus) -> Unit)? = null
    internal var forwardExternal: (LazyListScope.(LoadStatus) -> Unit)? = null

    fun backwardExternal(content: LazyListScope.(LoadStatus) -> Unit) {
        backwardExternal = content
    }

    fun forwardExternal(content: LazyListScope.(LoadStatus) -> Unit) {
        forwardExternal = content
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

        config.groupStartFn?.takeIf { isGroupStart }?.let { header ->
            val headerKey = GroupedListEntryKey(
                type = if (header.sticky) "sticky-header" else "header",
                index = index,
                itemKey = itemKey,
            )
            if (header.sticky) {
                scope.stickyHeader(key = headerKey) {
                    header.content.invoke(this, groupKey, item)
                }
            } else {
                scope.item(key = headerKey) {
                    header.content.invoke(this, groupKey, item)
                }
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

context(scope: LazyListScope)
internal fun PaginationState<*>.forwardExternalItems(
    block: LazyListScope.(LoadStatus) -> Unit,
) {
    if (this.init is LoadStatus.Idle || this.init is LoadStatus.EndReached) {
        block(scope, this.forward)
    }
}

context(scope: LazyListScope)
internal fun PaginationState<*>.backwardExternalItems(
    block: LazyListScope.(LoadStatus) -> Unit,
) {
    if (this.init is LoadStatus.Idle || this.init is LoadStatus.EndReached) {
        block(scope, this.backward)
    }
}
