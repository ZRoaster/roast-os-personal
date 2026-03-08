package com.roastos.app

data class RoastAnchorSet(
    var turningSec: Int? = null,
    var yellowSec: Int? = null,
    var fcSec: Int? = null,
    var dropSec: Int? = null
)

data class RoastTimeline(
    var predicted: RoastAnchorSet = RoastAnchorSet(),
    var actual: RoastAnchorSet = RoastAnchorSet(),

    var currentPhase: String = "Idle",
    var currentRor: Double? = null,

    var devSec: Int? = null,
    var dtrPercent: Double? = null
) {

    fun reset() {
        predicted = RoastAnchorSet()
        actual = RoastAnchorSet()
        currentPhase = "Idle"
        currentRor = null
        devSec = null
        dtrPercent = null
    }

    fun syncPredicted(
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?
    ) {
        predicted.turningSec = turningSec
        predicted.yellowSec = yellowSec
        predicted.fcSec = fcSec
        predicted.dropSec = dropSec
        recalcDevelopment()
    }

    fun syncActual(
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?,
        ror: Double?
    ) {
        actual.turningSec = turningSec
        actual.yellowSec = yellowSec
        actual.fcSec = fcSec
        actual.dropSec = dropSec
        currentRor = ror
        currentPhase = detectPhase()
        recalcDevelopment()
    }

    fun updateActualTurning(sec: Int?) {
        actual.turningSec = sec
        currentPhase = detectPhase()
        recalcDevelopment()
    }

    fun updateActualYellow(sec: Int?) {
        actual.yellowSec = sec
        currentPhase = detectPhase()
        recalcDevelopment()
    }

    fun updateActualFc(sec: Int?) {
        actual.fcSec = sec
        currentPhase = detectPhase()
        recalcDevelopment()
    }

    fun updateActualDrop(sec: Int?) {
        actual.dropSec = sec
        currentPhase = detectPhase()
        recalcDevelopment()
    }

    fun updateRor(ror: Double?) {
        currentRor = ror
        currentPhase = detectPhase()
    }

    fun hasPredictedBaseline(): Boolean {
        return predicted.turningSec != null ||
            predicted.yellowSec != null ||
            predicted.fcSec != null ||
            predicted.dropSec != null
    }

    fun hasActualData(): Boolean {
        return actual.turningSec != null ||
            actual.yellowSec != null ||
            actual.fcSec != null ||
            actual.dropSec != null
    }

    fun predictedDevSec(): Int? {
        val fc = predicted.fcSec
        val drop = predicted.dropSec
        return if (fc != null && drop != null && drop > fc) drop - fc else null
    }

    fun actualDevSec(): Int? {
        val fc = actual.fcSec
        val drop = actual.dropSec
        return if (fc != null && drop != null && drop > fc) drop - fc else null
    }

    fun predictedDtrPercent(): Double? {
        val fc = predicted.fcSec
        val drop = predicted.dropSec
        if (fc == null || drop == null || drop <= fc || drop <= 0) return null
        return ((drop - fc).toDouble() / drop.toDouble()) * 100.0
    }

    fun actualDtrPercent(): Double? {
        val fc = actual.fcSec
        val drop = actual.dropSec
        if (fc == null || drop == null || drop <= fc || drop <= 0) return null
        return ((drop - fc).toDouble() / drop.toDouble()) * 100.0
    }

    fun detectPhase(): String {
        return when {
            actual.dropSec != null -> "Finished"
            actual.fcSec != null -> "Development"
            actual.yellowSec != null -> "Maillard / Pre-FC"
            actual.turningSec != null -> "Drying"
            else -> "Idle"
        }
    }

    fun summary(): String {
        return """
Timeline

Predicted
Turning ${predicted.turningSec?.toString() ?: "-"}
Yellow ${predicted.yellowSec?.toString() ?: "-"}
FC ${predicted.fcSec?.toString() ?: "-"}
Drop ${predicted.dropSec?.toString() ?: "-"}

Actual
Turning ${actual.turningSec?.toString() ?: "-"}
Yellow ${actual.yellowSec?.toString() ?: "-"}
FC ${actual.fcSec?.toString() ?: "-"}
Drop ${actual.dropSec?.toString() ?: "-"}

Phase
$currentPhase

ROR
${currentRor?.let { "%.1f".format(it) } ?: "-"}

Dev
${devSec?.toString() ?: "-"}

DTR
${dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}
        """.trimIndent()
    }

    private fun recalcDevelopment() {
        val actualDev = actualDevSec()
        val predictedDev = predictedDevSec()

        devSec = actualDev ?: predictedDev

        dtrPercent = when {
            actualDtrPercent() != null -> actualDtrPercent()
            predictedDtrPercent() != null -> predictedDtrPercent()
            else -> null
        }
    }
}

object RoastTimelineStore {
    val current = RoastTimeline()

    fun reset() {
        current.reset()
    }

    fun syncPredicted(
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?
    ) {
        current.syncPredicted(
            turningSec = turningSec,
            yellowSec = yellowSec,
            fcSec = fcSec,
            dropSec = dropSec
        )
    }

    fun syncActual(
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?,
        ror: Double?
    ) {
        current.syncActual(
            turningSec = turningSec,
            yellowSec = yellowSec,
            fcSec = fcSec,
            dropSec = dropSec,
            ror = ror
        )
    }
}
