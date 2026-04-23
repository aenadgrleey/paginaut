package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * A forward-only pager that tracks pagination by item ID.
 *
 * The [load] lambda receives the ID of the last loaded item (or `null` for the first page)
 * and returns a list of items. The next key is derived via [idSelector] from the last item.
 * End is reached when the returned list is empty.
 */
fun <Id : Any, Item : Any> SimpleIdPager(
    idSelector: (Item) -> Id,
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    deduplicateBy: ((Item) -> Any)? = null,
    load: suspend (afterId: Id?) -> List<Item>,
): SimplePager<Id, Item> {
    return object : SimplePager<Id, Item>(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        coroutineContext = coroutineContext,
        deduplicateBy = deduplicateBy,
    ) {
        override val deduplicateBy: ((Item) -> Any)? = deduplicateBy

        override suspend fun load(key: Id?): SimplePage<Id, Item> {
            val items = load(key)
            val endReached = items.isEmpty()
            return SimplePage(
                items = items,
                nextKey = if (endReached) null else idSelector(items.last()),
                endReached = endReached,
            )
        }
    }
}
