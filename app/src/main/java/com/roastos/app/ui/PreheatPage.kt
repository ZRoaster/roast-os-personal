package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.PreheatLiveInput
import com.roastos.app.RoastPreheatAssistEngine
import com.roastos.app.TelemetrySourceMode

object PreheatPage {

    private var simulatorElapsedSec = 0

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "PREHEAT CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Semi-automatic preheat assist with telemetry mode, target, action, beep text, and charge countdown"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY MODE"))

        val manualBtn = Button(context)
        manualBtn.text = "Use MANUAL"

        val simulatorBtn = Button(context)
        simulatorBtn.text = "Use SIMULATOR"

        val machineBtn = Button(context)
        machineBtn.text = "Use MACHINE"

        val telemetryBody = UiKit.bodyText(context, "")

        telemetryCard.addView(manualBtn)
        telemetryCard.addView(simulatorBtn)
        telemetryCard.addView(machineBtn)
        telemetryCard.addView(telemetryBody)

        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val targetCard = UiKit.card(context)
        targetCard.addView(UiKit.cardTitle(context, "PREHEAT TARGET"))
        val targetBody = UiKit.bodyText(context, "")
        targetCard.addView(targetBody)
        root.addView(targetCard)
        root.addView(UiKit.spacer(context))

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "MANUAL / SIM INPUT"))

        val currentTempInput = decimalInput(context, "Current Temp ℃", "205.0")
        val riseRateInput = decimalInput(context, "Rise Rate ℃/s", "0.18")
        val powerInput = intInput(context, "Current Power W", "540")
        val airflowInput = intInput(context, "Current Airflow Pa", "10")
        val drumInput = intInput(context, "Current Drum RPM", "60")
        val elapsedInput = intInput(context, "Elapsed Sec", "0")
        val holdElapsedInput = intInput(context, "Hold Elapsed Sec", "0")
        val ambientTempInput = decimalInput(context, "Ambient Temp ℃", "")
        val ambientRhInput = decimalInput(context, "Ambient RH %", "")

        val pushManualBtn = Button(context)
        pushManualBtn.text = "Push Manual Frame"

        val simStep10Btn = Button(context)
        simStep10Btn.text = "Simulator +10s"

        val simStep30Btn = Button(context)
        simStep30Btn.text = "Simulator +30s"

        val simResetBtn = Button(context)
        simResetBtn.text = "Reset Simulator"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Preheat Assist"

        inputCard.addView(currentTempInput)
        inputCard.addView(riseRateInput)
        inputCard.addView(powerInput)
        inputCard.addView(airflowInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(holdElapsedInput)
        inputCard.addView(ambientTempInput)
        inputCard.addView(ambientRhInput)
        inputCard.addView(pushManualBtn)
        inputCard.addView(simStep10Btn)
        inputCard.addView(simStep30Btn)
        inputCard.addView(simResetBtn)
        inputCard.addView(refreshBtn)

        root.addView(inputCard)
        root.addView(UiKit.spacer(context))

        val assistCard = UiKit.card(context)
        assistCard.addView(UiKit.cardTitle(context, "PREHEAT ASSIST"))
        val assistBody = UiKit.bodyText(context, "")
        assistCard.addView(assistBody)
        root.addView(assistCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)
        root.addView(statusCard)

        fun currentAmbientTemp(targetTemp: Double): Double {
            return ambientTempInput.text.toString().toDoubleOrNull()
                ?: com.roastos.app.AppState.lastPlannerInput?.envTemp
                ?: (targetTemp - 185.0).coerceAtLeast(20.0)
        }

        fun currentAmbientRh(): Double {
            return ambientRhInput.text.toString().toDoubleOrNull()
                ?: com.roastos.app.AppState.lastPlannerInput?.envRH
                ?: 50.0
        }

        fun refreshAll() {
            val target = RoastPreheatAssistEngine.buildTargetFromCurrentState()
            val telemetry = MachineTelemetryEngine.currentState()

            val live = PreheatLiveInput(
                currentTempC = telemetry.liveBtC
                    ?: currentTempInput.text.toString().toDoubleOrNull()
                    ?: 0.0,
                riseRateCPerSec = (
                    (
                        telemetry.liveRorCPerMin
                            ?: (riseRateInput.text.toString().toDoubleOrNull()?.times(60.0))
                            ?: 0.0
                    ) / 60.0
                ),
                currentPowerW = telemetry.livePowerW,
                elapsedSec = telemetry.liveElapsedSec,
                holdElapsedSec = holdElapsedInput.text.toString().toIntOrNull() ?: 0,
                ambientTempC = currentAmbientTemp(target.targetTempC),
                ambientRh = currentAmbientRh()
            )

            val result = RoastPreheatAssistEngine.assess(target, live)

            telemetryBody.text = MachineTelemetryEngine.summary()

            targetBody.text = """
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

            assistBody.text = result.summary
            statusBody.text = result.statusText

            if (telemetry.liveBtC != null) {
                currentTempInput.setText("%.1f".format(telemetry.liveBtC))
            }
            if (telemetry.liveRorCPerMin != null) {
                riseRateInput.setText("%.2f".format(telemetry.liveRorCPerMin / 60.0))
            }
            powerInput.setText(telemetry.livePowerW.toString())
            airflowInput.setText(telemetry.liveAirflowPa.toString())
            drumInput.setText(telemetry.liveDrumRpm.toString())
            elapsedInput.setText(telemetry.liveElapsedSec.toString())
        }

        manualBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refreshAll()
        }

        simulatorBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refreshAll()
        }

        machineBtn.setOnClickListener {
            MachineTelemetryEngine.connectMachine()
            refreshAll()
        }

        pushManualBtn.setOnClickListener {
            val ambientTemp = currentAmbientTemp(
                RoastPreheatAssistEngine.buildTargetFromCurrentState().targetTempC
            )
            val ambientRh = currentAmbientRh()

            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            MachineTelemetryEngine.pushManualFrame(
                bt = currentTempInput.text.toString().toDoubleOrNull() ?: 0.0,
                et = null,
                ror = (riseRateInput.text.toString().toDoubleOrNull()?.times(60.0)) ?: 0.0,
                powerW = powerInput.text.toString().toIntOrNull() ?: 540,
                airflowPa = airflowInput.text.toString().toIntOrNull() ?: 10,
                drumRpm = drumInput.text.toString().toIntOrNull() ?: 60,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                machineStateLabel = "Preheating",
                environmentTemp = ambientTemp,
                environmentHumidity = ambientRh
            )
            refreshAll()
        }

        simStep10Btn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsedSec += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)
            refreshAll()
        }

        simStep30Btn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsedSec += 30
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)
            refreshAll()
        }

        simResetBtn.setOnClickListener {
            simulatorElapsedSec = 0
            MachineTelemetryEngine.reset()
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refreshAll()
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        refreshAll()

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
