package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aenadgrleey.paginaut.core.BidirPager
import com.aenadgrleey.paginaut.core.PaginationState

@Composable
fun <Item : Any> PaginatedLazyHorizontalStaggeredGrid(
    paginationState: PaginationState<Item>,
    rows: StaggeredGridCells,
    modifier: Modifier = Modifier,
    indicators: PaginationIndicatorsScope.() -> Unit = {},
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalItemSpacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyStaggeredGridItemScope.(Item) -> Unit,
) {
    val indicatorConfig = PaginationIndicatorsScope().apply(indicators).build()

    LazyHorizontalStaggeredGrid(
        rows = rows,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalItemSpacing = horizontalItemSpacing,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        paginationState.firstPageLoading { indicatorConfig.init.loading() }
        paginationState.firstPageError { indicatorConfig.init.error(it) }
        paginationState.firstPageEmpty { indicatorConfig.init.empty() }
        paginationState.backwardLoading { indicatorConfig.backward.loading() }
        paginationState.backwardError { indicatorConfig.backward.error(it) }
        paginationState.backwardEndReached { indicatorConfig.backward.empty() }
        paginationState.items(key, contentType, itemContent)
        paginationState.forwardLoading { indicatorConfig.forward.loading() }
        paginationState.forwardError { indicatorConfig.forward.error(it) }
        paginationState.forwardEndReached { indicatorConfig.forward.empty() }
    }
}

@Composable
fun <Key : Any, Item : Any> PaginatedLazyHorizontalStaggeredGrid(
    pager: BidirPager<Key, Item>,
    rows: StaggeredGridCells,
    modifier: Modifier = Modifier,
    indicators: PaginationIndicatorsScope.() -> Unit = {},
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalItemSpacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyStaggeredGridItemScope.(Item) -> Unit,
) {
    val paginationState by pager.state.collectAsState()

    PagerEffect(pager, state)

    PaginatedLazyHorizontalStaggeredGrid(
        paginationState = paginationState,
        rows = rows,
        modifier = modifier,
        indicators = indicators,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalItemSpacing = horizontalItemSpacing,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        key = key,
        contentType = contentType,
        itemContent = itemContent,
    )
}
