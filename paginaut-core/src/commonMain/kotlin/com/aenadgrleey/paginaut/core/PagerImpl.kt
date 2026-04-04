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
internal class PagerImpl<Key : Any, Item : Any>(
    private val pageSize: Int,
    private val prefetchDistance: Int,
    override var initialKey: Key?,
    private val load: suspend (LoadParams<Key>) -> Page<Key, Item>,
    coroutineContext: CoroutineContext = Dispatchers.Default,
) : Pager<Key, Item> {

    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)
    private val mutex = Mutex()
    private val _state = MutableStateFlow(PaginationState<Item>())
    private val initialized = AtomicBoolean(false)

    override val state: StateFlow<PaginationState<Item>> = _state.asStateFlow()

    private var forwardKey: Key? = null
    private var backwardKey: Key? = null
    private var forwardJob: Job? = null
    private var backwardJob: Job? = null

    override fun init() {
        if (!initialized.compareAndSet(expectedValue = false, newValue = true)) return
        refresh()
    }

    override fun onVisibleRangeChanged(range: VisibleRange) {
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

    override fun refresh(key: Key?) {
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

    override fun retry(direction: Direction) {
        scope.launch {
            mutex.withLock {
                _state.update {
                    when (direction) {
                        Direction.Forward -> it.copy(forward = LoadStatus.Loading)
                        Direction.Backward -> it.copy(backward = LoadStatus.Loading)
                        Direction.Init -> it.copy(init = LoadStatus.Loading)
                    }
                }
                doLoad(direction)
            }
        }
    }

    override fun continueLoading(direction: Direction) {
        scope.launch {
            mutex.withLock {
                _state.update {
                    when (direction) {
                        Direction.Forward -> if (it.forward == LoadStatus.EndReached) it.copy(forward = LoadStatus.Idle) else it
                        Direction.Backward -> if (it.backward == LoadStatus.EndReached) it.copy(backward = LoadStatus.Idle) else it
                        Direction.Init -> if (it.init == LoadStatus.EndReached) it.copy(init = LoadStatus.Idle) else it
                    }
                }
            }
        }
    }

    override fun update(block: (List<Item>) -> List<Item>) {
        scope.launch {
            mutex.withLock { _state.update { it.copy(items = block(it.items)) } }
        }
    }

    override fun close() = scope.cancel()

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

        runCatching { load(LoadParams(key, direction, pageSize)) }
            .onSuccess { page ->
                _state.update { current ->
                    val newItems = when (direction) {
                        Direction.Forward, Direction.Init -> current.items + page.items
                        Direction.Backward -> page.items + current.items
                    }
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
