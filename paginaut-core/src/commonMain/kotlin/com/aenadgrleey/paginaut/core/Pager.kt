package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

interface Pager<Key : Any, Item : Any> {
    val state: StateFlow<PaginationState<Item>>
    val initialKey: Key?

    fun init()
    fun onVisibleRangeChanged(range: VisibleRange)
    fun refresh(key: Key? = initialKey)
    fun jumpTo(key: Key) = refresh(key)
    fun retry(direction: Direction)
    fun continueForward()
    fun continueBackward()
    fun update(block: (List<Item>) -> List<Item>)
    fun close()
}

interface SimplePager<Key : Any, Item : Any> {
    val state: StateFlow<SimplePaginationState<Item>>
    val initialKey: Key?
    fun init()
    fun onVisibleRangeChanged(range: VisibleRange)
    fun refresh(key: Key? = initialKey)
    fun retry()
    fun continueForward()
    fun update(block: (List<Item>) -> List<Item>)
    fun close()
}

fun <Key : Any, Item : Any> Pager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    initialKey: Key? = null,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (LoadParams<Key>) -> Page<Key, Item>,
): Pager<Key, Item> = PagerImpl(pageSize, prefetchDistance, initialKey, load, coroutineContext)

fun <Key : Any, Item : Any> SimplePager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    initialKey: Key? = null,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (key: Key?) -> SimplePage<Key, Item>,
): SimplePager<Key, Item> = SimplePagerImpl(pageSize, prefetchDistance, initialKey, load, coroutineContext)

fun <Item : Any> SimpleOffsetPager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (offset: Int) -> List<Item>,
): SimplePager<Int, Item> = SimplePager(
    pageSize = pageSize,
    prefetchDistance = prefetchDistance,
    initialKey = 0,
    coroutineContext = coroutineContext,
) { offset ->
    val items = load(offset ?: 0)
    SimplePage(
        items = items,
        nextKey = (offset ?: 0) + items.size,
        endReached = items.size < pageSize,
    )
}

fun <Id : Any, Item : Any> SimpleIdPager(
    idSelector: (Item) -> Id,
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (afterId: Id?) -> List<Item>,
): SimplePager<Id, Item> = SimplePager(
    pageSize = pageSize,
    prefetchDistance = prefetchDistance,
    initialKey = null,
    coroutineContext = coroutineContext,
) { afterId ->
    val items = load(afterId)
    SimplePage(
        items = items,
        nextKey = items.lastOrNull()?.let(idSelector),
        endReached = items.size < pageSize,
    )
}
