package com.linote

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class NewestFollowTest {
    private val older = UUID.fromString("00000000-0000-7000-8000-000000000001")
    private val newer = UUID.fromString("00000000-0000-7000-8000-000000000002")

    @Test fun changedHead_jumpsEvenAfterLeavingTheEdge() {
        assertEquals(
            NewestFollow.Jump,
            newestFollow(armed = true, armedHead = older, head = newer, atNewestEdge = false),
        )
    }

    @Test fun scrolledAwayBeforeTheHeadChanges_cancels() {
        assertEquals(
            NewestFollow.Cancel,
            newestFollow(armed = true, armedHead = older, head = older, atNewestEdge = false),
        )
    }

    @Test fun stillAtTheEdgeWithTheSameHead_waits() {
        assertEquals(
            NewestFollow.Wait,
            newestFollow(armed = true, armedHead = older, head = older, atNewestEdge = true),
        )
    }

    @Test fun disarmed_ignoresANewHead() {
        assertEquals(
            NewestFollow.Idle,
            newestFollow(armed = false, armedHead = null, head = newer, atNewestEdge = true),
        )
    }

    @Test fun emptyListStillAtTheEdge_waits() {
        assertEquals(
            NewestFollow.Wait,
            newestFollow(armed = true, armedHead = null, head = null, atNewestEdge = true),
        )
    }

    @Test fun firstNoteOnAnEmptyList_jumps() {
        assertEquals(
            NewestFollow.Jump,
            newestFollow(armed = true, armedHead = null, head = newer, atNewestEdge = false),
        )
    }
}
