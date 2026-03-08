package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry

object HistoryPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST HISTORY"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "View roast history, evaluation status, replayability, risk summary, open batch detail, or clear all history"
            )
        )
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "HISTORY ACTIONS"))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh History"

        val clearBtn = Button(context)
        clearBtn.text = "Clear All History"

        val summaryBody = UiKit.bodyText(context, RoastHistoryEngine.summary())

        actionCard.addView(refreshBtn)
        actionCard.addView(clearBtn)
        actionCard.addView(summaryBody)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val listHost = LinearLayout(context)
        listHost.orientation = LinearLayout.VERTICAL
        root.addView(listHost)

        fun renderList() {
            summaryBody.text = RoastHistoryEngine.summary()
            listHost.removeAllViews()

            val items = RoastHistoryEngine.all()

            if (items.isEmpty()) {
                listHost.addView(
                    UiKit.buildCard(
                        context,
                        "NO HISTORY",
                        "No roast history saved yet."
                    )
                )
                return
            }

            items.forEachIndexed { index, entry ->
                listHost.addView(buildEntryCard(context, container, entry, ::renderList))
                if (index != items.lastIndex) {
                    listHost.addView(UiKit.spacer(context))
                }
            }
        }

        refreshBtn.setOnClickListener {
            renderList()
        }

        clearBtn.setOnClickListener {
            RoastHistoryEngine.clear()
            renderList()
        }

        renderList()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildEntryCard(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry,
        onChanged: () -> Unit
    ): LinearLayout {
        val card = UiKit.card(context)

        val title = TextView(context)
        title.text = entry.batchId
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)

        val body = TextView(context)
        body.textSize = 15f
        body.text = buildEntryBody(entry)
        body.setPadding(0, UiKit.INNER_GAP, 0, 0)

        val openDetailBtn = Button(context)
        openDetailBtn.text = "Open Detail"

        val deleteBtn = Button(context)
        deleteBtn.text = "Delete This Record"

        openDetailBtn.setOnClickListener {
            BatchDetailPage.show(
                context = context,
                container = container,
                batchId = entry.batchId
            )
        }

        deleteBtn.setOnClickListener {
            RoastHistoryEngine.delete(entry.batchId)
            onChanged()
        }

        card.addView(title)
        card.addView(body)
        card.addView(openDetailBtn)
        card.addView(deleteBtn)

        return card
    }

    private fun buildEntryBody(entry: RoastHistoryEntry): String {
        val evaluationStatus = if (entry.evaluation != null) "Saved" else "Not saved"
        val replayability = buildReplayability(entry)
        val risk = buildRisk(entry)
        val headline = buildHeadline(entry)

        val predicted = """
Predicted
Turning ${entry.predictedTurningSec?.toString() ?: "-"}
Yellow  ${entry.predictedYellowSec?.toString() ?: "-"}
FC      ${entry.predictedFcSec?.toString() ?: "-"}
Drop    ${entry.predictedDropSec?.toString() ?: "-"}
        """.trimIndent()

        val actual = """
Actual
Turning ${entry.actualTurningSec?.toString() ?: "-"}
Yellow  ${entry.actualYellowSec?.toString() ?: "-"}
FC      ${entry.actualFcSec?.toString() ?: "-"}
Drop    ${entry.actualDropSec?.toString() ?: "-"}
ROR     ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

        return """
Headline
$headline

Status
${entry.batchStatus}

Evaluation
$evaluationStatus

Replayability
$replayability

Risk
$risk

Bean
${entry.process.ifBlank { "-" }}
Density  ${"%.1f".format(entry.density)}
Moisture ${"%.1f".format(entry.moisture)}
aw       ${"%.2f".format(entry.aw)}

Environment
Temp ${"%.1f".format(entry.envTemp)}℃
RH   ${"%.1f".format(entry.envRh)}%

$predicted

$actual
        """.trimIndent()
    }

    private fun buildHeadline(entry: RoastHistoryEntry): String {
        val fcDelta = delta(entry.predictedFcSec, entry.actualFcSec)
        val turningDelta = delta(entry.predictedTurningSec, entry.actualTurningSec)
        val ror = entry.actualPreFcRor

        return when {
            ror != null && ror >= 10.8 -> "Late-stage acceleration too strong"
            ror != null && ror <= 7.0 -> "Energy may be collapsing before crack"
            fcDelta != null && fcDelta >= 20 -> "FC landed too late"
            fcDelta != null && fcDelta <= -20 -> "FC landed too early"
            turningDelta != null && turningDelta >= 12 -> "Front-end energy looked weak"
            turningDelta != null && turningDelta <= -12 -> "Front-end push looked too strong"
            else -> "Batch stayed relatively close to plan"
        }
    }

    private fun buildReplayability(entry: RoastHistoryEntry): String {
        val score = replayabilityScore(entry)
        return when {
            score >= 85 -> "High"
            score >= 65 -> "Medium"
            else -> "Low"
        }
    }

    private fun buildRisk(entry: RoastHistoryEntry): String {
        val score = riskScore(entry)
        return when {
            score >= 8 -> "High"
            score >= 4 -> "Medium"
            score >= 1 -> "Low"
            else -> "Minor"
        }
    }

    private fun replayabilityScore(entry: RoastHistoryEntry): Int {
        var score = 100

        val turningDelta = absDelta(entry.predictedTurningSec, entry.actualTurningSec)
        val yellowDelta = absDelta(entry.predictedYellowSec, entry.actualYellowSec)
        val fcDelta = absDelta(entry.predictedFcSec, entry.actualFcSec)
        val dropDelta = absDelta(entry.predictedDropSec, entry.actualDropSec)
        val ror = entry.actualPreFcRor

        score -= penalty(turningDelta, 2, 6, 12)
        score -= penalty(yellowDelta, 2, 8, 15)
        score -= penalty(fcDelta, 2, 10, 20)
        score -= penalty(dropDelta, 1, 10, 20)

        if (ror != null) {
            if (ror >= 10.8) score -= 18
            else if (ror >= 9.5) score -= 10
            else if (ror <= 7.0) score -= 18
            else if (ror <= 8.0) score -= 10
        }

        return score.coerceIn(0, 100)
    }

    private fun riskScore(entry: RoastHistoryEntry): Int {
        var score = 0

        val turningDelta = absDelta(entry.predictedTurningSec, entry.actualTurningSec)
        val yellowDelta = absDelta(entry.predictedYellowSec, entry.actualYellowSec)
        val fcDelta = absDelta(entry.predictedFcSec, entry.actualFcSec)
        val dropDelta = absDelta(entry.predictedDropSec, entry.actualDropSec)
        val ror = entry.actualPreFcRor

        if (turningDelta >= 12) score += 2 else if (turningDelta >= 6) score += 1
        if (yellowDelta >= 15) score += 2 else if (yellowDelta >= 8) score += 1
        if (fcDelta >= 20) score += 3 else if (fcDelta >= 10) score += 1
        if (dropDelta >= 20) score += 1 else if (dropDelta >= 10) score += 1

        if (ror != null) {
            if (ror >= 10.8 || ror <= 7.0) score += 3
            else if (ror >= 9.5 || ror <= 8.0) score += 1
        }

        return score
    }

    private fun penalty(absDelta: Int, mild: Int, mid: Int, high: Int): Int {
        return when {
            absDelta >= high -> 20
            absDelta >= mid -> 12
            absDelta >= mild -> 5
            else -> 0
        }
    }

    private fun absDelta(predicted: Int?, actual: Int?): Int {
        return if (predicted == null || actual == null) 0 else kotlin.math.abs(actual - predicted)
    }

    private fun delta(predicted: Int?, actual: Int?): Int? {
        return if (predicted == null || actual == null) null else actual - predicted
    }
}
