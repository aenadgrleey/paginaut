package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalAtomicApi::class)
open class BidirPager<Key : Any, Item : Any>(
    private val pageSize: Int = 20,
    private val prefetchDistance: Int = 5,
    var initialKey: Key? = null,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    deduplicateBy: ((Item) -> Any)? = null,
    load: (suspend (LoadParams<Key>) -> Page<Key, Item>)? = null,
) {

    private val _load = load
    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)
    private val mutex = Mutex()
    private val _state = MutableStateFlow(PaginationState<Item>())
    val state: StateFlow<PaginationState<Item>> = _state.asStateFlow()
    private val initialized = AtomicBoolean(false)

    private var forwardKey: Key? = null
    private var backwardKey: Key? = null
    private var forwardJob: Job? = null
    private var backwardJob: Job? = null

    fun init() {
        if (!initialized.compareAndSet(expectedValue = false, newValue = true)) return
        refresh()
    }

    fun onVisibleRangeChanged(range: VisibleRange) {
        init()
        val s = _state.value
        val count = s.items.size

        if (range.lastVisible + prefetchDistance >= count && s.forward == LoadStatus.Idle) {
            if (forwardJob?.isActive != true) {
                forwardJob = scope.launch { mutex.withLock { doLoad(Direction.Forward) } }
            }
        }

        if (range.firstVisible - prefetchDistance <= 0 && s.backward == LoadStatus.Idle) {
            if (backwardJob?.isActive != true) {
                backwardJob = scope.launch { mutex.withLock { doLoad(Direction.Backward) } }
            }
        }
    }

    fun refresh(key: Key? = initialKey) {
        forwardJob?.cancel()
        backwardJob?.cancel()
        scope.launch {
            mutex.withLock {
                initialKey = key
                forwardKey = key
                backwardKey = null
                _state.value = PaginationState(init = LoadStatus.Loading)
                doLoad(Direction.Init)
            }
        }
    }

    fun jumpTo(key: Key) = refresh(key)

    fun retry(direction: Direction) {
        scope.launch {
            mutex.withLock {
                val status = when (direction) {
                    Direction.Forward -> _state.value.forward
                    Direction.Backward -> _state.value.backward
                    Direction.Init -> _state.value.init
                }
                if (status is LoadStatus.Error) doLoad(direction)
            }
        }
    }

    fun continueForward() {
        scope.launch { mutex.withLock { doLoad(Direction.Forward) } }
    }

    fun continueBackward() {
        scope.launch { mutex.withLock { doLoad(Direction.Backward) } }
    }

    fun update(block: (List<Item>) -> List<Item>) {
        scope.launch {
            mutex.withLock { _state.update { it.copy(items = block(it.items)) } }
        }
    }

    fun close() = scope.cancel()

    protected open suspend fun load(params: LoadParams<Key>) =
        requireNotNull(_load?.invoke(params)) { "load() must be overridden or provided via constructor" }

    open val deduplicateBy: ((Item) -> Any)? = deduplicateBy

    private fun deduplicate(items: List<Item>): List<Item> {
        val selector = deduplicateBy ?: return items
        val seen = LinkedHashMap<Any, Item>(items.size)
        for (item in items) seen[selector(item)] = item
        return if (seen.size == items.size) items else seen.values.toList()
    }

    private suspend fun doLoad(direction: Direction) {
        val key = when (direction) {
            Direction.Forward -> forwardKey
            Direction.Backward -> backwardKey
            Direction.Init -> initialKey
        }

        _state.update {
            when (direction) {
                Direction.Forward -> it.copy(forward = LoadStatus.Loading)
                Direction.Backward -> it.copy(backward = LoadStatus.Loading)
                Direction.Init -> it.copy(init = LoadStatus.Loading)
            }
        }

        runCatching { this@BidirPager.load(LoadParams(key, direction, pageSize)) }
            .onSuccess { page ->
                _state.update { current ->
                    val newItems = deduplicate(
                        when (direction) {
                            Direction.Forward, Direction.Init -> current.items + page.items
                            Direction.Backward -> page.items + current.items
                        }
                    )
                    when (direction) {
                        Direction.Init -> {
                            forwardKey = page.nextKey ?: forwardKey
                            backwardKey = page.prevKey ?: backwardKey
                            current.copy(
                                items = newItems,
                                init = LoadStatus.Idle,
                                forward = if (page.endReached) LoadStatus.EndReached else LoadStatus.Idle,
                                backward = if (page.endReached) LoadStatus.EndReached else LoadStatus.Idle,
                            )
                        }

                        Direction.Forward -> {
                            forwardKey = page.nextKey ?: forwardKey
                            current.copy(
                                items = newItems,
                                forward = if (page.endReached) LoadStatus.EndReached else LoadStatus.Idle,
                            )
                        }

                        Direction.Backward -> {
                            backwardKey = page.prevKey ?: backwardKey
                            current.copy(
                                items = newItems,
                                backward = if (page.endReached) LoadStatus.EndReached else LoadStatus.Idle,
                            )
                        }
                    }
                }
            }
            .onFailure { e ->
                if (e is CancellationException) throw e
                _state.update {
                    when (direction) {
                        Direction.Forward -> it.copy(forward = LoadStatus.Error(e))
                        Direction.Backward -> it.copy(backward = LoadStatus.Error(e))
                        Direction.Init -> it.copy(init = LoadStatus.Error(e))
                    }
                }
            }
    }
}
