package com.roastos.app

data class TimelineRow(
    val label: String,
    val predictedSec: Int,
    val actualSec: Int?,
    val status: String
)

object TimelineEngine {

    fun build(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?
    ): List<TimelineRow> {

        return listOf(
            TimelineRow(
                label = "Turning",
                predictedSec = predTurning,
                actualSec = actualTurning,
                status = statusFor(actualTurning)
            ),
            TimelineRow(
                label = "Yellow",
                predictedSec = predYellow,
                actualSec = actualYellow,
                status = statusFor(actualYellow)
            ),
            TimelineRow(
                label = "First Crack",
                predictedSec = predFc,
                actualSec = actualFc,
                status = statusFor(actualFc)
            ),
            TimelineRow(
                label = "Drop",
                predictedSec = predDrop,
                actualSec = actualDrop,
                status = statusFor(actualDrop)
            )
        )
    }

    private fun statusFor(actual: Int?): String {
        return if (actual == null) "Pending" else "Recorded"
    }
}
