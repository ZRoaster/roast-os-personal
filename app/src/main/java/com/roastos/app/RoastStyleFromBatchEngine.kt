package com.roastos.app

data class RoastStyleCreateResult(
    val success: Boolean,
    val style: RoastStyleProfile?,
    val message: String
)

object RoastStyleFromBatchEngine {

    fun createFromBatch(
        batchId: String,
        styleName: String
    ): RoastStyleCreateResult {

        val entry = RoastHistoryEngine.findByBatchId(batchId)
            ?: return RoastStyleCreateResult(
                success = false,
                style = null,
                message = "Batch not found: $batchId"
            )

        val generated = MyStyleEngine.createFromBatch(
            batchId = batchId,
            newName = styleName
        ) ?: return RoastStyleCreateResult(
            success = false,
            style = null,
            message = "Failed to generate style from batch: $batchId"
        )

        val enriched = generated.copy(
            description = buildDescription(entry),
            flavorGoal = buildFlavorGoal(entry, generated.flavorGoal),
            notes = buildNotes(entry, generated.notes)
        )

        MyStyleEngine.save(enriched)

        return RoastStyleCreateResult(
            success = true,
            style = enriched,
            message = "Style created from batch $batchId"
        )
    }

    fun suggestStyleName(
        batchId: String
    ): String {
        val entry = RoastHistoryEngine.findByBatchId(batchId)
            ?: return "My Style"

        val evaluation = entry.evaluation
        val process = entry.process.ifBlank { "Roast" }

        val tags = mutableListOf<String>()

        if ((evaluation?.flavorClarity ?: 0) >= 7) tags.add("Clarity")
        if ((evaluation?.sweetness ?: 0) >= 7) tags.add("Sweet")
        if ((evaluation?.body ?: 0) >= 7) tags.add("Body")
        if ((evaluation?.acidity ?: 0) >= 7) tags.add("Bright")

        val suffix = if (tags.isEmpty()) {
            "Profile"
        } else {
            tags.joinToString(" ")
        }

        return "$process $suffix"
    }

    private fun buildDescription(
        entry: RoastHistoryEntry
    ): String {
        val parts = mutableListOf<String>()

        if (entry.process.isNotBlank()) {
            parts.add("Based on ${entry.process}")
        }

        entry.actualDropSec?.let {
            parts.add("Drop ${formatSec(it)}")
        }

        entry.actualFcSec?.let {
            parts.add("FC ${formatSec(it)}")
        }

        return if (parts.isEmpty()) {
            "Generated from roast batch ${entry.batchId}"
        } else {
            parts.joinToString(" · ")
        }
    }

    private fun buildFlavorGoal(
        entry: RoastHistoryEntry,
        fallback: String
    ): String {
        val e = entry.evaluation ?: return fallback

        val parts = mutableListOf<String>()

        if ((e.flavorClarity ?: 0) >= 7) parts.add("高清晰度")
        if ((e.sweetness ?: 0) >= 7) parts.add("高甜感")
        if ((e.body ?: 0) >= 7) parts.add("较高醇厚")
        if ((e.acidity ?: 0) >= 7) parts.add("明亮酸质")
        if ((e.balance ?: 0) >= 7) parts.add("平衡")

        return if (parts.isEmpty()) fallback else parts.joinToString(" / ")
    }

    private fun buildNotes(
        entry: RoastHistoryEntry,
        fallback: String?
    ): String {
        val e = entry.evaluation

        val base = mutableListOf<String>()
        base.add("Created from batch ${entry.batchId}")

        if (!e?.notes.isNullOrBlank()) {
            base.add("Cup notes: ${e?.notes}")
        }

        if ((entry.roastHealthHeadline).isNotBlank()) {
            base.add("Roast health: ${entry.roastHealthHeadline}")
        }

        return listOfNotNull(
            fallback,
            base.joinToString("\n")
        ).joinToString("\n")
    }

    private fun formatSec(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }
}
