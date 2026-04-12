package com.aenadgrleey.paginaut.compose.list

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.LoadStatus
import com.aenadgrleey.paginaut.core.PaginationState
import com.aenadgrleey.paginaut.compose.shared.FillRemainingHeight
import com.aenadgrleey.paginaut.compose.shared.ListViewportState
import com.aenadgrleey.paginaut.compose.shared.PagerKeys

context(scope: LazyListScope)
internal fun PaginationState<*>.placeholders(
    count: Int,
    content: @Composable LazyItemScope.(Int) -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        repeat(count) { index ->
            scope.item(key = "${PagerKeys.INIT_PLACEHOLDER}_$index") {
                content.invoke(this, index)
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.initLoading(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (init is LoadStatus.Loading && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_LOADING) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.initError(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.(Throwable) -> Unit,
) {
    val error = init as? LoadStatus.Error
    if (error != null && items.isEmpty()) {
        scope.item(key = PagerKeys.INIT_ERROR) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content(error.cause) }
            } else {
                content(error.cause)
            }
        }
    }
}

context(scope: LazyListScope)
fun PaginationState<*>.initEmpty(
    viewport: ListViewportState? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (items.isEmpty() && init is LoadStatus.Idle && forward is LoadStatus.EndReached) {
        scope.item(key = PagerKeys.INIT_EMPTY) {
            if (viewport != null) {
                FillRemainingHeight(viewport) { content() }
            } else {
                content()
            }
        }
    }
}
