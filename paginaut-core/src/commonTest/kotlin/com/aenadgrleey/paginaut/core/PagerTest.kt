package com.aenadgrleey.paginaut.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PagerTest {

    /**
     * Regression: a single loadJob was shared between forward and backward.
     * Scrolling (onVisibleRangeChanged) cancelled the job, so a forward load
     * in flight would be killed when a backward load was triggered, and vice versa.
     *
     * After the fix, each direction has its own job — both loads must complete.
     */
    @Test
    fun forwardAndBackwardLoads_doNotCancelEachOther() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val completedDirections = mutableListOf<Direction>()

        val pager = Pager<Int, String>(
            pageSize = 5,
            prefetchDistance = 2,
            initialKey = 50,
            coroutineContext = dispatcher,
        ) { params ->
            when (params.direction) {
                Direction.Init -> Page(
                    items = (50..54).map { "item-$it" },
                    nextKey = 55,
                    prevKey = 49,
                )

                Direction.Forward -> {
                    completedDirections += Direction.Forward
                    Page(
                        items = (55..59).map { "item-$it" },
                        nextKey = 60,
                        prevKey = null,
                    )
                }

                Direction.Backward -> {
                    completedDirections += Direction.Backward
                    Page(
                        items = (45..49).map { "item-$it" },
                        nextKey = null,
                        prevKey = 44,
                    )
                }
            }
        }

        // Init
        pager.init()
        advanceUntilIdle()
        assertEquals(5, pager.state.value.items.size)

        // Trigger both forward AND backward prefetch:
        // firstVisible near 0 → backward, lastVisible near end → forward
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 0, lastVisible = 4))
        advanceUntilIdle()

        // Both loads must have completed — neither cancelled the other
        assertEquals(
            setOf(Direction.Forward, Direction.Backward),
            completedDirections.toSet(),
            "Both forward and backward loads must complete"
        )
        assertEquals(15, pager.state.value.items.size)
        assertEquals("item-45", pager.state.value.items.first())
        assertEquals("item-59", pager.state.value.items.last())
    }

    /**
     * Rapid scroll events must not cancel an in-flight load of the same direction.
     */
    @Test
    fun rapidScrolls_doNotCancelInFlightForwardLoad() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val forwardGate = CompletableDeferred<Unit>()
        var forwardLoadStarted = 0

        val pager = Pager<Int, String>(
            pageSize = 5,
            prefetchDistance = 2,
            initialKey = 0,
            coroutineContext = dispatcher,
        ) { params ->
            when (params.direction) {
                Direction.Init -> Page(
                    items = (0..4).map { "item-$it" },
                    nextKey = 5,
                    prevKey = null,
                )

                Direction.Forward -> {
                    forwardLoadStarted++
                    forwardGate.await()
                    Page(
                        items = (5..9).map { "item-$it" },
                        nextKey = 10,
                        prevKey = null,
                    )
                }

                Direction.Backward -> Page(items = emptyList(), nextKey = null, prevKey = null)
            }
        }

        pager.init()
        advanceUntilIdle()

        // First scroll triggers forward load
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 2, lastVisible = 4))
        advanceUntilIdle()
        assertEquals(LoadStatus.Loading, pager.state.value.forward)

        // Rapid scrolls while load is in-flight — must NOT restart
        repeat(10) {
            pager.onVisibleRangeChanged(VisibleRange(firstVisible = 2, lastVisible = 4))
        }
        advanceUntilIdle()

        // Only one forward load should have started
        assertEquals(1, forwardLoadStarted)

        // Let it finish
        forwardGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(10, pager.state.value.items.size)
        assertEquals(LoadStatus.Idle, pager.state.value.forward)
    }

    /**
     * Verify that refresh still cancels both direction jobs.
     */
    @Test
    fun refresh_cancelsBothDirectionJobs() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val forwardGate = CompletableDeferred<Unit>()
        var forwardCompleted = false

        val pager = Pager<Int, String>(
            pageSize = 5,
            prefetchDistance = 2,
            initialKey = 0,
            coroutineContext = dispatcher,
        ) { params ->
            when (params.direction) {
                Direction.Init -> Page(
                    items = (0..4).map { "item-$it" },
                    nextKey = 5,
                    prevKey = null,
                )

                Direction.Forward -> {
                    forwardGate.await()
                    forwardCompleted = true
                    Page(
                        items = (5..9).map { "item-$it" },
                        nextKey = 10,
                        prevKey = null,
                    )
                }

                Direction.Backward -> Page(items = emptyList(), nextKey = null, prevKey = null)
            }
        }

        pager.init()
        advanceUntilIdle()

        // Start forward load
        pager.onVisibleRangeChanged(VisibleRange(firstVisible = 2, lastVisible = 4))
        advanceUntilIdle()
        assertEquals(LoadStatus.Loading, pager.state.value.forward)

        // Refresh should cancel in-flight forward
        pager.refresh()
        forwardGate.complete(Unit) // unblock, but job was cancelled
        advanceUntilIdle()

        // Forward load should NOT have completed (was cancelled by refresh)
        assertEquals(false, forwardCompleted)
        // State should be fresh from refresh
        assertEquals(5, pager.state.value.items.size)
    }
}
