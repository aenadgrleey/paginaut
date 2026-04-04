package com.aenadgrleey.paginaut.core

internal fun <Item : Any> PaginationState<Item>.toSimpleState(): SimplePaginationState<Item> =
    SimplePaginationState(
        items = items,
        loadStatus = when {
            forward is LoadStatus.Loading || init is LoadStatus.Loading -> LoadStatus.Loading
            forward is LoadStatus.Error -> forward
            init is LoadStatus.Error -> init
            forward is LoadStatus.EndReached -> forward
            else -> LoadStatus.Idle
        }
    )

internal fun <Item : Any, Key : Any> SimplePage<Key, Item>.toPage(): Page<Key, Item> =
    Page(
        items = items,
        nextKey = nextKey,
        prevKey = null,
        endReached = endReached,
    )