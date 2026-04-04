package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext

open class SimplePager<Key : Any, Item : Any>(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    initialKey: Key? = null,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (key: Key?) -> SimplePage<Key, Item>,
) {

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val pager = BidirPager(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialKey = initialKey,
        coroutineContext = coroutineContext,
    ) { params ->
        if (params.direction == Direction.Backward) {
            Page(items = emptyList(), nextKey = null, prevKey = null)
        } else {
            load(params.key).toPage()
        }
    }

    val state: StateFlow<SimplePaginationState<Item>> =
        pager.state
            .map { s -> s.toSimpleState() }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = pager.state.value.toSimpleState()
            )

    val initialKey: Key? get() = pager.initialKey

    open fun init() = pager.init()

    open fun onVisibleRangeChanged(range: VisibleRange) =
        pager.onVisibleRangeChanged(range)

    open fun refresh(key: Key? = pager.initialKey) = pager.refresh(key)

    open fun retry() {
        pager.retry(Direction.Init)
        pager.retry(Direction.Forward)
    }

    open fun continueForward() = pager.continueForward()

    open fun update(block: (List<Item>) -> List<Item>) =
        pager.update { block(it) }

    open fun close() {
        coroutineScope.cancel()
        pager.close()
    }
}

open class SimpleOffsetPager<Item : Any>(
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (offset: Int) -> List<Item>,
) : SimplePager<Int, Item>(
    pageSize = pageSize,
    prefetchDistance = prefetchDistance,
    initialKey = 0,
    coroutineContext = coroutineContext,
    load = { offset ->
        val items = load(offset ?: 0)
        SimplePage(
            items = items,
            nextKey = (offset ?: 0) + items.size,
            endReached = items.size < pageSize,
        )
    },
)

open class SimpleIdPager<Id : Any, Item : Any>(
    idSelector: (Item) -> Id,
    pageSize: Int = 20,
    prefetchDistance: Int = 5,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    load: suspend (afterId: Id?) -> List<Item>,
) : SimplePager<Id, Item>(
    pageSize = pageSize,
    prefetchDistance = prefetchDistance,
    initialKey = null,
    coroutineContext = coroutineContext,
    load = { afterId ->
        val items = load(afterId)
        SimplePage(
            items = items,
            nextKey = items.lastOrNull()?.let(idSelector),
            endReached = items.size < pageSize,
        )
    },
)
