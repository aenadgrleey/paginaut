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

### SimpleOffsetPager

For APIs with offset-based pagination:

```kotlin
val pager = SimpleOffsetPager<Article>(pageSize = 20) { offset ->
    api.getArticles(offset = offset, limit = 20)
}

pager.init()

// Collect state
pager.state.collect { state ->
    val items = state.items         // List<Article>
    val status = state.loadStatus   // LoadStatus
}
```

### SimpleIdPager

For cursor/id-based APIs that paginate by last item id:

```kotlin
val pager = SimpleIdPager<Long, Message>(
    idSelector = { it.id },
    pageSize = 30,
) { afterId ->
    api.getMessages(afterId = afterId, limit = 30)
}
```

### SimplePager

For custom key-based forward-only pagination:

```kotlin
val pager = SimplePager<String, Post>(
    pageSize = 25,
    initialKey = null,
) { cursor ->
    val response = api.getPosts(cursor = cursor, limit = 25)
    SimplePage(
        items = response.posts,
        nextKey = response.nextCursor, // null signals end of list
    )
}
```

### Pager (bidirectional)

For bidirectional pagination (e.g. chat starting from a specific message):

```kotlin
val pager = Pager<Long, ChatMessage>(
    pageSize = 20,
    initialKey = targetMessageId, // start loading from this point
) { params ->
    params.nextPage(
        items = api.getMessages(
            aroundId = params.initKey,    // non-null only on Init
            afterId = params.forwardKey,  // non-null only on Forward
            beforeId = params.backwardKey, // non-null only on Backward
            limit = params.pageSize,
        ),
        nextKey = { items.lastOrNull()?.id },
        prevKey = { items.firstOrNull()?.id },
    )
}

// Jump to a different position
pager.jumpTo(key = otherMessageId)
```

### State management

```kotlin
// Refresh (reload from initial key)
pager.refresh()

// Retry after error
pager.retry(Direction.Forward) // Pager
pager.retry()                  // SimplePager

// Update items in-place (e.g. toggle favorite)
pager.update { items ->
    items.map { if (it.id == targetId) it.copy(isFavorite = true) else it }
}

// Cleanup
pager.close()
```

### Compose

Drop-in paginated lists and grids with customizable indicators:

```kotlin
@Composable
fun ItemList(pager: Pager<Int, Item>) {
    PaginatedLazyColumn(
        pager = pager,
        indicators = {
            initStateIndicator {
                placeholders(count = 6) { index ->
                    ItemSkeletonRow(index = index)
                }
                loading { CircularProgressIndicator() }
                error { error -> Text("Error: ${error.message}") }
                empty { Text("No items") }
            }
            forwardStateIndicator {
                loading { CircularProgressIndicator() }
                error { _ -> Text("Load failed") }
                empty { Text("End reached") }
            }
            backwardStateIndicator {
                error { _ -> Text("Load failed") }
            }
        },
    ) { item ->
        ItemRow(item)
    }
}
```

Also available: `PaginatedLazyRow`, `PaginatedLazyVerticalGrid`, `PaginatedLazyHorizontalGrid`, `PaginatedLazyVerticalStaggeredGrid`, `PaginatedLazyHorizontalStaggeredGrid`.

Grouped rendering for list-based composables is available via `grouping`:

```kotlin
PaginatedLazyColumn(
    pager = pager,
    indicators = {
        forwardStateIndicator {
            empty { Text("No more items") }
        }
        backwardStateIndicator {
            empty { Text("Reached start") }
        }
    },
    grouping = {
        groupBy { it.date }
        groupStart(sticky = true) { date, _ -> Text(date.toString()) }
        groupEnd { _, _ -> Divider() }
        key = { it.id }
    },
) { item ->
    ItemRow(item)
}
```

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
- **`SimplePager<Key, Item>`** — Forward-only pager with a simplified `retry()`. Exposes `paginationState: StateFlow<PaginationState>` plus the collapsed `state: StateFlow<SimplePaginationState>`.
- **`PaginationState<Item>`** — Holds `items: List<Item>` and load statuses (`init`, `forward`, `backward`).
- **`SimplePaginationState<Item>`** — Holds `items: List<Item>` and a single `loadStatus`.
- **`LoadStatus`** — `Idle`, `Loading`, `Error(cause)`, or `EndReached`.
- **`Page<Key, Item>`** — A single page result: `items`, `nextKey`, `prevKey`.
- **`LoadParams<Key>`** — Passed to your load function: `key`, `direction`, `pageSize`.


## License

Apache 2.0
