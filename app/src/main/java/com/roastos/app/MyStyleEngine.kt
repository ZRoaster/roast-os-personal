package com.roastos.app

object MyStyleEngine {

    private val styles = mutableListOf<RoastStyleProfile>()

    fun all(): List<RoastStyleProfile> {
        return styles.toList()
    }

    fun count(): Int {
        return styles.size
    }

    fun findById(id: String): RoastStyleProfile? {
        return styles.firstOrNull { it.id == id }
    }

    fun exists(id: String): Boolean {
        return styles.any { it.id == id }
    }

    fun save(style: RoastStyleProfile): Boolean {
        val index = styles.indexOfFirst { it.id == style.id }

        return if (index >= 0) {
            styles[index] = style
            true
        } else {
            styles.add(style)
            true
        }
    }

    fun delete(id: String): Boolean {
        return styles.removeAll { it.id == id }
    }

    fun clear() {
        styles.clear()
    }

    fun createManualStyle(
        name: String,
        description: String,
        flavorGoal: String,
        suitableProcess: String?,
        turningTargetSec: Int?,
        yellowTargetSec: Int?,
        firstCrackTargetSec: Int?,
        dropTargetSec: Int?,
        developmentRatio: Double?,
        rorTrend: String?,
        airflowStrategy: String?,
        drumStrategy: String?,
        notes: String?
    ): RoastStyleProfile {
        return RoastStyleProfile(
            id = buildId(name),
            name = name,
            description = description,
            origin = "my_style",
            flavorGoal = flavorGoal,
            suitableProcess = suitableProcess,
            turningTargetSec = turningTargetSec,
            yellowTargetSec = yellowTargetSec,
            firstCrackTargetSec = firstCrackTargetSec,
            dropTargetSec = dropTargetSec,
            developmentRatio = developmentRatio,
            rorTrend = rorTrend,
            airflowStrategy = airflowStrategy,
            drumStrategy = drumStrategy,
            notes = notes
        )
    }

    fun createFromBuiltIn(
        baseStyleId: String,
        newName: String,
        notes: String? = null
    ): RoastStyleProfile? {
        val base = RoastStyleEngine.findById(baseStyleId) ?: return null

        return base.copy(
            id = buildId(newName),
            name = newName,
            origin = "my_style",
            notes = mergeNotes(base.notes, notes)
        )
    }

    fun createFromBatch(
        batchId: String,
        newName: String
    ): RoastStyleProfile? {

        val entry = RoastHistoryEngine.findByBatchId(batchId) ?: return null
        val log = buildLogLikeTargets(entry)
        val flavorGoal = buildFlavorGoal(entry)

        return RoastStyleProfile(
            id = buildId(newName),
            name = newName,
            description = "Generated from roast batch $batchId",
            origin = "my_style",
            flavorGoal = flavorGoal,
            suitableProcess = entry.process.ifBlank { null },
            turningTargetSec = entry.actualTurningSec ?: entry.predictedTurningSec,
            yellowTargetSec = entry.actualYellowSec ?: entry.predictedYellowSec,
            firstCrackTargetSec = entry.actualFcSec ?: entry.predictedFcSec,
            dropTargetSec = entry.actualDropSec ?: entry.predictedDropSec,
            developmentRatio = log.developmentRatio,
            rorTrend = inferRorTrend(entry),
            airflowStrategy = inferAirflowStrategy(entry),
            drumStrategy = inferDrumStrategy(entry),
            notes = "Created from batch ${entry.batchId}"
        )
    }

    fun summary(): String {
        if (styles.isEmpty()) {
            return """
My Styles

Count
0

Status
No saved custom styles
            """.trimIndent()
        }

        val latest = styles.last()

        return """
My Styles

Count
${styles.size}

Latest
${latest.name}

Origin
${latest.origin}

Flavor Goal
${latest.flavorGoal}
        """.trimIndent()
    }

    private fun buildId(name: String): String {
        val normalized = name
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

        val suffix = System.currentTimeMillis().toString().takeLast(6)

        return if (normalized.isBlank()) {
            "my_style_$suffix"
        } else {
            "${normalized}_$suffix"
        }
    }

    private fun mergeNotes(
        base: String?,
        extra: String?
    ): String? {
        return when {
            base.isNullOrBlank() && extra.isNullOrBlank() -> null
            base.isNullOrBlank() -> extra
            extra.isNullOrBlank() -> base
            else -> "$base\n$extra"
        }
    }

    private fun buildFlavorGoal(
        entry: RoastHistoryEntry
    ): String {
        val e = entry.evaluation

        if (e == null) {
            return "Generated from roast history"
        }

        val parts = mutableListOf<String>()

        if ((e.acidity ?: 0) >= 7) parts.add("明亮酸质")
        if ((e.sweetness ?: 0) >= 7) parts.add("高甜感")
        if ((e.body ?: 0) >= 7) parts.add("较高醇厚")
        if ((e.flavorClarity ?: 0) >= 7) parts.add("高清晰度")
        if ((e.balance ?: 0) >= 7) parts.add("平衡")

        return if (parts.isEmpty()) {
            "Generated from roast history"
        } else {
            parts.joinToString(" / ")
        }
    }

    private fun inferRorTrend(
        entry: RoastHistoryEntry
    ): String {
        val ror = entry.actualPreFcRor ?: return "Unknown"

        return when {
            ror < 3.0 -> "Low Support"
            ror < 6.0 -> "Stable Declining"
            ror < 9.0 -> "Energetic"
            else -> "High Energy"
        }
    }

    private fun inferAirflowStrategy(
        entry: RoastHistoryEntry
    ): String {
        return when {
            (entry.actualFcSec ?: 0) > 0 && (entry.actualDropSec ?: 0) - (entry.actualFcSec ?: 0) <= 70 ->
                "Clean Exhaust Late"
            else ->
                "Progressive"
        }
    }

    private fun inferDrumStrategy(
        entry: RoastHistoryEntry
    ): String {
        return when {
            (entry.density) >= 820.0 -> "Supportive"
            (entry.moisture) >= 11.0 -> "Moderate"
            else -> "Stable"
        }
    }

    private fun buildLogLikeTargets(
        entry: RoastHistoryEntry
    ): RoastLog {
        val dropSec = entry.actualDropSec ?: entry.predictedDropSec ?: 0
        val fcSec = entry.actualFcSec ?: entry.predictedFcSec
        val developmentTime =
            if (fcSec != null && dropSec >= fcSec) dropSec - fcSec else null
        val developmentRatio =
            if (developmentTime != null && dropSec > 0) {
                developmentTime.toDouble() / dropSec.toDouble()
            } else {
                null
            }

        return RoastLog(
            batchId = entry.batchId,
            machineName = "HB M2SE",
            status = entry.batchStatus,
            totalTimeSec = dropSec,
            chargeTemp = null,
            dropTemp = null,
            turningPointSec = entry.actualTurningSec ?: entry.predictedTurningSec,
            turningPointTemp = null,
            dryEndSec = entry.actualYellowSec ?: entry.predictedYellowSec,
            dryEndTemp = null,
            maillardStartSec = entry.actualYellowSec ?: entry.predictedYellowSec,
            maillardStartTemp = null,
            firstCrackSec = fcSec,
            firstCrackTemp = null,
            dropSec = dropSec,
            developmentTimeSec = developmentTime,
            developmentRatio = developmentRatio,
            finalRor = entry.actualPreFcRor,
            summary = ""
        )
    }
}
