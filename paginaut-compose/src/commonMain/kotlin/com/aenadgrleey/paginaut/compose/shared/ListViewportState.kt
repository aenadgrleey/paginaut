package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Stable
class ListViewportState internal constructor(
    val listState: LazyListState,
    private val density: Density,
) {
    internal val trackedHeights = mutableStateMapOf<Any, Float>()

    val remainingHeight: Dp by derivedStateOf {
        val info = listState.layoutInfo
        val viewportHeight = info.viewportSize.height
        if (viewportHeight == 0 || listState.firstVisibleItemIndex != 0) {
            return@derivedStateOf Dp.Unspecified
        }

        val trackedTotal = trackedHeights.values.sum()
        val spacing = info.mainAxisItemSpacing
        val spacingTotal = spacing * trackedHeights.size
        val padding = info.beforeContentPadding + info.afterContentPadding

        val remaining = viewportHeight - trackedTotal - spacingTotal - padding
        if (remaining > 0) with(density) { remaining.toDp() }
        else Dp.Unspecified
    }
}

@Composable
fun rememberListViewport(
    listState: LazyListState,
): ListViewportState {
    val density = LocalDensity.current
    return remember(listState, density) { ListViewportState(listState, density) }
}

fun Modifier.extensive(viewport: ListViewportState): Modifier = composed {
    val key = remember { Any() }
    DisposableEffect(key) {
        onDispose { viewport.trackedHeights.remove(key) }
    }
    this.onGloballyPositioned {
        viewport.trackedHeights[key] = it.size.height.toFloat()
    }
}
