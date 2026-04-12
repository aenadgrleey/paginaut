package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
internal fun FillRemainingHeight(
    viewport: GridViewportState,
    content: @Composable () -> Unit,
) {
    FillRemainingHeightImpl(viewport.remainingHeight, content)
}

@Composable
internal fun FillRemainingHeight(
    viewport: ListViewportState,
    content: @Composable () -> Unit,
) {
    FillRemainingHeightImpl(viewport.remainingHeight, content)
}

@Composable
private fun FillRemainingHeightImpl(
    remainingHeight: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = if (remainingHeight != Dp.Unspecified) {
            Modifier.heightIn(min = remainingHeight)
        } else {
            Modifier
        },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
