# Paginaut

Pagination for Kotlin Multiplatform. Handles paginated data loading with built-in UI components for Jetpack Compose and SwiftUI.

## Modules

| Module | Description | Platforms |
|---|---|---|
| `paginaut-core` | Core pagination logic | Android, JVM, iOS, macOS, JS, Wasm, Linux, Windows |
| `paginaut-compose` | Compose UI components | Android, JVM (Desktop), iOS, Wasm |
| `paginaut-swiftui` | SwiftUI components | iOS 16+, macOS 13+ |

## Installation

### Gradle (JitPack)

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

Add the dependencies:

```kotlin
dependencies {
    implementation("com.aenadgrleey.paginaut:paginaut-core:0.1.0")
    implementation("com.aenadgrleey.paginaut:paginaut-compose:0.1.0") // for Compose
}
```

### Swift Package Manager

Add the `paginaut-swiftui` package to your Xcode project or `Package.swift`.

## Usage

### Core

Create a pager with a load function that returns pages of data:

```kotlin
val pager = SimplePager<Int, Item>(
    pageSize = 20,
    prefetchDistance = 5,
    initialKey = 0,
) { key ->
    val response = api.getItems(page = key ?: 0)
    Page(
        items = response.items,
        nextKey = response.nextPage,
        prevKey = null,
    )
}

pager.init()

// Collect state
pager.state.collect { state ->
    val items = state.items       // List<Item>
    val loading = state.forward   // LoadStatus
}
```

For bidirectional pagination, use `Pager` directly:

```kotlin
val pager = Pager<Int, Item>(
    pageSize = 20,
    initialKey = 0,
) { params ->
    val response = api.getItems(page = params.key ?: 0)
    Page(
        items = response.items,
        nextKey = response.nextPage,
        prevKey = response.prevPage,
    )
}
```

### Compose

Drop-in paginated lists and grids with customizable indicators:

```kotlin
@Composable
fun ItemList(pager: Pager<Int, Item>) {
    PaginatedLazyColumn(
        pager = pager,
        refreshIndicator = { CircularProgressIndicator() },
        refreshErrorIndicator = { error -> Text("Error: ${error.message}") },
        emptyIndicator = { Text("No items") },
        forwardLoadingIndicator = { CircularProgressIndicator() },
        forwardErrorIndicator = { error -> Text("Load failed") },
    ) { item ->
        ItemRow(item)
    }
}
```

Also available: `PaginatedLazyRow`, `PaginatedLazyVerticalGrid`, `PaginatedLazyHorizontalGrid`, `PaginatedLazyVerticalStaggeredGrid`, `PaginatedLazyHorizontalStaggeredGrid`.

#### Viewport State (auto-filling state indicators in grids/lists)

When you have full-span header items above paginated content, first-page state indicators (loading, error, empty) should fill the remaining viewport space. `GridViewportState` / `ListViewportState` handle this automatically — no manual height tracking needed:

```kotlin
@Composable
fun FeedScreen(pager: Pager<Int, CarModel>) {
    val gridState = rememberLazyGridState()
    val viewport = rememberGridViewport(gridState)
    val paginationState by pager.state.collectAsState()

    PagerEffect(pager, gridState)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Mark full-span headers with Modifier.extensive(viewport)
        item(span = { GridItemSpan(maxLineSpan) }) {
            FiltersHeader(modifier = Modifier.extensive(viewport))
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SortingHeader(modifier = Modifier.extensive(viewport))
        }

        // Pass viewport to state indicators — they auto-fill remaining space
        with(paginationState) {
            items(key = { it.id }) { item -> ItemCard(item) }
            firstPageLoading(viewport) { CircularProgressIndicator() }
            firstPageError(viewport) { error -> Text("Error: ${error.message}") }
            empty(viewport) { Text("No items") }
            forwardLoading { CircularProgressIndicator() }
            forwardError { Text("Load failed") }
        }
    }
}
```

The same pattern works for lists with `ListViewportState` / `rememberListViewport(listState)`.

### SwiftUI

```swift
PaginatedList(
    pager: pager,
    indicators: PaginationIndicators()
        .refreshIndicator { ProgressView() }
        .refreshErrorIndicator { error in Text("Error: \(error)") }
        .emptyIndicator { Text("No items") }
        .forwardLoadingIndicator { ProgressView() }
        .forwardErrorIndicator { error in Text("Load failed") }
) { item in
    ItemRow(item: item)
}
```

Also available: `PaginatedScrollRow`, `PaginatedVGrid(columns:)`, `PaginatedHGrid(rows:)`.

**Supporting types:**

- **`PagerState<Item>`** — `ObservableObject` that bridges Kotlin `StateFlow<PaginationState>` to SwiftUI via `@Published`. Use with `@StateObject` or `@ObservedObject`.
- **`PaginationIndicators`** — Builder-style configuration for all 8 indicator views (refresh, error, empty, forward/backward loading/error, end reached). Defaults to `EmptyView` for each.
- **`VisibleRangeTracker`** — Tracks visible item indices via `onAppear`/`onDisappear` to drive prefetching.

## API Overview

### Core Types

- **`Pager<Key, Item>`** — Main interface. Exposes `state: StateFlow<PaginationState>`, plus `refresh()`, `retry(direction)`, `jumpTo(key)`, `update(block)`, and `close()`.
- **`SimplePager<Key, Item>`** — Forward-only pager with a simplified `retry()`.
- **`PaginationState<Item>`** — Holds `items: List<Item>` and load statuses (`forward`, `backward`, `refresh`).
- **`LoadStatus`** — `Idle`, `Loading`, `Error(cause)`, or `EndReached`.
- **`Page<Key, Item>`** — A single page result: `items`, `nextKey`, `prevKey`.
- **`LoadParams<Key>`** — Passed to your load function: `key`, `direction`, `pageSize`.

### Compose Types

- **`GridViewportState`** — Tracks extensive (full-span) items' heights in a `LazyVerticalGrid` and computes remaining viewport space for state indicators. Created via `rememberGridViewport(gridState)`.
- **`ListViewportState`** — Same for `LazyColumn`/`LazyRow`. Created via `rememberListViewport(listState)`.
- **`Modifier.extensive(viewport)`** — Apply to full-span header items to track their height.


## License

Apache 2.0
