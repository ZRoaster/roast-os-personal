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
            else -> "B $label is ${abs(diff)}s earlier than A."
        }
    }

    private fun buildBatchOverview(entry: RoastHistoryEntry): String {
        return """
Batch ID
${entry.batchId}

Title
${entry.title}

Created
${formatDateTime(entry.createdAtMillis)}

Status
${entry.batchStatus}

Process
${entry.process}
        """.trimIndent()
    }

    private fun buildMaterial(entry: RoastHistoryEntry): String {
        return """
Density
${entry.density}

Moisture
${entry.moisture}

AW
${entry.aw}

Pre-FC RoR
${formatRor(entry.actualPreFcRor)}
        """.trimIndent()
    }

    private fun buildEnvironment(entry: RoastHistoryEntry): String {
        return """
Env Temp
${entry.envTemp} ℃

Env RH
${entry.envRh} %
        """.trimIndent()
    }

    private fun buildTimeline(entry: RoastHistoryEntry): String {
        return """
Turning
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)}

Yellow
${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

First Crack
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)}

Drop
${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}
        """.trimIndent()
    }

    private fun buildRoastHealth(entry: RoastHistoryEntry): String {
        return """
Headline
${entry.roastHealthHeadline}

Detail
${entry.roastHealthDetail}
        """.trimIndent()
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRor(value: Double?): String {
        if (value == null) return "-"
        return String.format(Locale.getDefault(), "%.1f ℃/min", value)
    }

    private fun formatOneDecimal(value: Double): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
