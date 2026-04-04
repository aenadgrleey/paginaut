package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext

internal class SimplePagerImpl<Key : Any, Item : Any>(
    pageSize: Int,
    prefetchDistance: Int,
    initialKey: Key?,
    load: suspend (key: Key?) -> SimplePage<Key, Item>,
    coroutineContext: CoroutineContext,
) : SimplePager<Key, Item> {

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val pager = Pager(
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

    override val state: StateFlow<SimplePaginationState<Item>> =
        pager.state
            .map { s -> s.toSimpleState() }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = pager.state.value.toSimpleState()
            )

    override val initialKey: Key? get() = pager.initialKey

    override fun init() = pager.init()

    override fun onVisibleRangeChanged(range: VisibleRange) =
        pager.onVisibleRangeChanged(range)

    override fun refresh(key: Key?) = pager.refresh(key)

    override fun retry() {
        val s = pager.state.value
        when {
            s.forward is LoadStatus.Error -> pager.retry(Direction.Forward)
            s.init is LoadStatus.Error -> pager.retry(Direction.Init)
        }
    }

    override fun continueLoading() {
        pager.continueLoading(Direction.Init)
        pager.continueLoading(Direction.Forward)
    }

    override fun update(block: (List<Item>) -> List<Item>) =
        pager.update { block(it) }

    override fun close() = pager.close()
}