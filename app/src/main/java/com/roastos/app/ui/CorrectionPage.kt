package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastCorrectionBridgeV2
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
                "Roast diagnostics, live interpretation, and unified correction guidance"
            )
        )

        root.addView(UiKit.spacer(context))

        val assistCard = UiKit.card(context)
        assistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))

        val assistBody = UiKit.bodyText(context, "")
        assistCard.addView(assistBody)

        val assistRefresh = UiKit.primaryButton(context, "Refresh Assist")
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
        historyCard.addView(UiKit.cardTitle(context, "LATEST BATCH"))

        val historyBody = UiKit.bodyText(context, "")
        historyCard.addView(historyBody)

        val openHistoryBtn = UiKit.secondaryButton(context, "Open History")
        historyCard.addView(openHistoryBtn)

        root.addView(historyCard)

        fun buildAssist(): String {

            val assist = RoastLiveAssistEngine.buildFromTelemetry()

            return """
Phase
${assist.phase}

Interpretation
${assist.interpretation}

Action
${assist.actionNow}
""".trimIndent()
        }

        fun buildCorrection(): String {

            val latest = RoastHistoryEngine.latest()

            if (latest == null) {
                return """
No roast history available

Finish a roast batch to generate correction guidance
""".trimIndent()
            }

            val correction = RoastCorrectionBridgeV2.buildFromBatch(latest.batchId)

            return correction.summary
        }

        fun buildHistory(): String {

            val latest = RoastHistoryEngine.latest()

            if (latest == null) {
                return """
No roast history recorded
""".trimIndent()
            }

            return """
Batch ID
${latest.batchId}

Process
${latest.process}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}

Baseline
${latest.baselineLabel ?: "None"}
""".trimIndent()
        }

        fun refreshAll() {

            assistBody.text = buildAssist()
            correctionBody.text = buildCorrection()
            historyBody.text = buildHistory()
        }

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
