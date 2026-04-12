package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.grid.LazyGridState
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
class GridViewportState internal constructor(
    val gridState: LazyGridState,
    private val density: Density,
) {
    internal val trackedHeights = mutableStateMapOf<Any, Float>()

    val remainingHeight: Dp by derivedStateOf {
        val info = gridState.layoutInfo
        val viewportHeight = info.viewportSize.height
        if (viewportHeight == 0 || gridState.firstVisibleItemIndex != 0) {
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
fun rememberGridViewport(
    gridState: LazyGridState,
): GridViewportState {
    val density = LocalDensity.current
    return remember(gridState, density) { GridViewportState(gridState, density) }
}

fun Modifier.extensive(viewport: GridViewportState): Modifier = composed {
    val key = remember { Any() }
    DisposableEffect(key) {
        onDispose { viewport.trackedHeights.remove(key) }
    }
    this.onGloballyPositioned {
        viewport.trackedHeights[key] = it.size.height.toFloat()
    }
}
