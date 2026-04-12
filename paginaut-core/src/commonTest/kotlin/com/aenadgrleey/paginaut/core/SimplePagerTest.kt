package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimplePagerTest {

    // ===========================================
    // 1. BASIC FUNCTIONALITY
    // ===========================================

    @Test
    fun init_loadsFirstPage() = runTest {
        val pager = SimplePager<Int, String>(
            pageSize = 3,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            val start = key ?: 0
            SimplePage(
                items = (start until start + 3).map { "item-$it" },
                nextKey = start + 3,
            )
        }

        pager.init()
        advanceUntilIdle()

        val state = pager.state.value
        assertEquals(listOf("item-0", "item-1", "item-2"), state.items)
        assertEquals(LoadStatus.Idle, state.loadStatus)
    }

    @Test
    fun paginationState_exposesDirectionalStatuses() = runTest {
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = StandardTestDispatcher(testScheduler),
        ) {
            delay(100)
            SimplePage(items = listOf("item-0", "item-1"), nextKey = null)
        }

        pager.init()
        runCurrent()

        val state = pager.paginationState.value
        assertEquals(LoadStatus.Loading, state.init)
        assertEquals(LoadStatus.Idle, state.forward)
        assertEquals(LoadStatus.Idle, state.backward)

        advanceUntilIdle()
        assertEquals(listOf("item-0", "item-1"), pager.paginationState.value.items)
    }

    @Test
    fun init_calledMultipleTimes_loadsOnlyOnce() = runTest {
        var loadCount = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            loadCount++
            SimplePage(items = listOf("item"), nextKey = null)
        }

        pager.init()
        pager.init()
        pager.init()
        advanceUntilIdle()

        assertEquals(1, loadCount)
    }

    // ===========================================
    // 2. FORWARD LOADING
    // ===========================================

    @Test
    fun forward_isSupported_loadsNextPage() = runTest {
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            val start = key ?: 0
            SimplePage(
                items = listOf("item-$start", "item-${start + 1}"),
                nextKey = start + 2,
            )
        }

        pager.init()
        advanceUntilIdle()

        // Simulate scroll to end - should trigger forward prefetch
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 1))
        advanceUntilIdle()

        val state = pager.state.value
        assertEquals(4, state.items.size)
        assertEquals(listOf("item-0", "item-1", "item-2", "item-3"), state.items)
    }

    @Test
    fun refresh_isSupported_replacesAllItems() = runTest {
        var loadCounter = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            loadCounter++
            SimplePage(
                items = listOf("batch-$loadCounter-item-0", "batch-$loadCounter-item-1"),
                nextKey = 2,
            )
        }

        pager.init()
        advanceUntilIdle()

        val initialItems = pager.state.value.items
        assertEquals(listOf("batch-1-item-0", "batch-1-item-1"), initialItems)

        pager.refresh()
        advanceUntilIdle()

        val state = pager.state.value
        assertEquals(listOf("batch-2-item-0", "batch-2-item-1"), state.items)
    }

    @Test
    fun retry_afterInitError_loadsSuccessfully() = runTest {
        var attempts = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            attempts++
            if (attempts == 1) {
                throw RuntimeException("First attempt fails")
            }
            SimplePage(items = listOf("item"), nextKey = null)
        }

        pager.init()
        advanceUntilIdle()

        assertIs<LoadStatus.Error>(pager.state.value.loadStatus)

        pager.retry()
        advanceUntilIdle()

        assertEquals(listOf("item"), pager.state.value.items)
    }

    // ===========================================
    // 3. CONCURRENT POSITION REPORTS
    // ===========================================

    @Test
    fun rapidPositionReports_handlesCorrectly() = runTest {
        var loadStartCount = 0
        var loadCompleteCount = 0
        val dispatcher = StandardTestDispatcher(testScheduler)
        val pager = SimplePager<Int, String>(
            pageSize = 5,
            prefetchDistance = 2,
            coroutineContext = dispatcher,
        ) { key ->
            loadStartCount++
            delay(100) // Simulate network delay
            loadCompleteCount++
            SimplePage(
                items = (0 until 5).map { "item-$it" },
                nextKey = (key ?: 0) + 5,
            )
        }

        pager.init()
        advanceUntilIdle()

        // Simulate rapid scrolling with many position reports without waiting
        repeat(10) { i ->
            pager.onVisibleRangeChanged(VisibleRange(firstVisible = i, lastVisible = i + 2))
        }
        advanceUntilIdle()

        // With separate jobs, only one forward load should start (guarded by isActive)
        assertTrue(
            loadCompleteCount <= 3,
            "Expected few completed loads, got $loadCompleteCount completions out of $loadStartCount starts"
        )
    }

    // ===========================================
    // 4. ERROR HANDLING
    // ===========================================

    @Test
    fun loadError_setsErrorStatus() = runTest {
        val testError = RuntimeException("Network error")
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            throw testError
        }

        pager.init()
        advanceUntilIdle()

        val state = pager.state.value
        assertIs<LoadStatus.Error>(state.loadStatus)
        assertEquals(testError, (state.loadStatus as LoadStatus.Error).cause)
    }

    @Test
    fun forwardLoadError_setsErrorStatus() = runTest {
        var shouldFail = false
        val testError = RuntimeException("Forward load failed")
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            if (shouldFail && key != null) {
                throw testError
            }
            SimplePage(
                items = listOf("item-${key ?: 0}"),
                nextKey = (key ?: 0) + 1,
            )
        }

        pager.init()
        advanceUntilIdle()

        shouldFail = true
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 0))
        advanceUntilIdle()

        val state = pager.state.value
        assertIs<LoadStatus.Error>(state.loadStatus)
        assertEquals(testError, (state.loadStatus as LoadStatus.Error).cause)
    }

    @Test
    fun retry_afterError_resetsAndLoads() = runTest {
        var attempts = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            attempts++
            if (attempts < 3) {
                throw RuntimeException("Attempt $attempts failed")
            }
            SimplePage(items = listOf("success"), nextKey = null)
        }

        pager.init()
        advanceUntilIdle()
        assertIs<LoadStatus.Error>(pager.state.value.loadStatus)

        // First retry - still fails
        pager.retry()
        advanceUntilIdle()
        assertIs<LoadStatus.Error>(pager.state.value.loadStatus)

        // Second retry - succeeds
        pager.retry()
        advanceUntilIdle()

        assertEquals(listOf("success"), pager.state.value.items)
        assertEquals(3, attempts)
    }

    @Test
    fun error_preservesExistingItems() = runTest {
        var shouldFail = false
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            if (shouldFail) throw RuntimeException("Error")
            SimplePage(
                items = listOf("item-${key ?: 0}", "item-${(key ?: 0) + 1}"),
                nextKey = (key ?: 0) + 2,
            )
        }

        pager.init()
        advanceUntilIdle()
        val initialItems = pager.state.value.items

        shouldFail = true
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 1))
        advanceUntilIdle()

        val state = pager.state.value
        assertIs<LoadStatus.Error>(state.loadStatus)
        // Items should be preserved despite error
        assertEquals(initialItems, state.items)
    }

    // ===========================================
    // 5. END REACHED
    // ===========================================

    @Test
    fun nullNextKey_setsEndReached() = runTest {
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            SimplePage(items = listOf("only-item"), nextKey = null, endReached = true)
        }

        pager.init()
        advanceUntilIdle()

        assertEquals(LoadStatus.EndReached, pager.state.value.loadStatus)
    }

    @Test
    fun endReached_doesNotTriggerMoreLoads() = runTest {
        var loadCount = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            loadCount++
            SimplePage(items = listOf("item-$loadCount"), nextKey = null, endReached = true)
        }

        pager.init()
        advanceUntilIdle()
        assertEquals(1, loadCount)

        // Report position changes - should not trigger more loads
        repeat(5) {
            pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 0))
            advanceUntilIdle()
        }

        assertEquals(1, loadCount, "No additional loads should happen after EndReached")
    }

    // ===========================================
    // 5a. CONTINUE LOADING
    // ===========================================

    @Test
    fun continueLoading_resumesAfterEndReached() = runTest {
        var loadCount = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            loadCount++
            if (loadCount == 1) {
                SimplePage(items = listOf("item-1"), nextKey = 2, endReached = true)
            } else {
                SimplePage(items = listOf("item-2"), nextKey = null, endReached = true)
            }
        }

        pager.init()
        advanceUntilIdle()
        assertEquals(1, loadCount)
        assertEquals(LoadStatus.EndReached, pager.state.value.loadStatus)

        // Resume loading — should use preserved key
        pager.continueForward()
        advanceUntilIdle()

        assertEquals(2, loadCount)
        assertEquals(listOf("item-1", "item-2"), pager.state.value.items)
    }

    @Test
    fun continueLoading_loadsFromAnyNonLoadingState() = runTest {
        var loadCount = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            loadCount++
            SimplePage(items = listOf("item-$loadCount"), nextKey = loadCount + 1)
        }

        pager.init()
        advanceUntilIdle()
        assertEquals(1, loadCount)

        // continueLoading on Idle triggers a load
        pager.continueForward()
        advanceUntilIdle()

        assertEquals(2, loadCount)
    }

    @Test
    fun continueLoading_preservesKeyFromEndReachedPage() = runTest {
        var receivedKey: Int? = null
        var loadCount = 0
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            loadCount++
            receivedKey = key
            SimplePage(items = listOf("item-$loadCount"), nextKey = 42, endReached = true)
        }

        pager.init()
        advanceUntilIdle()
        assertEquals(1, loadCount)

        pager.continueForward()
        advanceUntilIdle()

        assertEquals(2, loadCount)
        assertEquals(42, receivedKey, "Should re-use the key returned by the end-reached page")
    }

    // ===========================================
    // 6. SimpleOffsetPager SPECIFICS
    // ===========================================

    @Test
    fun simpleOffsetPager_calculatesNextOffset() = runTest {
        val receivedOffsets = mutableListOf<Int>()
        val pager = SimpleOffsetPager<String>(
            pageSize = 3,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { offset ->
            receivedOffsets.add(offset)
            (offset until offset + 3).map { "item-$it" }
        }

        pager.init()
        advanceUntilIdle()

        // Trigger next page
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 2))
        advanceUntilIdle()

        assertEquals(listOf(0, 3), receivedOffsets)
    }

    @Test
    fun simpleOffsetPager_detectsEndByPageSize() = runTest {
        val pager = SimpleOffsetPager<String>(
            pageSize = 10,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { offset ->
            // Return fewer items than pageSize - signals end
            if (offset == 0) listOf("item-0", "item-1", "item-2") else emptyList()
        }

        pager.init()
        advanceUntilIdle()

        assertEquals(LoadStatus.EndReached, pager.state.value.loadStatus)
    }

    // ===========================================
    // 7. SimpleIdPager SPECIFICS
    // ===========================================

    @Test
    fun simpleIdPager_usesLastItemId() = runTest {
        data class Item(val id: Int, val name: String)

        val receivedIds = mutableListOf<Int?>()
        val pager = SimpleIdPager<Int, Item>(
            idSelector = { it.id },
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { afterId ->
            receivedIds.add(afterId)
            when (afterId) {
                null -> listOf(Item(1, "first"), Item(2, "second"))
                2 -> listOf(Item(3, "third"), Item(4, "fourth"))
                else -> emptyList()
            }
        }

        pager.init()
        advanceUntilIdle()

        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 1))
        advanceUntilIdle()

        assertEquals(listOf(null, 2), receivedIds)
    }

    // ===========================================
    // 8. UPDATE ITEMS
    // ===========================================

    @Test
    fun update_modifiesItemsInPlace() = runTest {
        val pager = SimplePager<Int, String>(
            pageSize = 3,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { _ ->
            SimplePage(items = listOf("a", "b", "c"), nextKey = null)
        }

        pager.init()
        advanceUntilIdle()

        pager.update { items -> items.map { it.uppercase() } }

        assertEquals(listOf("A", "B", "C"), pager.state.value.items)
    }

    // ===========================================
    // 9. CLOSE
    // ===========================================

    @Test
    fun close_cancelsScope() = runTest {
        var loadAttemptedAfterClose = false
        val pager = SimplePager<Int, String>(
            pageSize = 2,
            prefetchDistance = 1,
            coroutineContext = UnconfinedTestDispatcher(testScheduler),
        ) { key ->
            if (key != null) {
                loadAttemptedAfterClose = true
            }
            SimplePage(items = listOf("item"), nextKey = 1)
        }

        pager.init()
        advanceUntilIdle()
        pager.close()

        // Trigger what would be a load
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 0))
        advanceUntilIdle()

        assertEquals(false, loadAttemptedAfterClose)
    }
}
