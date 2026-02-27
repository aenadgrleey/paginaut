package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.Pager
import com.aenadgrleey.paginaut.core.PaginationState

@Composable
fun <Item : Any> PaginatedLazyVerticalGrid(
    paginationState: PaginationState<Item>,
    columns: GridCells,
    modifier: Modifier = Modifier,
    refreshIndicator: @Composable () -> Unit = {},
    refreshErrorIndicator: @Composable (Throwable) -> Unit = {},
    emptyIndicator: @Composable () -> Unit = {},
    forwardLoadingIndicator: @Composable () -> Unit = {},
    forwardErrorIndicator: @Composable (Throwable) -> Unit = {},
    backwardLoadingIndicator: @Composable () -> Unit = {},
    backwardErrorIndicator: @Composable (Throwable) -> Unit = {},
    endReachedIndicator: @Composable () -> Unit = {},
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(Item) -> Unit,
) {
    when {
        paginationState.refresh is LoadStatus.Loading && paginationState.items.isEmpty() -> {
            Box(modifier, contentAlignment = Alignment.Center) { refreshIndicator() }
            return
        }
        paginationState.refresh is LoadStatus.Error && paginationState.items.isEmpty() -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                refreshErrorIndicator((paginationState.refresh as LoadStatus.Error).cause)
            }
            return
        }
        paginationState.items.isEmpty()
                && paginationState.refresh is LoadStatus.Idle
                && paginationState.forward is LoadStatus.EndReached -> {
            Box(modifier, contentAlignment = Alignment.Center) { emptyIndicator() }
            return
        }
    }

    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        paginationState.backwardLoading { backwardLoadingIndicator() }
        paginationState.backwardError { backwardErrorIndicator(it) }
        paginationState.items(key, contentType, itemContent)
        paginationState.forwardLoading { forwardLoadingIndicator() }
        paginationState.forwardError { forwardErrorIndicator(it) }
        paginationState.endReached { endReachedIndicator() }
    }
}

@Composable
fun <Key : Any, Item : Any> PaginatedLazyVerticalGrid(
    pager: Pager<Key, Item>,
    columns: GridCells,
    modifier: Modifier = Modifier,
    refreshIndicator: @Composable () -> Unit = {},
    refreshErrorIndicator: @Composable (Throwable) -> Unit = {},
    emptyIndicator: @Composable () -> Unit = {},
    forwardLoadingIndicator: @Composable () -> Unit = {},
    forwardErrorIndicator: @Composable (Throwable) -> Unit = {},
    backwardLoadingIndicator: @Composable () -> Unit = {},
    backwardErrorIndicator: @Composable (Throwable) -> Unit = {},
    endReachedIndicator: @Composable () -> Unit = {},
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(Item) -> Unit,
) {
    val paginationState by pager.state.collectAsState()

    PagerEffect(pager, state)

    PaginatedLazyVerticalGrid(
        paginationState = paginationState,
        columns = columns,
        modifier = modifier,
        refreshIndicator = refreshIndicator,
        refreshErrorIndicator = refreshErrorIndicator,
        emptyIndicator = emptyIndicator,
        forwardLoadingIndicator = forwardLoadingIndicator,
        forwardErrorIndicator = forwardErrorIndicator,
        backwardLoadingIndicator = backwardLoadingIndicator,
        backwardErrorIndicator = backwardErrorIndicator,
        endReachedIndicator = endReachedIndicator,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        key = key,
        contentType = contentType,
        itemContent = itemContent,
    )
}
