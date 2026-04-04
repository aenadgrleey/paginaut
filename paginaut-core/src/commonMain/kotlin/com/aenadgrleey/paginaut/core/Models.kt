package com.aenadgrleey.paginaut.core

enum class Direction { Forward, Backward, Init }

sealed interface LoadStatus {
    data object Idle : LoadStatus
    data object Loading : LoadStatus
    data class Error(val cause: Throwable) : LoadStatus
    data object EndReached : LoadStatus
}

data class LoadParams<Key : Any>(
    internal val key: Key?,
    val direction: Direction,
    val pageSize: Int,
) {
    val initKey: Key? get() = key.takeIf { direction == Direction.Init }
    val forwardKey: Key? get() = key.takeIf { direction == Direction.Forward }
    val backwardKey: Key? get() = key.takeIf { direction == Direction.Backward }

    fun <Item : Any> nextPage(
        items: List<Item>,
        nextKey: (Direction) -> Key?,
        prevKey: (Direction) -> Key?,
        direction: Direction = this.direction,
    ): Page<Key, Item> = when (direction) {
        Direction.Forward -> Page(items, nextKey = nextKey(direction), prevKey = null)
        Direction.Backward -> Page(items, nextKey = null, prevKey = prevKey(direction))
        Direction.Init -> Page(items, nextKey = nextKey(direction), prevKey = prevKey(direction))
    }
}

data class Page<out Key : Any, out Item : Any>(
    val items: List<Item>,
    val nextKey: Key?,
    val prevKey: Key?,
)

data class SimplePage<out Key : Any, out Item : Any>(
    val items: List<Item>,
    val nextKey: Key?,
)

data class VisibleRange(
    val firstVisible: Int = 0,
    val lastVisible: Int = 0,
)

data class PaginationState<Item : Any>(
    val items: List<Item> = emptyList(),
    val init: LoadStatus = LoadStatus.Idle,
    val forward: LoadStatus = LoadStatus.Idle,
    val backward: LoadStatus = LoadStatus.Idle,
)


data class SimplePaginationState<Item : Any>(
    val items: List<Item> = emptyList(),
    val loadStatus: LoadStatus = LoadStatus.Idle,
)