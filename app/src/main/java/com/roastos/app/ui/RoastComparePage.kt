package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.UiKit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object RoastComparePage {

    fun show(
        context: Context,
        container: LinearLayout,
        left: RoastHistoryEntry?,
        right: RoastHistoryEntry?,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST COMPARE"))
        root.addView(UiKit.pageSubtitle(context, "Side-by-side batch comparison"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")

        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(backBtn)

        root.addView(navCard)
        root.addView(UiKit.spacer(context))

        if (left == null || right == null) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO COMPARE DATA"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "Two roast history entries are required for comparison."
                )
            )
            root.addView(emptyCard)

            backBtn.setOnClickListener {
                onBack?.invoke() ?: RoastStudioPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        root.addView(
            buildSectionCard(
                context = context,
                title = "OPERATION HINTS",
                leftLabel = "HINTS",
                leftValue = buildOperationHints(left, right),
                rightLabel = "SELECTED",
                rightValue = """
A
${left.batchId}

B
${right.batchId}
                """.trimIndent()
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "KEY DIFFERENCES",
                leftLabel = "TAGS",
                leftValue = buildKeyDifferences(left, right),
                rightLabel = "SELECTED",
                rightValue = """
A
${left.batchId}

B
${right.batchId}
                """.trimIndent()
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "COMPARE SUMMARY",
                leftLabel = "SUMMARY",
                leftValue = buildCompareSummary(left, right),
                rightLabel = "SELECTED",
                rightValue = """
A
${left.batchId}

B
${right.batchId}
                """.trimIndent()
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "BATCH OVERVIEW",
                leftLabel = "A",
                leftValue = buildBatchOverview(left),
                rightLabel = "B",
                rightValue = buildBatchOverview(right)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "MATERIAL",
                leftLabel = "A",
                leftValue = buildMaterial(left),
                rightLabel = "B",
                rightValue = buildMaterial(right)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "ENVIRONMENT",
                leftLabel = "A",
                leftValue = buildEnvironment(left),
                rightLabel = "B",
                rightValue = buildEnvironment(right)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "TIMELINE",
                leftLabel = "A",
                leftValue = buildTimeline(left),
                rightLabel = "B",
                rightValue = buildTimeline(right)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "ROAST HEALTH",
                leftLabel = "A",
                leftValue = buildRoastHealth(left),
                rightLabel = "B",
                rightValue = buildRoastHealth(right)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "REPORT",
                leftLabel = "A",
                leftValue = left.reportText.ifBlank { "-" },
                rightLabel = "B",
                rightValue = right.reportText.ifBlank { "-" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "DIAGNOSIS",
                leftLabel = "A",
                leftValue = left.diagnosisText.ifBlank { "-" },
                rightLabel = "B",
                rightValue = right.diagnosisText.ifBlank { "-" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            buildSectionCard(
                context = context,
                title = "CORRECTION",
                leftLabel = "A",
                leftValue = left.correctionText.ifBlank { "-" },
                rightLabel = "B",
                rightValue = right.correctionText.ifBlank { "-" }
            )
        )

        backBtn.setOnClickListener {
            onBack?.invoke() ?: RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildSectionCard(
        context: Context,
        title: String,
        leftLabel: String,
        leftValue: String,
        rightLabel: String,
        rightValue: String
    ): LinearLayout {
        val card = UiKit.card(context)

        card.addView(UiKit.cardTitle(context, title))
        card.addView(
            UiKit.bodyText(
                context,
                """
$leftLabel
$leftValue

$rightLabel
$rightValue
                """.trimIndent()
            )
        )

        return card
    }

    private fun buildOperationHints(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val hints = mutableListOf<String>()

        val fcA = left.actualFcSec ?: left.predictedFcSec
        val fcB = right.actualFcSec ?: right.predictedFcSec
        val fcDiff = diffIfBothPresent(fcA, fcB)
        if (fcDiff != null && abs(fcDiff) >= 10) {
            hints += if (fcDiff < 0) {
                """
Focus 1
A reaches first crack much earlier than B.
Next roast should verify whether mid-late phase energy was intentionally higher in A.
                """.trimIndent()
            } else {
                """
Focus 1
B reaches first crack much earlier than A.
Next roast should verify whether mid-late phase energy was intentionally higher in B.
                """.trimIndent()
            }
        }

        val dropA = left.actualDropSec ?: left.predictedDropSec
        val dropB = right.actualDropSec ?: right.predictedDropSec
        val dropDiff = diffIfBothPresent(dropA, dropB)
        if (dropDiff != null && abs(dropDiff) >= 10) {
            hints += if (dropDiff < 0) {
                """
Focus ${hints.size + 1}
A drops earlier than B by a clear margin.
Next roast should confirm whether the finish window was deliberately shortened in A.
                """.trimIndent()
            } else {
                """
Focus ${hints.size + 1}
B drops earlier than A by a clear margin.
Next roast should confirm whether the finish window was deliberately shortened in B.
                """.trimIndent()
            }
        }

        val rorA = left.actualPreFcRor
        val rorB = right.actualPreFcRor
        val rorDiff = diffIfBothPresent(rorA, rorB)
        if (rorDiff != null && abs(rorDiff) >= 0.5) {
            hints += if (rorDiff > 0) {
                """
Focus ${hints.size + 1}
A shows clearly higher pre-FC RoR than B.
Next roast should pay attention to whether this stronger momentum was intentional and repeatable.
                """.trimIndent()
            } else {
                """
Focus ${hints.size + 1}
B shows clearly higher pre-FC RoR than A.
Next roast should pay attention to whether this stronger momentum was intentional and repeatable.
                """.trimIndent()
            }
        }

        val envTempDiff = left.envTemp - right.envTemp
        val envRhDiff = left.envRh - right.envRh
        if (abs(envTempDiff) >= 1.0 || abs(envRhDiff) >= 5.0) {
            hints += """
Focus ${hints.size + 1}
Ambient conditions differ clearly between A and B.
Do not compare heat application or phase timing without accounting for the environment shift first.
            """.trimIndent()
        }

        val riskA = riskScore(left.roastHealthHeadline)
        val riskB = riskScore(right.roastHealthHeadline)
        if (riskA != riskB && maxOf(riskA, riskB) > 0) {
            hints += if (riskA > riskB) {
                """
Focus ${hints.size + 1}
A shows a higher roast health risk headline than B.
Next roast should review late-stage stability in A before repeating the same finish pattern.
                """.trimIndent()
            } else {
                """
Focus ${hints.size + 1}
B shows a higher roast health risk headline than A.
Next roast should review late-stage stability in B before repeating the same finish pattern.
                """.trimIndent()
            }
        }

        val yellowA = left.actualYellowSec ?: left.predictedYellowSec
        val yellowB = right.actualYellowSec ?: right.predictedYellowSec
        val yellowDiff = diffIfBothPresent(yellowA, yellowB)
        if (yellowDiff != null && abs(yellowDiff) >= 10 && hints.size < 5) {
            hints += if (yellowDiff < 0) {
                """
Focus ${hints.size + 1}
A reaches yellow earlier than B.
Next roast should check whether early drying pace was intentionally faster in A.
                """.trimIndent()
            } else {
                """
Focus ${hints.size + 1}
B reaches yellow earlier than A.
Next roast should check whether early drying pace was intentionally faster in B.
                """.trimIndent()
            }
        }

        return if (hints.isEmpty()) {
            """
Focus 1
No strong operational gap is detected under the current rules.

Focus 2
Treat these two batches as broadly comparable and inspect the detailed sections for smaller differences.
            """.trimIndent()
        } else {
            hints.take(5).joinToString("\n\n")
        }
    }

    private fun buildKeyDifferences(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val tags = mutableListOf<String>()

        buildTimeTag(
            label = "FASTER TURNING",
            leftSec = left.actualTurningSec ?: left.predictedTurningSec,
            rightSec = right.actualTurningSec ?: right.predictedTurningSec,
            detailBase = "Turning"
        )?.let { tags += it }

        buildTimeTag(
            label = "FASTER YELLOW",
            leftSec = left.actualYellowSec ?: left.predictedYellowSec,
            rightSec = right.actualYellowSec ?: right.predictedYellowSec,
            detailBase = "Yellow"
        )?.let { tags += it }

        buildTimeTag(
            label = "FASTER FC",
            leftSec = left.actualFcSec ?: left.predictedFcSec,
            rightSec = right.actualFcSec ?: right.predictedFcSec,
            detailBase = "First crack"
        )?.let { tags += it }

        buildTimeTag(
            label = "FASTER DROP",
            leftSec = left.actualDropSec ?: left.predictedDropSec,
            rightSec = right.actualDropSec ?: right.predictedDropSec,
            detailBase = "Drop"
        )?.let { tags += it }

        buildHigherDoubleTag(
            label = "HIGHER PRE-FC ROR",
            leftValue = left.actualPreFcRor,
            rightValue = right.actualPreFcRor,
            unit = "℃/min",
            detailPrefix = "Pre-FC RoR"
        )?.let { tags += it }

        buildHigherDoubleTag(
            label = "HIGHER ENV TEMP",
            leftValue = left.envTemp,
            rightValue = right.envTemp,
            unit = "℃",
            detailPrefix = "Env temp"
        )?.let { tags += it }

        buildHigherDoubleTag(
            label = "HIGHER ENV RH",
            leftValue = left.envRh,
            rightValue = right.envRh,
            unit = "%",
            detailPrefix = "Env RH"
        )?.let { tags += it }

        buildRiskTag(
            leftHeadline = left.roastHealthHeadline,
            rightHeadline = right.roastHealthHeadline
        )?.let { tags += it }

        return if (tags.isEmpty()) {
            """
NO STRONG DIFFERENCE
Current records do not show a strong difference under the active rules.
            """.trimIndent()
        } else {
            tags.joinToString("\n\n")
        }
    }

    private fun buildTimeTag(
        label: String,
        leftSec: Int?,
        rightSec: Int?,
        detailBase: String
    ): String? {
        if (leftSec == null || rightSec == null) return null
        val diff = leftSec - rightSec
        if (abs(diff) < 5) return null

        return if (diff < 0) {
            """
$label
A reaches $detailBase ${abs(diff)}s earlier than B.
            """.trimIndent()
        } else {
            """
$label
B reaches $detailBase ${abs(diff)}s earlier than A.
            """.trimIndent()
        }
    }

    private fun buildHigherDoubleTag(
        label: String,
        leftValue: Double?,
        rightValue: Double?,
        unit: String,
        detailPrefix: String
    ): String? {
        if (leftValue == null || rightValue == null) return null
        val diff = leftValue - rightValue
        if (abs(diff) < 0.2) return null

        return if (diff > 0) {
            """
$label
A $detailPrefix is ${formatOneDecimal(abs(diff))}$unit higher than B.
            """.trimIndent()
        } else {
            """
$label
B $detailPrefix is ${formatOneDecimal(abs(diff))}$unit higher than A.
            """.trimIndent()
        }
    }

    private fun buildRiskTag(
        leftHeadline: String,
        rightHeadline: String
    ): String? {
        val leftScore = riskScore(leftHeadline)
        val rightScore = riskScore(rightHeadline)

        if (leftScore == rightScore) return null
        if (leftScore <= 0 && rightScore <= 0) return null

        return if (leftScore > rightScore) {
            """
HIGHER RISK
A shows a higher roast health risk headline than B.
            """.trimIndent()
        } else {
            """
HIGHER RISK
B shows a higher roast health risk headline than A.
            """.trimIndent()
        }
    }

    private fun riskScore(headline: String): Int {
        val text = headline.lowercase(Locale.getDefault())

        return when {
            "高风险" in headline -> 4
            "中风险" in headline -> 3
            "需留意" in headline -> 2
            "低风险" in headline -> 1
            "high" in text -> 4
            "medium" in text -> 3
            "watch" in text -> 2
            "low" in text -> 1
            else -> 0
        }
    }

    private fun buildCompareSummary(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val lines = mutableListOf<String>()

        lines += "Created"
        lines += buildCreatedSummary(left, right)
        lines += ""
        lines += "Timeline"
        lines += buildTimelineSummary(left, right)
        lines += ""
        lines += "Environment"
        lines += buildEnvironmentSummary(left, right)
        lines += ""
        lines += "RoR"
        lines += buildRorSummary(left, right)
        lines += ""
        lines += "Health"
        lines += "A: ${left.roastHealthHeadline}"
        lines += "B: ${right.roastHealthHeadline}"

        return lines.joinToString("\n")
    }

    private fun buildCreatedSummary(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val diffMillis = left.createdAtMillis - right.createdAtMillis
        if (diffMillis == 0L) return "A and B were created at the same time."

        val diffMinutes = abs(diffMillis) / 60000
        val earlier = if (diffMillis < 0) "A" else "B"
        val later = if (earlier == "A") "B" else "A"

        return if (diffMinutes < 1) {
            "$earlier was created slightly earlier than $later."
        } else {
            "$earlier was created ${diffMinutes} min earlier than $later."
        }
    }

    private fun buildTimelineSummary(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val parts = mutableListOf<String>()

        compareSecLine("Turning", left.actualTurningSec ?: left.predictedTurningSec, right.actualTurningSec ?: right.predictedTurningSec)
            ?.let { parts += it }

        compareSecLine("Yellow", left.actualYellowSec ?: left.predictedYellowSec, right.actualYellowSec ?: right.predictedYellowSec)
            ?.let { parts += it }

        compareSecLine("First Crack", left.actualFcSec ?: left.predictedFcSec, right.actualFcSec ?: right.predictedFcSec)
            ?.let { parts += it }

        compareSecLine("Drop", left.actualDropSec ?: left.predictedDropSec, right.actualDropSec ?: right.predictedDropSec)
            ?.let { parts += it }

        return if (parts.isEmpty()) {
            "No comparable timeline data."
        } else {
            parts.joinToString("\n")
        }
    }

    private fun buildEnvironmentSummary(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val tempDiff = left.envTemp - right.envTemp
        val rhDiff = left.envRh - right.envRh

        val tempLine = when {
            abs(tempDiff) < 0.05 -> "Env temp is effectively the same."
            tempDiff > 0 -> "A env temp is ${formatOneDecimal(abs(tempDiff))} ℃ higher."
            else -> "B env temp is ${formatOneDecimal(abs(tempDiff))} ℃ higher."
        }

        val rhLine = when {
            abs(rhDiff) < 0.05 -> "Env RH is effectively the same."
            rhDiff > 0 -> "A env RH is ${formatOneDecimal(abs(rhDiff))} % higher."
            else -> "B env RH is ${formatOneDecimal(abs(rhDiff))} % higher."
        }

        return "$tempLine\n$rhLine"
    }

    private fun buildRorSummary(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val a = left.actualPreFcRor
        val b = right.actualPreFcRor

        if (a == null || b == null) {
            return "No comparable pre-FC RoR data."
        }

        val diff = a - b

        return when {
            abs(diff) < 0.05 -> "Pre-FC RoR is effectively the same."
            diff > 0 -> "A pre-FC RoR is ${formatOneDecimal(abs(diff))} ℃/min higher."
            else -> "B pre-FC RoR is ${formatOneDecimal(abs(diff))} ℃/min higher."
        }
    }

    private fun compareSecLine(
        label: String,
        leftSec: Int?,
        rightSec: Int?
    ): String? {
        if (leftSec == null || rightSec == null) return null

        val diff = leftSec - rightSec

        return when {
            diff == 0 -> "$label is the same."
            diff < 0 -> "A $label is ${abs(diff)}s earlier than B."
            else -> "B $label is ${abs(d
