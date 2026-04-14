# SwiftUI Compatibility Demo

This demo proves the generated SwiftUI integration compiles against a consumer-built
Kotlin framework named `Shared`, not the library's original `PaginautCore` module name.

## What It Covers

- `swiftui-compat-demo-shared` builds a `Shared.xcframework` that re-exports `:paginaut-core`.
- `generateSwiftUiSources -Ppaginaut.swift.module=Shared` rewrites the SwiftUI sources to import `Shared`.
- The demo app source in `demo/swiftui-compat/App` imports `Shared` and uses `PaginatedList`.

## Verify

Run:

```bash
./demo/swiftui-compat/verify.sh
```

That command:

1. Builds `Shared.xcframework`
2. Regenerates the SwiftUI wrapper sources for the `Shared` module
3. Type-checks the demo app sources plus the generated wrappers against the built iOS simulator framework
