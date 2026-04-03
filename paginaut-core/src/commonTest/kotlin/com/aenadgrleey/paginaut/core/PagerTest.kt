package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PagerTest {
    @Test
    fun simplePagerLoadsFirstPage() = runTest {
        val pager = SimplePager<Int, String>(pageSize = 2) { key ->
            val start = key ?: 0
            Page(
                items = listOf("item-$start", "item-${start + 1}"),
                key = start + 2,
            )
        }
        pager.init()
        val state = pager.state.first { it.items.isNotEmpty() }
        assertEquals(listOf("item-0", "item-1"), state.items)
    }
}
