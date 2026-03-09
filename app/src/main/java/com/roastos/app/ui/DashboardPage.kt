package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.PlannerBaselineStore
import com.roastos.app.RoastCorrectionBridgeV2
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastPreheatAssistEngine
import com.roastos.app.RoastProfileEngine

object DashboardPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST OS DASHBOARD"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "System overview for telemetry, baseline, preheat, history, profile, and unified correction"
            )
        )
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "DASHBOARD ACTIONS"))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Dashboard"

        actionCard.addView(refreshBtn)
        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "MACHINE TELEMETRY"))
        val telemetryBody = UiKit.bodyText(context, "")
        telemetryCard.addView(telemetryBody)
        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val baselineCard = UiKit.card(context)
        baselineCard.addView(UiKit.cardTitle(context, "PLANNER BASELINE"))
        val baselineBody = UiKit.bodyText(context, "")
        baselineCard.addView(baselineBody)
        root.addView(baselineCard)
        root.addView(UiKit.spacer(context))

        val preheatCard = UiKit.card(context)
        preheatCard.addView(UiKit.cardTitle(context, "PREHEAT TARGET"))
        val preheatBody = UiKit.bodyText(context, "")
        preheatCard.addView(preheatBody)
        root.addView(preheatCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        historyCard.addView(UiKit.cardTitle(context, "LATEST HISTORY"))
        val historyBody = UiKit.bodyText(context, "")
        historyCard.addView(historyBody)
        root.addView(historyCard)
        root.addView(UiKit.spacer(context))

        val profileCard = UiKit.card(context)
        profileCard.addView(UiKit.cardTitle(context, "LATEST PROFILE"))
        val profileBody = UiKit.bodyText(context, "")
        profileCard.addView(profileBody)
        root.addView(profileCard)
        root.addView(UiKit.spacer(context))

        val correctionCard = UiKit.card(context)
        correctionCard.addView(UiKit.cardTitle(context, "LATEST UNIFIED CORRECTION"))
        val correctionBody = UiKit.bodyText(context, "")
        correctionCard.addView(correctionBody)
        root.addView(correctionCard)

        fun buildBaselineText(): String {
            val baseline = PlannerBaselineStore.current()
                ?: return """
Status
No active planner baseline

Next Step
Apply a profile suggestion or capture a planner result as baseline
                """.trimIndent()

            val match = PlannerBaselineStore.evaluateMatchAgainstCurrentInput()

            return """
Source
${baseline.source}

Label
${baseline.label}

Turning
${baseline.turningSec?.toString()?.plus("s") ?: "-"}

Yellow
${baseline.yellowSec?.toString()?.plus("s") ?: "-"}

FC
${baseline.fcSec?.toString()?.plus("s") ?: "-"}

Drop
${baseline.dropSec?.toString()?.plus("s") ?: "-"}

Dev
${baseline.devSec?.let { "${it}s" } ?: "-"}

DTR
${baseline.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}

Match Grade
${formatBaselineMatch(match?.grade?.name)}

Match Score
${match?.score?.toString() ?: "-"}
            """.trimIndent()
        }

        fun buildPreheatText(): String {
            val target = RoastPreheatAssistEngine.buildTargetFromCurrentState()

            return """
Target
${"%.1f".format(target.targetTempC)}℃

Window
${"%.1f".format(target.windowLowC)}–${"%.1f".format(target.windowHighC)}℃

Hold
${target.holdSec}s

Intent
${target.intent}

Reason
${target.reason}
            """.trimIndent()
        }

        fun buildLatestHistoryText(): String {
            val latest = RoastHistoryEngine.latest()
                ?: return """
Status
No roast history yet

Next Step
Finish a batch and save it into history
                """.trimIndent()

            return """
Batch ID
${latest.batchId}

Created
${latest.createdAtMillis}

Status
${latest.batchStatus}

Process
${latest.process.ifBlank { "-" }}

Replayability
${buildReplayability(latest.actualPreFcRor)}

Risk
${buildRisk(latest.actualPreFcRor)}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}

Baseline
${latest.baselineLabel ?: "Not recorded"}

Baseline Match
${formatBaselineMatch(latest.baselineMatchGrade)}
            """.trimIndent()
        }

        fun buildLatestProfileText(): String {
            val latest = RoastProfileEngine.latest()
                ?: return """
Status
No roast profile yet

Next Step
Open a batch detail page and save a good batch as Profile
                """.trimIndent()

            return """
Profile Name
${latest.name}

Profile ID
${latest.profileId}

Source Batch
${latest.sourceBatchId}

Replayability
${latest.replayability}

Risk
${latest.risk}

Evaluation
${if (latest.evaluationSaved) "Saved" else "Not saved"}

Process
${latest.process.ifBlank { "-" }}
            """.trimIndent()
        }

        fun buildLatestUnifiedCorrectionText(): String {
            val latest = RoastHistoryEngine.latest()
                ?: return """
Status
No roast history yet

Next Step
Save a batch into history first
                """.trimIndent()

            return RoastCorrectionBridgeV2.buildFromBatch(latest.batchId).summary
        }

        fun refreshAll() {
            telemetryBody.text = MachineTelemetryEngine.summary()
            baselineBody.text = buildBaselineText()
            preheatBody.text = buildPreheatText()
            historyBody.text = buildLatestHistoryText()
            profileBody.text = buildLatestProfileText()
            correctionBody.text = buildLatestUnifiedCorrectionText()
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildReplayability(ror: Double?): String {
        val value = ror ?: return "Medium"
        return when {
            value in 8.0..9.5 -> "High"
            value in 7.0..10.8 -> "Medium"
            else -> "Low"
        }
    }

    private fun buildRisk(ror: Double?): String {
        val value = ror ?: return "Minor"
        return when {
            value >= 10.8 || value <= 7.0 -> "High"
            value >= 9.5 || value <= 8.0 -> "Medium"
            else -> "Low"
        }
    }

    private fun formatBaselineMatch(raw: String?): String {
        return when (raw) {
            "EXACT_MATCH" -> "Exact Match"
            "SIMILAR_MATCH" -> "Similar Match"
            "REFERENCE_ONLY" -> "Reference Only"
            else -> "-"
        }
    }
}
