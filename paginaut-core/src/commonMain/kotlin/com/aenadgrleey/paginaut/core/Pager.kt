package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.flow.StateFlow

interface Pager<Key : Any, Item : Any> {
    val state: StateFlow<PaginationState<Item>>

    fun init()
    fun onVisibleRangeChanged(range: VisibleRange)
    fun refresh()
    fun jumpTo(key: Key)
    fun retry(direction: Direction)
    fun update(block: (List<Item>) -> List<Item>)
    fun close()
}

interface SimplePager<Key : Any, Item : Any> : Pager<Key, Item> {
    fun retry()
}

fun <Key : Any, Item : Any> Pager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    initialKey: Key? = null,
    load: suspend (LoadParams<Key>) -> Page<Key, Item>,
): Pager<Key, Item> = PagerImpl(pageSize, prefetchDistance, initialKey, load)

fun <Key : Any, Item : Any> SimplePager(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    initialKey: Key? = null,
    load: suspend (key: Key?) -> Page<Key, Item>,
): SimplePager<Key, Item> {
    val pager = Pager(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialKey = initialKey,
    ) { params ->
        if (params.direction == Direction.Backward) {
            Page(items = emptyList(), nextKey = null, prevKey = null)
        } else {
            load(params.key)
        }
    }

    return object : SimplePager<Key, Item>, Pager<Key, Item> by pager {
        override fun retry() = pager.retry(Direction.Forward)
    }
}
