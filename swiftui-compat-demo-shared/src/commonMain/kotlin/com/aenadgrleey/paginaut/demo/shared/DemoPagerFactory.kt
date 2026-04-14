package com.aenadgrleey.paginaut.demo.shared

import com.aenadgrleey.paginaut.core.BidirPager
import com.aenadgrleey.paginaut.core.Direction
import com.aenadgrleey.paginaut.core.Page

data class DemoArticle(
    val id: String,
    val title: String,
    val subtitle: String,
)

object DemoPagerFactory {
    fun makePager(): BidirPager<String, DemoArticle> =
        BidirPager(
            pageSize = PAGE_SIZE,
            initialKey = pageKey(0),
        ) { params ->
            val pageIndex = when (params.direction) {
                Direction.Init -> params.initKey?.toPageIndex() ?: 0
                Direction.Forward -> params.forwardKey?.toPageIndex() ?: demoPages.size
                Direction.Backward -> params.backwardKey?.toPageIndex() ?: -1
            }

            val items = demoPages.getOrNull(pageIndex).orEmpty()

            Page(
                items = items,
                nextKey = pageIndex
                    .takeIf { it >= 0 && it < demoPages.lastIndex }
                    ?.let { pageKey(it + 1) },
                prevKey = pageIndex
                    .takeIf { it > 0 && it <= demoPages.lastIndex }
                    ?.let { pageKey(it - 1) },
                endReached = when (params.direction) {
                    Direction.Init, Direction.Forward -> items.isEmpty() || pageIndex >= demoPages.lastIndex
                    Direction.Backward -> items.isEmpty() || pageIndex <= 0
                },
            )
        }.apply { init() }

    private val demoPages = listOf(
        listOf(
            DemoArticle("1", "Inbox", "Bidirectional pager exported through Shared"),
            DemoArticle("2", "Updates", "SwiftUI sources generated for the consumer module"),
            DemoArticle("3", "Mentions", "Compiles without importing PaginautCore directly"),
        ),
        listOf(
            DemoArticle("4", "Builds", "The verification script regenerates Swift wrappers"),
            DemoArticle("5", "Type Checks", "SwiftUI app sources compile against Shared.xcframework"),
            DemoArticle("6", "Proves", "The generated integration is source-compatible"),
        ),
    )

    private fun pageKey(index: Int): String = "page:$index"

    private fun String.toPageIndex(): Int? = substringAfter("page:", missingDelimiterValue = "").toIntOrNull()

    private const val PAGE_SIZE = 3
}
