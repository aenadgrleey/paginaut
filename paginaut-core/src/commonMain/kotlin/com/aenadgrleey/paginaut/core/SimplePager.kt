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
    deduplicateBy: ((Item) -> Any)? = null,
    load: (suspend (key: Key?) -> SimplePage<Key, Item>)? = null,
) {

    private val _load = load
    private val coroutineScope = CoroutineScope(coroutineContext)

    open val deduplicateBy: ((Item) -> Any)? = deduplicateBy

    private val pager = object : BidirPager<Key, Item>(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialKey = initialKey,
        coroutineContext = coroutineContext,
    ) {
        override val deduplicateBy get() = this@SimplePager.deduplicateBy

        override suspend fun load(params: LoadParams<Key>): Page<Key, Item> =
            if (params.direction == Direction.Backward) {
                Page(items = emptyList(), nextKey = null, prevKey = null)
            } else {
                this@SimplePager.load(params.key).toPage()
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

    protected open suspend fun load(key: Key?) =
        requireNotNull(_load?.invoke(key)) { "load() must be overridden or provided via constructor" }

    fun init() = pager.init()

    fun onVisibleRangeChanged(range: VisibleRange) =
        pager.onVisibleRangeChanged(range)

    fun refresh(key: Key? = pager.initialKey) = pager.refresh(key)

    fun retry() {
        pager.retry(Direction.Init)
        pager.retry(Direction.Forward)
    }

    fun continueForward() = pager.continueForward()

    fun update(block: (List<Item>) -> List<Item>) =
        pager.update { block(it) }

    fun close() {
        coroutineScope.cancel()
        pager.close()
    }
}
