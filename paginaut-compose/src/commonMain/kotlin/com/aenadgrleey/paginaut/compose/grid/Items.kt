package com.aenadgrleey.paginaut.compose

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aenadgrleey.paginaut.core.PaginationState

context(scope: LazyGridScope)
fun <Item : Any> PaginationState<Item>.items(
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    span: (LazyGridItemSpanScope.(Item) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(Item) -> Unit,
) {
    scope.items(
        count = items.size,
        key = key?.let { keyFn -> { keyFn(items[it]) } },
        contentType = { contentType(items[it]) },
        span = span?.let { spanFn -> { spanFn(items[it]) } },
    ) {
        itemContent(items[it])
    }
}
