package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
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
                "Cockpit view driven by MachineTelemetryEngine and RoastLiveAssistEngine"
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
            val assist = RoastLiveAssistEngine.buildFromTelemetry()

            telemetryBody.text = MachineTelemetryEngine.summary()
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
}
