package com.aenadgrleey.paginaut.core

import androidx.compose.runtime.Immutable

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
        endReached: Boolean = items.isEmpty(),
    ): Page<Key, Item> = when (direction) {
        Direction.Forward -> Page(items, nextKey = nextKey(direction), prevKey = null, endReached = endReached)
        Direction.Backward -> Page(items, nextKey = null, prevKey = prevKey(direction), endReached = endReached)
        Direction.Init -> Page(items, nextKey = nextKey(direction), prevKey = prevKey(direction), endReached = endReached)
    }
}

data class Page<out Key : Any, out Item : Any>
    (
    val items: List<Item>,
    val nextKey: Key?,
    val prevKey: Key?,
    val endReached: Boolean = items.isEmpty(),
)

data class SimplePage<out Key : Any, out Item : Any>(
    val items: List<Item>,
    val nextKey: Key?,
    val endReached: Boolean = items.isEmpty(),
)

data class VisibleRange(
    val firstVisible: Int = 0,
    val lastVisible: Int = 0,
)

@Immutable
data class PaginationState<Item : Any>(
    val items: List<Item> = emptyList(),
    val init: LoadStatus = LoadStatus.Idle,
    val forward: LoadStatus = LoadStatus.Idle,
    val backward: LoadStatus = LoadStatus.Idle,
)


@Immutable
data class SimplePaginationState<Item : Any>(
    val items: List<Item> = emptyList(),
    val loadStatus: LoadStatus = LoadStatus.Idle,
)
