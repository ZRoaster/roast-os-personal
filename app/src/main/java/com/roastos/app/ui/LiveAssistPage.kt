package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.TelemetrySourceMode

object LiveAssistPage {

    private var simulatorElapsedSec = 0

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "LIVE ASSIST"))
        root.addView(UiKit.pageSubtitle(context, "Real-time telemetry and roast assist"))
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY"))

        val telemetryBody = UiKit.bodyText(context, "")

        val manualBtn = Button(context)
        manualBtn.text = "Manual Mode"

        val simulatorBtn = Button(context)
        simulatorBtn.text = "Simulator Mode"

        val machineBtn = Button(context)
        machineBtn.text = "Machine Mode"

        telemetryCard.addView(manualBtn)
        telemetryCard.addView(simulatorBtn)
        telemetryCard.addView(machineBtn)
        telemetryCard.addView(telemetryBody)

        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "MANUAL INPUT"))

        val btInput = decimalInput(context, "BT ℃", "200")
        val rorInput = decimalInput(context, "RoR ℃/s", "0.15")
        val powerInput = intInput(context, "Power W", "600")
        val airflowInput = intInput(context, "Airflow Pa", "10")
        val drumInput = intInput(context, "Drum RPM", "60")
        val elapsedInput = intInput(context, "Elapsed Sec", "0")

        val pushBtn = Button(context)
        pushBtn.text = "Push Frame"

        val sim10 = Button(context)
        sim10.text = "Simulator +10s"

        val sim30 = Button(context)
        sim30.text = "Simulator +30s"

        val simReset = Button(context)
        simReset.text = "Reset Simulator"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh"

        inputCard.addView(btInput)
        inputCard.addView(rorInput)
        inputCard.addView(powerInput)
        inputCard.addView(airflowInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(pushBtn)
        inputCard.addView(sim10)
        inputCard.addView(sim30)
        inputCard.addView(simReset)
        inputCard.addView(refreshBtn)

        root.addView(inputCard)

        fun refresh() {
            telemetryBody.text = MachineTelemetryEngine.summary()
        }

        manualBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refresh()
        }

        simulatorBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        machineBtn.setOnClickListener {
            MachineTelemetryEngine.connectMachine()
            refresh()
        }

        pushBtn.setOnClickListener {

            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)

            MachineTelemetryEngine.pushManualFrame(
                bt = btInput.text.toString().toDoubleOrNull() ?: 0.0,
                et = null,
                ror = (rorInput.text.toString().toDoubleOrNull()?.times(60.0)) ?: 0.0,
                powerW = powerInput.text.toString().toIntOrNull() ?: 600,
                airflowPa = airflowInput.text.toString().toIntOrNull() ?: 10,
                drumRpm = drumInput.text.toString().toIntOrNull() ?: 60,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                machineStateLabel = "Roasting",
                environmentTemp = 25.0,
                environmentHumidity = 50.0
            )

            refresh()
        }

        sim10.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsedSec += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)
            refresh()
        }

        sim30.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsedSec += 30
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)
            refresh()
        }

        simReset.setOnClickListener {
            simulatorElapsedSec = 0
            MachineTelemetryEngine.reset()
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        refreshBtn.setOnClickListener {
            refresh()
        }

        refresh()

        scroll.addView(root)
        container.addView(scroll)
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
