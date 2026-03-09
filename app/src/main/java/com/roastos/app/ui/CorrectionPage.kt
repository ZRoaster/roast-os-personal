package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastCorrectionBridgeV2
import com.roastos.app.RoastDeviationEngine
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastLiveAssistEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "CORRECTION CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Deviation diagnostics, live assist interpretation, and unified correction guidance"
            )
        )

        root.addView(UiKit.spacer(context))

        val deviationCard = UiKit.card(context)
        deviationCard.addView(UiKit.cardTitle(context, "DEVIATION DIAGNOSIS"))
        val deviationBody = UiKit.bodyText(context, "")
        deviationCard.addView(deviationBody)

        val deviationRefresh = UiKit.primaryButton(context, "Refresh Diagnosis")
        deviationCard.addView(deviationRefresh)

        root.addView(deviationCard)

        root.addView(UiKit.spacer(context))

        val assistCard = UiKit.card(context)
        assistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))
        val assistBody = UiKit.bodyText(context, "")
        assistCard.addView(assistBody)

        val assistRefresh = UiKit.secondaryButton(context, "Refresh Assist")
        assistCard.addView(assistRefresh)

        root.addView(assistCard)

        root.addView(UiKit.spacer(context))

        val correctionCard = UiKit.card(context)
        correctionCard.addView(UiKit.cardTitle(context, "UNIFIED CORRECTION"))
        val correctionBody = UiKit.bodyText(context, "")
        correctionCard.addView(correctionBody)

        val correctionRefresh = UiKit.primaryButton(context, "Rebuild Correction")
        correctionCard.addView(correctionRefresh)

        root.addView(correctionCard)

        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        historyCard.addView(UiKit.cardTitle(context, "LATEST BATCH REFERENCE"))
        val historyBody = UiKit.bodyText(context, "")
        historyCard.addView(historyBody)

        val openHistoryBtn = UiKit.secondaryButton(context, "Open History")
        historyCard.addView(openHistoryBtn)

        root.addView(historyCard)

        fun buildDeviation(): String {

            val result = RoastDeviationEngine.buildFromCurrentState()

            return """
Deviation Summary

Phase
${result.phase}

Severity
${result.severity}

Diagnosis
${result.diagnosis}

Cause
${result.cause}

Risk
${result.risk}
            """.trimIndent()
        }

        fun buildAssist(): String {

            val assist = RoastLiveAssistEngine.buildFromTelemetry()

            return """
Live Interpretation

Phase
${assist.phase}

Interpretation
${assist.interpretation}

Action
${assist.actionNow}

Heat Suggestion
${assist.heatCommand}

Air Suggestion
${assist.airCommand}

Target Window
${assist.targetWindow}
            """.trimIndent()
        }

        fun buildUnifiedCorrection(): String {

            val latest = RoastHistoryEngine.latest()

            if (latest == null) {
                return """
No roast history available

Finish a roast batch and save it to generate unified correction guidance
                """.trimIndent()
            }

            val correction = RoastCorrectionBridgeV2.buildFromBatch(latest.batchId)

            return correction.summary
        }

        fun buildHistory(): String {

            val latest = RoastHistoryEngine.latest()

            if (latest == null) {
                return """
No history recorded

Save a roast batch first
                """.trimIndent()
            }

            return """
Batch ID
${latest.batchId}

Status
${latest.batchStatus}

Process
${latest.process}

Replayability
${latest.replayability}

Risk
${latest.risk}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}
            """.trimIndent()
        }

        fun refreshAll() {

            deviationBody.text = buildDeviation()
            assistBody.text = buildAssist()
            correctionBody.text = buildUnifiedCorrection()
            historyBody.text = buildHistory()
        }

        deviationRefresh.setOnClickListener { refreshAll() }

        assistRefresh.setOnClickListener { refreshAll() }

        correctionRefresh.setOnClickListener { refreshAll() }

        openHistoryBtn.setOnClickListener {
            HistoryPage.show(context, container)
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }
}
