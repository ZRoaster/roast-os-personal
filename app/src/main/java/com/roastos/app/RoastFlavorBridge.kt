package com.roastos.app

data class RoastFlavorBridgeResult(
    val headline: String,
    val flavorDirection: String,
    val priority: List<String>,
    val suggestions: List<String>,
    val summary: String
)

object RoastFlavorBridge {

    fun buildFromBatch(batchId: String): RoastFlavorBridgeResult {
        val entry = RoastHistoryEngine.findByBatchId(batchId)

        if (entry == null) {
            return RoastFlavorBridgeResult(
                headline = "Batch not found",
                flavorDirection = "No flavor direction available",
                priority = listOf("Open a valid batch first"),
                suggestions = listOf("No roast history found for this batch"),
                summary = """
Flavor Bridge

Headline
Batch not found

Flavor Direction
No flavor direction available

Priority
• Open a valid batch first

Suggestions
• No roast history found for this batch
                """.trimIndent()
            )
        }

        return buildFromEntry(entry)
    }

    fun buildFromEntry(entry: RoastHistoryEntry): RoastFlavorBridgeResult {
        val evaluation = entry.evaluation

        if (evaluation == null) {
            return RoastFlavorBridgeResult(
                headline = "Evaluation not saved",
                flavorDirection = "Save roasted bean and cup data first",
                priority = listOf("Add evaluation for this batch"),
                suggestions = listOf(
                    "Enter bean color / ground color / roasted aw",
                    "Score sweetness / acidity / body / clarity / balance",
                    "Then build flavor-direction guidance"
                ),
                summary = """
Flavor Bridge

Headline
Evaluation not saved

Flavor Direction
Save roasted bean and cup data first

Priority
• Add evaluation for this batch

Suggestions
• Enter bean color / ground color / roasted aw
• Score sweetness / acidity / body / clarity / balance
• Then build flavor-direction guidance
                """.trimIndent()
            )
        }

        val priority = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        val sweetness = evaluation.sweetness
        val acidity = evaluation.acidity
        val body = evaluation.body
        val clarity = evaluation.flavorClarity
        val balance = evaluation.balance

        val beanColor = evaluation.beanColor
        val groundColor = evaluation.groundColor
        val roastedAw = evaluation.roastedAw

        if (sweetness != null && sweetness <= 5) {
            priority.add("Increase sweetness")
            suggestions.add("Consider slightly longer development on next batch")
            suggestions.add("Protect more usable energy into first crack")
        } else if (sweetness != null && sweetness >= 8) {
            priority.add("Preserve current sweetness")
            suggestions.add("Keep current finish structure stable")
        }

        if (acidity != null && acidity >= 8) {
            if ((sweetness != null && sweetness <= 6) || (body != null && body <= 5)) {
                priority.add("Round sharp acidity")
                suggestions.add("Add a little more finish development")
                suggestions.add("Avoid dropping too early if cup feels尖薄")
            } else {
                priority.add("Preserve bright acidity")
                suggestions.add("Keep acidity clean; avoid over-correcting darker")
            }
        } else if (acidity != null && acidity <= 4) {
            priority.add("Recover liveliness")
            suggestions.add("Avoid over-extending development")
            suggestions.add("Keep finish cleaner to preserve sparkle")
        }

        if (body != null && body <= 5) {
            priority.add("Increase body")
            suggestions.add("Slightly deepen finish or extend development a little")
            suggestions.add("Avoid excessive airflow late if body feels too thin")
        } else if (body != null && body >= 8) {
            priority.add("Prevent heaviness")
            suggestions.add("Do not over-extend development")
            suggestions.add("Protect clarity while keeping body")
        }

        if (clarity != null && clarity <= 5) {
            priority.add("Improve flavor clarity")
            suggestions.add("Tighten finish and avoid dragging development too long")
            suggestions.add("Keep structure cleaner if cup feels muddy")
        } else if (clarity != null && clarity >= 8) {
            priority.add("Preserve clarity")
            suggestions.add("Avoid adding unnecessary late-stage heaviness")
        }

        if (balance != null && balance <= 5) {
            priority.add("Improve balance")
            suggestions.add("Use smaller corrections instead of single large moves")
            suggestions.add("Coordinate sweetness / acidity / body together")
        }

        if (beanColor != null && beanColor < 95.0) {
            priority.add("Watch roast depth")
            suggestions.add("Roasted bean color looks relatively light; verify whether finish is slightly short")
        } else if (beanColor != null && beanColor > 110.0) {
            priority.add("Watch overdevelopment risk")
            suggestions.add("Bean color looks relatively deep; verify whether finish is too heavy")
        }

        if (groundColor != null && beanColor != null) {
            val spread = groundColor - beanColor
            if (spread < 8.0) {
                priority.add("Check inner development")
                suggestions.add("Bean / ground color spread looks narrow; verify internal development and extraction behavior")
            }
        }

        if (roastedAw != null) {
            if (roastedAw >= 0.42) {
                priority.add("Check finish stability")
                suggestions.add("Roasted aw looks a bit high; verify whether finish is slightly short or roast is resting unevenly")
            } else if (roastedAw <= 0.28) {
                priority.add("Check dryness risk")
                suggestions.add("Roasted aw looks low; verify whether finish is too dry or structure is too pushed")
            }
        }

        if (priority.isEmpty()) {
            priority.add("Maintain current flavor direction")
            suggestions.add("Cup looks broadly acceptable; use only minor adjustments")
            suggestions.add("Change one variable at a time on next batch")
        }

        val flavorDirection = buildFlavorDirection(
            sweetness = sweetness,
            acidity = acidity,
            body = body,
            clarity = clarity,
            balance = balance
        )

        val headline = buildHeadline(
            sweetness = sweetness,
            acidity = acidity,
            body = body,
            clarity = clarity,
            balance = balance
        )

        val summary = """
Flavor Bridge

Headline
$headline

Flavor Direction
$flavorDirection

Priority
${priority.joinToString("\n") { "• $it" }}

Suggestions
${suggestions.distinct().joinToString("\n") { "• $it" }}
        """.trimIndent()

        return RoastFlavorBridgeResult(
            headline = headline,
            flavorDirection = flavorDirection,
            priority = priority.distinct(),
            suggestions = suggestions.distinct(),
            summary = summary
        )
    }

    private fun buildHeadline(
        sweetness: Int?,
        acidity: Int?,
        body: Int?,
        clarity: Int?,
        balance: Int?
    ): String {
        return when {
            sweetness != null && sweetness <= 5 && body != null && body <= 5 ->
                "Cup looks under-developed in sweetness and body"
            acidity != null && acidity >= 8 && sweetness != null && sweetness <= 6 ->
                "Acidity is vivid but support is insufficient"
            clarity != null && clarity <= 5 && body != null && body >= 7 ->
                "Cup has weight but lacks clarity"
            balance != null && balance <= 5 ->
                "Cup balance needs coordination"
            sweetness != null && sweetness >= 8 && clarity != null && clarity >= 8 ->
                "Cup shows strong sweetness and clarity"
            else ->
                "Flavor profile is relatively stable"
        }
    }

    private fun buildFlavorDirection(
        sweetness: Int?,
        acidity: Int?,
        body: Int?,
        clarity: Int?,
        balance: Int?
    ): String {
        return when {
            sweetness != null && sweetness <= 5 && body != null && body <= 5 ->
                "Move slightly sweeter, fuller, and more developed"
            acidity != null && acidity >= 8 && sweetness != null && sweetness <= 6 ->
                "Keep acidity but add sweetness support"
            clarity != null && clarity <= 5 && body != null && body >= 7 ->
                "Keep body but clean up finish and clarity"
            balance != null && balance <= 5 ->
                "Use smaller coordinated corrections for a more balanced cup"
            sweetness != null && sweetness >= 8 && clarity != null && clarity >= 8 ->
                "Preserve the current sweet and clear profile"
            else ->
                "Apply light flavor-direction tuning only"
        }
    }
}
