package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.PlannerBaseline
import com.roastos.app.PlannerBaselineStore
import com.roastos.app.RoastLiveAssistEngine
import com.roastos.app.TelemetrySourceMode

object RoastPage {

    private var simulatorElapsed = 0

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Cockpit view driven by MachineTelemetryEngine, RoastLiveAssistEngine, and PlannerBaselineStore"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY STATUS"))
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

        val cockpitCard = UiKit.card(context)
        cockpitCard.addView(UiKit.cardTitle(context, "COCKPIT"))
        val cockpitBody = UiKit.bodyText(context, "")
        cockpitCard.addView(cockpitBody)
        root.addView(cockpitCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        controlCard.addView(UiKit.cardTitle(context, "TELEMETRY CONTROL"))
        controlCard.addView(UiKit.captionText(context, "Use simulator or machine mode to drive live roast state."))

        val manualBtn = UiKit.secondaryButton(context, "Manual Mode")
        val simBtn = UiKit.secondaryButton(context, "Simulator Mode")
        val simStep10 = UiKit.primaryButton(context, "Sim +10s")
        val simStep30 = UiKit.primaryButton(context, "Sim +30s")
        val simReset = UiKit.dangerButton(context, "Reset Simulator")
        val machineBtn = UiKit.secondaryButton(context, "Machine Mode")

        controlCard.addView(manualBtn)
        controlCard.addView(simBtn)
        controlCard.addView(simStep10)
        controlCard.addView(simStep30)
        controlCard.addView(simReset)
        controlCard.addView(machineBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val curveCard = UiKit.card(context)
        curveCard.addView(UiKit.cardTitle(context, "ROAST CURVE"))
        val curveBody = UiKit.bodyText(context, "")
        curveCard.addView(curveBody)
        root.addView(curveCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "ROAST STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)
        root.addView(statusCard)

        fun refresh() {
            val telemetry = MachineTelemetryEngine.currentState()
            val assist = RoastLiveAssistEngine.buildFromTelemetry()
            val baseline = PlannerBaselineStore.current()

            telemetryBody.text = MachineTelemetryEngine.summary()
            baselineBody.text = buildBaselineText()
            cockpitBody.text = assist.summary

            val bt = telemetry.liveBtC ?: 0.0
            val et = telemetry.liveEtC
            val ror = telemetry.liveRorCPerMin ?: 0.0
            val power = telemetry.livePowerW
            val air = telemetry.liveAirflowPa
            val drum = telemetry.liveDrumRpm
            val time = telemetry.liveElapsedSec
            val machineState = telemetry.machineState

            curveBody.text = """
Curve Monitor

BT
${"%.1f".format(bt)}℃

ET
${et?.let { "%.1f".format(it) + "℃" } ?: "-"}

ROR
${"%.1f".format(ror)}℃/min

Elapsed
${time}s

Interpretation
${assist.interpretation}

Baseline Reference
${buildBaselineReferenceText(baseline, time)}
            """.trimIndent()

            statusBody.text = """
Machine State
$machineState

BT
${"%.1f".format(bt)}℃

ROR
${"%.1f".format(ror)}℃/min

Power
${power}W

Air
${air}Pa

Drum
${drum}rpm

Elapsed
${time}s

Source Mode
${telemetry.mode}
            """.trimIndent()
        }

        manualBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refresh()
        }

        simBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        simStep10.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refresh()
        }

        simStep30.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 30
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refresh()
        }

        simReset.setOnClickListener {
            simulatorElapsed = 0
            MachineTelemetryEngine.reset()
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        machineBtn.setOnClickListener {
            MachineTelemetryEngine.connectMachine()
            refresh()
        }

        refresh()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildBaselineText(): String {
        val baseline = PlannerBaselineStore.current()
            ?: return """
Status
No active planner baseline

Next Step
Apply profile suggestion or capture current planner result as baseline
            """.trimIndent()

        val match = PlannerBaselineStore.evaluateMatchAgainstCurrentInput()

        return """
Source
${baseline.source}

Label
${baseline.label}

Match Grade
${formatBaselineMatch(match?.grade?.name)}

Turning
${baseline.turningSec?.toString()?.plus("s") ?: "-"}

Yellow
${baseline.yellowSec?.toString()?.plus("s") ?: "-"}

FC
${baseline.fcSec?.toString()?.plus("s") ?: "-"}

Drop
${baseline.dropSec?.toString()?.plus("s") ?: "-"}
        """.trimIndent()
    }

    private fun buildBaselineReferenceText(
        baseline: PlannerBaseline?,
        elapsedSec: Int
    ): String {
        baseline ?: return "No baseline active"

        return when {
            baseline.turningSec != null && elapsedSec < baseline.turningSec ->
                "Approaching Turning anchor"
            baseline.yellowSec != null && elapsedSec < baseline.yellowSec ->
                "Working toward Yellow anchor"
            baseline.fcSec != null && elapsedSec < baseline.fcSec ->
                "Working toward FC anchor"
            baseline.dropSec != null && elapsedSec < baseline.dropSec ->
                "Working toward Drop anchor"
            else ->
                "Past or near final baseline anchors"
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
