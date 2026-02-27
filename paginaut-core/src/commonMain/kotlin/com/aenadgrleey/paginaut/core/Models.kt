package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.flow.StateFlow

enum class Direction { Forward, Backward, Refresh }

sealed interface LoadStatus {
    data object Idle : LoadStatus
    data object Loading : LoadStatus
    data class Error(val cause: Throwable) : LoadStatus
    data object EndReached : LoadStatus
}

data class LoadParams<Key : Any>(
    val key: Key?,
    val direction: Direction,
    val pageSize: Int,
)

data class Page<out Key : Any, out Item : Any>(
    val items: List<Item>,
    val nextKey: Key?,
    val prevKey: Key?,
)

data class VisibleRange(
    val firstVisible: Int = 0,
    val lastVisible: Int = 0,
)

data class PaginationState<Item : Any>(
    val items: List<Item> = emptyList(),
    val forward: LoadStatus = LoadStatus.Idle,
    val backward: LoadStatus = LoadStatus.Idle,
    val refresh: LoadStatus = LoadStatus.Idle,
)
