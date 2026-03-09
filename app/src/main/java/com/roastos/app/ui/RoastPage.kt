package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
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
                "Cockpit view driven by MachineTelemetryEngine"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY STATUS"))
        val telemetryBody = UiKit.bodyText(context, "")
        telemetryCard.addView(telemetryBody)
        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val cockpitCard = UiKit.card(context)
        cockpitCard.addView(UiKit.cardTitle(context, "COCKPIT"))
        val cockpitBody = UiKit.bodyText(context, "")
        cockpitCard.addView(cockpitBody)
        root.addView(cockpitCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        controlCard.addView(UiKit.cardTitle(context, "TELEMETRY CONTROL"))

        val manualBtn = Button(context)
        manualBtn.text = "Manual Mode"

        val simBtn = Button(context)
        simBtn.text = "Simulator Mode"

        val simStep10 = Button(context)
        simStep10.text = "Sim +10s"

        val simStep30 = Button(context)
        simStep30.text = "Sim +30s"

        val simReset = Button(context)
        simReset.text = "Reset Simulator"

        val machineBtn = Button(context)
        machineBtn.text = "Machine Mode"

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

            telemetryBody.text = MachineTelemetryEngine.summary()

            val bt = telemetry.liveBtC ?: 0.0
            val et = telemetry.liveEtC
            val ror = telemetry.liveRorCPerMin ?: 0.0
            val power = telemetry.livePowerW
            val air = telemetry.liveAirflowPa
            val drum = telemetry.liveDrumRpm
            val time = telemetry.liveElapsedSec
            val machineState = telemetry.machineState

            val phase = buildPhase(bt = bt, elapsedSec = time)
            val risk = buildRisk(ror = ror, elapsedSec = time)
            val action = buildActionNow(bt = bt, ror = ror, elapsedSec = time)
            val watchpoint = buildNextWatchpoint(bt = bt, ror = ror, elapsedSec = time)

            cockpitBody.text = """
PHASE
$phase

RISK
$risk

ACTION NOW
$action

NEXT WATCHPOINT
$watchpoint
            """.trimIndent()

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
${buildCurveInterpretation(bt = bt, ror = ror, elapsedSec = time)}
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

    private fun buildPhase(
        bt: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Charge / Early Front-End"
            bt <= 120.0 -> "Drying"
            bt <= 160.0 -> "Drying → Maillard Transition"
            bt <= 195.0 -> "Maillard / Pre-FC"
            else -> "Development / Late Roast"
        }
    }

    private fun buildRisk(
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Low"
            ror >= 10.8 -> "High"
            ror <= 7.0 && elapsedSec >= 240 -> "High"
            ror >= 9.5 || (ror <= 8.0 && elapsedSec >= 180) -> "Medium"
            else -> "Low"
        }
    }

    private fun buildActionNow(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 ->
                "Watch early momentum and avoid over-reacting too fast"

            ror >= 10.8 ->
                "Reduce heat earlier and watch late acceleration"

            ror <= 7.0 && elapsedSec >= 240 ->
                "Protect energy immediately and avoid crash into crack"

            bt <= 120.0 ->
                "Maintain stable drying and avoid unnecessary aggressive changes"

            bt <= 160.0 ->
                "Guide transition cleanly and keep momentum into Maillard"

            bt <= 195.0 ->
                "Monitor ROR carefully and prepare first-crack entry structure"

            else ->
                "Control development and prepare disciplined drop timing"
        }
    }

    private fun buildNextWatchpoint(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 ->
                "First turning response and early front-end strength"

            bt <= 120.0 ->
                "Drying completion and momentum into Yellow"

            bt <= 160.0 ->
                "Yellow timing and middle-phase energy continuity"

            bt <= 195.0 && ror >= 10.0 ->
                "Spike risk before first crack"

            bt <= 195.0 ->
                "FC approach timing and pre-FC ROR stability"

            else ->
                "Development time, drop point, and finish cleanliness"
        }
    }

    private fun buildCurveInterpretation(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Front-end phase, monitor early momentum"
            ror >= 10.8 -> "Late acceleration risk is high"
            ror <= 7.0 && elapsedSec >= 240 -> "Energy may be collapsing"
            ror in 8.0..9.8 -> "ROR looks relatively stable"
            bt <= 120.0 -> "Likely early drying stage"
            bt <= 160.0 -> "Likely drying to Maillard transition"
            bt <= 195.0 -> "Likely Maillard / pre-FC stage"
            else -> "Likely development stage"
        }
    }
}
