package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastFlavorBridge
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry

object BatchDetailPage {

    fun show(
        context: Context,
        container: LinearLayout,
        batchId: String
    ) {
        container.removeAllViews()

        val entry = RoastHistoryEngine.findByBatchId(batchId)

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "BATCH DETAIL"))
        root.addView(UiKit.pageSubtitle(context, "Roast replay, diagnosis, correction, report, and evaluation entry"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))

        val backBtn = Button(context)
        backBtn.text = "Back to History"
        backBtn.setOnClickListener {
            HistoryPage.show(context, container)
        }

        val evaluationBtn = Button(context)
        evaluationBtn.text = "Open Evaluation"
        evaluationBtn.setOnClickListener {
            RoastEvaluationPage.show(
                context = context,
                container = container,
                batchId = batchId
            )
        }

        navCard.addView(backBtn)
        navCard.addView(evaluationBtn)
        root.addView(navCard)
        root.addView(UiKit.spacer(context))

        if (entry == null) {
            root.addView(
                UiKit.buildCard(
                    context,
                    "NOT FOUND",
                    "No roast history found for batchId = $batchId"
                )
            )
            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val flavorBridge = RoastFlavorBridge.buildFromEntry(entry)

        root.addView(
            UiKit.buildCard(
                context,
                "HEADER",
                buildHeader(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "RESULT SUMMARY",
                buildResultSummary(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "KEY METRICS",
                buildKeyMetrics(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "BEAN / ENVIRONMENT",
                buildBeanEnv(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "PREDICTED PLAN",
                buildPredicted(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "ACTUAL TIMELINE",
                buildActual(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "DEVIATION SUMMARY",
                buildDeviation(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "EVALUATION STATUS",
                buildEvaluationStatus(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "FLAVOR BRIDGE",
                flavorBridge.summary
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "DIAGNOSIS",
                entry.diagnosisText.ifBlank { "No diagnosis saved" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "NEXT-BATCH CORRECTION",
                entry.correctionText.ifBlank { "No correction bridge saved" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "FULL ROAST REPORT",
                entry.reportText.ifBlank { "No roast report saved" }
            )
        )

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildHeader(entry: RoastHistoryEntry): String {
        return """
Batch ID
${entry.batchId}

Created
${entry.createdAtMillis}

Status
${entry.batchStatus}

Bean
${entry.process.ifBlank { "-" }}
        """.trimIndent()
    }

    private fun buildResultSummary(entry: RoastHistoryEntry): String {
        val headline = buildHeadline(entry)
        val replayability = buildReplayability(entry)
        val risk = buildRisk(entry)

        return """
Headline
$headline

Replayability
$replayability

Risk
$risk
        """.trimIndent()
    }

    private fun buildKeyMetrics(entry: RoastHistoryEntry): String {
        val devSec =
            if (entry.actualFcSec != null && entry.actualDropSec != null && entry.actualDropSec > entry.actualFcSec) {
                (entry.actualDropSec - entry.actualFcSec).toString() + "s"
            } else {
                "-"
            }

        val dtr =
            if (entry.actualFcSec != null && entry.actualDropSec != null && entry.actualDropSec > entry.actualFcSec && entry.actualDropSec > 0) {
                "%.1f".format(
                    ((entry.actualDropSec - entry.actualFcSec).toDouble() / entry.actualDropSec.toDouble()) * 100.0
                ) + "%"
            } else {
                "-"
            }

        return """
FC
${entry.actualFcSec?.toString() ?: entry.predictedFcSec?.toString() ?: "-"}

Drop
${entry.actualDropSec?.toString() ?: entry.predictedDropSec?.toString() ?: "-"}

Development
$devSec

DTR
$dtr

Pre-FC ROR
${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()
    }

    private fun buildBeanEnv(entry: RoastHistoryEntry): String {
        return """
Bean
Process    ${entry.process.ifBlank { "-" }}
Density    ${"%.1f".format(entry.density)}
Moisture   ${"%.1f".format(entry.moisture)}
aw         ${"%.2f".format(entry.aw)}

Environment
Temp       ${"%.1f".format(entry.envTemp)}℃
RH         ${"%.1f".format(entry.envRh)}%
        """.trimIndent()
    }

    private fun buildPredicted(entry: RoastHistoryEntry): String {
        return """
Predicted Anchors
Turning   ${secOrDash(entry.predictedTurningSec)}
Yellow    ${secOrDash(entry.predictedYellowSec)}
FC        ${secOrDash(entry.predictedFcSec)}
Drop      ${secOrDash(entry.predictedDropSec)}
        """.trimIndent()
    }

    private fun buildActual(entry: RoastHistoryEntry): String {
        return """
Actual Anchors
Turning   ${secOrDash(entry.actualTurningSec)}
Yellow    ${secOrDash(entry.actualYellowSec)}
FC        ${secOrDash(entry.actualFcSec)}
Drop      ${secOrDash(entry.actualDropSec)}

Current
Status    ${entry.batchStatus}
Pre-FC ROR ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()
    }

    private fun buildDeviation(entry: RoastHistoryEntry): String {
        return """
Turning   ${deviationLine(entry.predictedTurningSec, entry.actualTurningSec)}
Yellow    ${deviationLine(entry.predictedYellowSec, entry.actualYellowSec)}
FC        ${deviationLine(entry.predictedFcSec, entry.actualFcSec)}
Drop      ${deviationLine(entry.predictedDropSec, entry.actualDropSec)}
ROR       ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()
    }

    private fun buildEvaluationStatus(entry: RoastHistoryEntry): String {
        val evaluation = entry.evaluation ?: return """
Status
Not saved

Next Step
Open Evaluation and enter roasted bean / cup data
        """.trimIndent()

        return """
Status
Saved

Bean Color
${evaluation.beanColor?.let { "%.2f".format(it) } ?: "-"}

Ground Color
${evaluation.groundColor?.let { "%.2f".format(it) } ?: "-"}

Roasted aw
${evaluation.roastedAw?.let { "%.3f".format(it) } ?: "-"}

Cup
Sweetness ${evaluation.sweetness ?: "-"}
Acidity ${evaluation.acidity ?: "-"}
Body ${evaluation.body ?: "-"}
Flavor Clarity ${evaluation.flavorClarity ?: "-"}
Balance ${evaluation.balance ?: "-"}

Notes
${evaluation.notes.ifBlank { "-" }}
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

    private fun deviationLine(predicted: Int?, actual: Int?): String {
        return when {
            predicted == null && actual == null -> "Pred - / Actual - / Δ -"
            predicted == null -> "Pred - / Actual $actual / Δ -"
            actual == null -> "Pred $predicted / Actual - / Δ -"
            else -> {
                val delta = actual - predicted
                val deltaText = if (delta > 0) "+$delta" else delta.toString()
                "Pred $predicted / Actual $actual / Δ ${deltaText}s"
            }
        }
    }

    private fun absDelta(predicted: Int?, actual: Int?): Int {
        return if (predicted == null || actual == null) 0 else kotlin.math.abs(actual - predicted)
    }

    private fun delta(predicted: Int?, actual: Int?): Int? {
        return if (predicted == null || actual == null) null else actual - predicted
    }

    private fun secOrDash(value: Int?): String {
        return value?.toString() ?: "-"
    }
}
