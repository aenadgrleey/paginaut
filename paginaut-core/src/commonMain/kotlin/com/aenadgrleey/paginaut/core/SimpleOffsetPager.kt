package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext

/**
 * A forward-only pager that uses integer offsets.
 *
 * The [load] lambda receives the current offset (0-based) and returns a list of items.
 * The next offset is automatically calculated as `currentOffset + items.size`.
 * End is reached when the returned list has fewer items than [pageSize] or is empty.
 */
fun <Item : Any> SimpleOffsetPager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    deduplicateBy: ((Item) -> Any)? = null,
    load: suspend (offset: Int) -> List<Item>,
): SimplePager<Int, Item> {
    return object : SimplePager<Int, Item>(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialKey = 0,
        coroutineContext = coroutineContext,
        deduplicateBy = deduplicateBy,
    ) {
        override val deduplicateBy: ((Item) -> Any)? = deduplicateBy

        override suspend fun load(key: Int?): SimplePage<Int, Item> {
            val offset = key ?: 0
            val items = load(offset)
            val endReached = items.size < pageSize
            return SimplePage(
                items = items,
                nextKey = if (endReached) null else offset + items.size,
                endReached = endReached,
            )
        }
    }
}
