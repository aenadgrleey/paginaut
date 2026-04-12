package com.aenadgrleey.paginaut.compose.list

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aenadgrleey.paginaut.core.BidirPager
import com.aenadgrleey.paginaut.core.PaginationState
import com.aenadgrleey.paginaut.compose.shared.IndicatorsScope
import com.aenadgrleey.paginaut.compose.shared.PagerEffect

@Composable
fun <Item : Any> PaginatedLazyRow(
    paginationState: PaginationState<Item>,
    modifier: Modifier = Modifier,
    indicators: IndicatorsScope.() -> Unit = {},
    externals: ExternalItemsScope<Item>.() -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    val indicatorConfig = IndicatorsScope().apply(indicators)
    val externalConfig = ExternalItemsScope<Item>().apply(externals)

    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        if (indicatorConfig.init.shouldShowPlaceholders()) {
            paginationState.placeholders(
                count = indicatorConfig.init.placeholderCount,
                content = indicatorConfig.init.placeholderContent,
            )
        } else {
            paginationState.initLoading { indicatorConfig.init.loading() }
        }
        paginationState.initError { indicatorConfig.init.error(it) }
        paginationState.initEmpty { indicatorConfig.init.empty() }
        paginationState.backwardLoading { indicatorConfig.backward.loading() }
        paginationState.backwardError { indicatorConfig.backward.error(it) }
        paginationState.backwardEndReached { indicatorConfig.backward.empty() }
        paginationState.backwardExternalItems { externalConfig.backwardExternal?.invoke(this, it) }
        paginationState.items(key, contentType, itemContent)
        paginationState.forwardExternalItems { externalConfig.forwardExternal?.invoke(this, it) }
        paginationState.forwardLoading { indicatorConfig.forward.loading() }
        paginationState.forwardError { indicatorConfig.forward.error(it) }
        paginationState.forwardEndReached { indicatorConfig.forward.empty() }
    }
}

@Composable
fun <Key : Any, Item : Any> PaginatedLazyRow(
    pager: BidirPager<Key, Item>,
    modifier: Modifier = Modifier,
    indicators: IndicatorsScope.() -> Unit = {},
    externals: ExternalItemsScope<Item>.() -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    key: ((Item) -> Any)? = null,
    contentType: (Item) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    val paginationState by pager.state.collectAsState()

    PagerEffect(pager, state)

    PaginatedLazyRow(
        paginationState = paginationState,
        modifier = modifier,
        indicators = indicators,
        externals = externals,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        key = key,
        contentType = contentType,
        itemContent = itemContent,
    )
}

@Composable
fun <Item : Any, GroupKey : Any> PaginatedLazyRow(
    paginationState: PaginationState<Item>,
    modifier: Modifier = Modifier,
    indicators: IndicatorsScope.() -> Unit = {},
    externals: ExternalItemsScope<Item>.() -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    grouping: GroupedItemsDsl<Item, GroupKey>.() -> Unit,
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    val indicatorConfig = IndicatorsScope().apply(indicators)
    val externalConfig = ExternalItemsScope<Item>().apply(externals)

    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        if (indicatorConfig.init.shouldShowPlaceholders()) {
            paginationState.placeholders(
                count = indicatorConfig.init.placeholderCount,
                content = indicatorConfig.init.placeholderContent,
            )
        } else {
            paginationState.initLoading { indicatorConfig.init.loading() }
        }
        paginationState.initError { indicatorConfig.init.error(it) }
        paginationState.initEmpty { indicatorConfig.init.empty() }
        paginationState.backwardLoading { indicatorConfig.backward.loading() }
        paginationState.backwardError { indicatorConfig.backward.error(it) }
        paginationState.backwardEndReached { indicatorConfig.backward.empty() }
        paginationState.backwardExternalItems { externalConfig.backwardExternal?.invoke(this, it) }
        paginationState.groupedItems(
            grouping = grouping,
            itemContent = itemContent,
        )
        paginationState.forwardExternalItems { externalConfig.forwardExternal?.invoke(this, it) }
        paginationState.forwardLoading { indicatorConfig.forward.loading() }
        paginationState.forwardError { indicatorConfig.forward.error(it) }
        paginationState.forwardEndReached { indicatorConfig.forward.empty() }
    }
}

@Composable
fun <Key : Any, Item : Any, GroupKey : Any> PaginatedLazyRow(
    pager: BidirPager<Key, Item>,
    modifier: Modifier = Modifier,
    indicators: IndicatorsScope.() -> Unit = {},
    externals: ExternalItemsScope<Item>.() -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    grouping: GroupedItemsDsl<Item, GroupKey>.() -> Unit,
    itemContent: @Composable LazyItemScope.(Item) -> Unit,
) {
    val paginationState by pager.state.collectAsState()

    PagerEffect(pager, state)

    PaginatedLazyRow(
        paginationState = paginationState,
        modifier = modifier,
        indicators = indicators,
        externals = externals,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        grouping = grouping,
        itemContent = itemContent,
    )
}
