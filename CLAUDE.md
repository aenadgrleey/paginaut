## Build & Test Commands

```bash
./gradlew build                    # Build all modules
./gradlew :paginaut-core:jvmTest   # Fast: run core tests on JVM
./gradlew :paginaut-core:jvmTest --tests "com.aenadgrleey.paginaut.core.PagerTest"           # Single test class
./gradlew :paginaut-core:jvmTest --tests "com.aenadgrleey.paginaut.core.PagerTest.methodName" # Single test method
./gradlew :paginaut-core:allTests  # Core tests on all platforms
./gradlew publishToMavenLocal      # Publish to local Maven (also used by JitPack)
```

No linter configured. No CI — publishing goes through JitPack (`jitpack.yml`).

## Architecture

Two Gradle modules + a Swift package:

- **paginaut-core** — Platform-agnostic pagination. Targets: Android, JVM, JS, WasmJS, iOS, macOS, Linux, Windows.
- **paginaut-compose** — Compose Multiplatform UI. Targets: Android, JVM (Desktop), iOS, WasmJS. Uses `-Xcontext-parameters`.
- **paginaut-swiftui** — Swift Package (iOS 16+, macOS 13+). Not built by Gradle.

### Core (`paginaut-core`)

`Pager<Key, Item>` is the bidirectional interface. `SimplePager<Key, Item>` is forward-only. Factory functions: `Pager()`, `SimplePager()`, `SimpleOffsetPager()`, `SimpleIdPager()`.

`SimplePagerImpl` wraps `PagerImpl` internally — it blocks backward loads and collapses `PaginationState` (3 statuses: init/forward/backward) into `SimplePaginationState` (single `loadStatus`) via `stateIn(Eagerly)`.

`PagerImpl` uses separate `forwardJob`/`backwardJob` coroutines so directions don't cancel each other. A `Mutex` serializes state access. `LoadParams` provides direction-aware key accessors (`initKey`, `forwardKey`, `backwardKey`) — only the one matching the current direction is non-null.

### Compose (`paginaut-compose`)

`PagerEffect` is the bridge — a `@Composable` that observes `LazyListState`/`LazyGridState` via `snapshotFlow` and calls `pager.onVisibleRangeChanged()`.

Each `PaginatedLazy*` component has two overloads: one taking `PaginationState` (stateless), one taking `Pager` (wires up `PagerEffect` + `collectAsState` internally).

Lazy scope extensions (`context(LazyListScope)`) provide `items()`, `firstPageLoading()`, `forwardLoading()`, `backwardError()`, etc. for manual composition. Grid variants use `GridItemSpan(maxLineSpan)` for full-width indicators.

`ListViewportState`/`GridViewportState` + `Modifier.extensive()` track item heights for remaining-space calculation in indicators.