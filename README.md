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


## License

Apache 2.0
