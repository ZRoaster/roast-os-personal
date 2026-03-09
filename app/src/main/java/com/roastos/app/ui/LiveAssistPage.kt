package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.RoastLiveAssistEngine
import com.roastos.app.TelemetrySourceMode

object LiveAssistPage {

    private var simulatorElapsed = 0

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "LIVE ASSIST"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Live driving assist powered by MachineTelemetryEngine and RoastLiveAssistEngine"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY STATUS"))
        val telemetryBody = UiKit.bodyText(context, "")
        telemetryCard.addView(telemetryBody)
        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val assistCard = UiKit.card(context)
        assistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))
        val assistBody = UiKit.bodyText(context, "")
        assistCard.addView(assistBody)
        root.addView(assistCard)
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

        attachLiveInputPanel(
            context = context,
            parent = root,
            onDataChanged = {}
        )
        root.addView(UiKit.spacer(context))

        fun refresh() {
            telemetryBody.text = MachineTelemetryEngine.summary()
            assistBody.text = buildLiveAssist()
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

    fun buildLiveAssist(): String {
        val telemetry = MachineTelemetryEngine.currentState()
        val assist = RoastLiveAssistEngine.buildFromTelemetry()

        val bt = telemetry.liveBtC?.let { "%.1f".format(it) + "℃" } ?: "-"
        val et = telemetry.liveEtC?.let { "%.1f".format(it) + "℃" } ?: "-"
        val ror = telemetry.liveRorCPerMin?.let { "%.1f".format(it) + "℃/min" } ?: "-"
        val elapsed = "${telemetry.liveElapsedSec}s"

        return """
${assist.summary}

LIVE INPUT SNAPSHOT
BT
$bt

ET
$et

ROR
$ror

Elapsed
$elapsed

Machine State
${telemetry.machineState}

Source Mode
${telemetry.mode}
        """.trimIndent()
    }

    fun attachLiveInputPanel(
        context: Context,
        parent: LinearLayout,
        onDataChanged: () -> Unit
    ) {
        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "MANUAL TELEMETRY INPUT"))

        val btInput = decimalInput(context, "BT ℃", "")
        val etInput = decimalInput(context, "ET ℃", "")
        val rorInput = decimalInput(context, "ROR ℃/min", "")
        val powerInput = intInput(context, "Power W", "540")
        val airInput = intInput(context, "Airflow Pa", "10")
        val drumInput = intInput(context, "Drum RPM", "60")
        val elapsedInput = intInput(context, "Elapsed Sec", "0")

        val turningInput = intInput(context, "Turning Sec", "")
        val yellowInput = intInput(context, "Yellow Sec", "")
        val fcInput = intInput(context, "FC Sec", "")
        val dropInput = intInput(context, "Drop Sec", "")

        val pushBtn = Button(context)
        pushBtn.text = "Push Manual Frame"

        val statusBody = UiKit.bodyText(context, "")

        inputCard.addView(btInput)
        inputCard.addView(etInput)
        inputCard.addView(rorInput)
        inputCard.addView(powerInput)
        inputCard.addView(airInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(turningInput)
        inputCard.addView(yellowInput)
        inputCard.addView(fcInput)
        inputCard.addView(dropInput)
        inputCard.addView(pushBtn)
        inputCard.addView(statusBody)

        pushBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)

            MachineTelemetryEngine.pushManualFrame(
                btC = btInput.text.toString().toDoubleOrNull(),
                etC = etInput.text.toString().toDoubleOrNull(),
                rorCPerMin = rorInput.text.toString().toDoubleOrNull(),
                powerW = powerInput.text.toString().toIntOrNull(),
                airflowPa = airInput.text.toString().toIntOrNull(),
                drumRpm = drumInput.text.toString().toIntOrNull(),
                elapsedSec = elapsedInput.text.toString().toIntOrNull(),
                turningSec = turningInput.text.toString().toIntOrNull(),
                yellowSec = yellowInput.text.toString().toIntOrNull(),
                fcSec = fcInput.text.toString().toIntOrNull(),
                dropSec = dropInput.text.toString().toIntOrNull(),
                machineState = "Running"
            )

            val assist = RoastLiveAssistEngine.buildFromTelemetry()
            statusBody.text = """
Manual frame pushed

PHASE
${assist.phase}

RISK
${assist.risk}

ACTION NOW
${assist.actionNow}

NEXT WATCHPOINT
${assist.nextWatchpoint}
            """.trimIndent()

            onDataChanged()
        }

        parent.addView(inputCard)
    }

    private fun decimalInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        input.setText(defaultText)
        return input
    }

    private fun intInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        input.setText(defaultText)
        return input
    }
}
