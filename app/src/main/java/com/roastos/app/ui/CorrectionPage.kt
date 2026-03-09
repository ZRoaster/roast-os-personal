package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.RoastCorrectionBridge
import com.roastos.app.RoastCorrectionBridgeV2
import com.roastos.app.RoastDeviationEngine
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastLiveAssistEngine
import com.roastos.app.TelemetrySourceMode

object CorrectionPage {

    private var simulatorElapsed = 0

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "CORRECTION CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Live assist, deviation diagnosis, current correction, and latest unified correction"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY STATUS"))
        val telemetryBody = UiKit.bodyText(context, "")
        telemetryCard.addView(telemetryBody)
        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val liveAssistCard = UiKit.card(context)
        liveAssistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))
        val liveAssistBody = UiKit.bodyText(context, "")
        liveAssistCard.addView(liveAssistBody)
        root.addView(liveAssistCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        controlCard.addView(UiKit.cardTitle(context, "TELEMETRY CONTROL"))

        val manualBtn = Button(context)
        manualBtn.text = "Manual Mode"

        val simBtn = Button(context)
        simBtn.text = "Simulator Mode"

        val simStep10Btn = Button(context)
        simStep10Btn.text = "Sim +10s"

        val simStep30Btn = Button(context)
        simStep30Btn.text = "Sim +30s"

        val simResetBtn = Button(context)
        simResetBtn.text = "Reset Simulator"

        val machineBtn = Button(context)
        machineBtn.text = "Machine Mode"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Correction"

        controlCard.addView(manualBtn)
        controlCard.addView(simBtn)
        controlCard.addView(simStep10Btn)
        controlCard.addView(simStep30Btn)
        controlCard.addView(simResetBtn)
        controlCard.addView(machineBtn)
        controlCard.addView(refreshBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val diagnosisCard = UiKit.card(context)
        diagnosisCard.addView(UiKit.cardTitle(context, "DEVIATION DIAGNOSIS"))
        val diagnosisBody = UiKit.bodyText(context, "")
        diagnosisCard.addView(diagnosisBody)
        root.addView(diagnosisCard)
        root.addView(UiKit.spacer(context))

        val currentCorrectionCard = UiKit.card(context)
        currentCorrectionCard.addView(UiKit.cardTitle(context, "CURRENT CORRECTION"))
        val currentCorrectionBody = UiKit.bodyText(context, "")
        currentCorrectionCard.addView(currentCorrectionBody)
        root.addView(currentCorrectionCard)
        root.addView(UiKit.spacer(context))

        val unifiedCard = UiKit.card(context)
        unifiedCard.addView(UiKit.cardTitle(context, "LATEST UNIFIED CORRECTION"))
        val unifiedBody = UiKit.bodyText(context, "")
        unifiedCard.addView(unifiedBody)
        root.addView(unifiedCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        historyCard.addView(UiKit.cardTitle(context, "HISTORY SUMMARY"))
        val historyBody = UiKit.bodyText(context, "")
        historyCard.addView(historyBody)
        root.addView(historyCard)

        fun buildUnifiedCorrectionText(): String {
            val latest = RoastHistoryEngine.latest()
                ?: return """
No saved roast history yet

Next Step
Finish a batch and save it into history first
                """.trimIndent()

            val unified = RoastCorrectionBridgeV2.buildFromBatch(latest.batchId)

            return """
Latest Batch
${latest.batchId}

${unified.summary}
            """.trimIndent()
        }

        fun refreshAll() {
            telemetryBody.text = MachineTelemetryEngine.summary()
            liveAssistBody.text = RoastLiveAssistEngine.buildFromTelemetry().summary
            diagnosisBody.text = RoastDeviationEngine.diagnoseFromCurrentState().summary
            currentCorrectionBody.text = RoastCorrectionBridge.buildFromCurrentState().summary
            unifiedBody.text = buildUnifiedCorrectionText()
            historyBody.text = RoastHistoryEngine.summary()
        }

        manualBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refreshAll()
        }

        simBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refreshAll()
        }

        simStep10Btn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refreshAll()
        }

        simStep30Btn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 30
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refreshAll()
        }

        simResetBtn.setOnClickListener {
            simulatorElapsed = 0
            MachineTelemetryEngine.reset()
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refreshAll()
        }

        machineBtn.setOnClickListener {
            MachineTelemetryEngine.connectMachine()
            refreshAll()
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }
}
