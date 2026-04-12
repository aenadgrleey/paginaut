package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.aenadgrleey.paginaut.core.BidirPager
import com.aenadgrleey.paginaut.core.VisibleRange

@Composable
fun PagerEffect(
    pager: BidirPager<*, *>,
    listState: LazyListState,
) {
    LaunchedEffect(pager, listState) {
        snapshotFlow {
            VisibleRange(
                firstVisible = listState.firstVisibleItemIndex,
                lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0,
            )
        }.collect { pager.onVisibleRangeChanged(it) }
    }
}

@Composable
fun PagerEffect(
    pager: BidirPager<*, *>,
    gridState: LazyGridState,
) {
    LaunchedEffect(pager, gridState) {
        snapshotFlow {
            VisibleRange(
                firstVisible = gridState.firstVisibleItemIndex,
                lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0,
            )
        }.collect { pager.onVisibleRangeChanged(it) }
    }
}

@Composable
fun PagerEffect(
    pager: BidirPager<*, *>,
    staggeredGridState: LazyStaggeredGridState,
) {
    LaunchedEffect(pager, staggeredGridState) {
        snapshotFlow {
            VisibleRange(
                firstVisible = staggeredGridState.firstVisibleItemIndex,
                lastVisible = staggeredGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0,
            )
        }.collect { pager.onVisibleRangeChanged(it) }
    }
}
