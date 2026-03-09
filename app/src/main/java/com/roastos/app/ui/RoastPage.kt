package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.DecisionEngine
import com.roastos.app.EnergyEngine
import com.roastos.app.EnergyState
import com.roastos.app.MachineControlCapabilities
import com.roastos.app.MachineControlPlanner
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.TelemetrySourceMode

object RoastPage {

    private var simulatorElapsedSec = 0

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Telemetry, energy state, control capability, decision, and control planner cockpit"
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

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "LIVE INPUT"))

        val btInput = decimalInput(context, "BT ℃", "165.0")
        val rorInput = decimalInput(context, "RoR ℃/s", "0.12")
        val powerInput = intInput(context, "Power W", "680")
        val airflowInput = intInput(context, "Airflow Pa", "10")
        val drumInput = intInput(context, "Drum RPM", "60")
        val elapsedInput = intInput(context, "Elapsed Sec", "240")
        val envTempInput = decimalInput(context, "Env Temp ℃", "25.0")
        val envRhInput = decimalInput(context, "Env RH %", "50.0")

        val pushManualBtn = Button(context)
        pushManualBtn.text = "Push Manual Frame"

        val sim10Btn = Button(context)
        sim10Btn.text = "Simulator +10s"

        val sim30Btn = Button(context)
        sim30Btn.text = "Simulator +30s"

        val simResetBtn = Button(context)
        simResetBtn.text = "Reset Simulator"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Cockpit"

        inputCard.addView(btInput)
        inputCard.addView(rorInput)
        inputCard.addView(powerInput)
        inputCard.addView(airflowInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(envTempInput)
        inputCard.addView(envRhInput)
        inputCard.addView(pushManualBtn)
        inputCard.addView(sim10Btn)
        inputCard.addView(sim30Btn)
        inputCard.addView(simResetBtn)
        inputCard.addView(refreshBtn)

        root.addView(inputCard)
        root.addView(UiKit.spacer(context))

        val capabilityCard = UiKit.card(context)
        capabilityCard.addView(UiKit.cardTitle(context, "CONTROL CAPABILITY"))
        val capabilityBody = UiKit.bodyText(context, "")
        capabilityCard.addView(capabilityBody)

        root.addView(capabilityCard)
        root.addView(UiKit.spacer(context))

        val energyCard = UiKit.card(context)
        energyCard.addView(UiKit.cardTitle(context, "ENERGY ENGINE"))
        val energyBody = UiKit.bodyText(context, "")
        energyCard.addView(energyBody)

        root.addView(energyCard)
        root.addView(UiKit.spacer(context))

        val decisionCard = UiKit.card(context)
        decisionCard.addView(UiKit.cardTitle(context, "DECISION"))
        val decisionBody = UiKit.bodyText(context, "")
        decisionCard.addView(decisionBody)

        root.addView(decisionCard)
        root.addView(UiKit.spacer(context))

        val plannerCard = UiKit.card(context)
        plannerCard.addView(UiKit.cardTitle(context, "CONTROL PLANNER"))
        val plannerBody = UiKit.bodyText(context, "")
        plannerCard.addView(plannerBody)

        root.addView(plannerCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)

        root.addView(statusCard)

        fun currentMachineState() = MachineStateEngine.buildState(
            powerW = MachineTelemetryEngine.currentState().livePowerW,
            airflowPa = MachineTelemetryEngine.currentState().liveAirflowPa,
            drumRpm = MachineTelemetryEngine.currentState().liveDrumRpm,
            beanTemp = MachineTelemetryEngine.currentState().liveBtC
                ?: btInput.text.toString().toDoubleOrNull()
                ?: 0.0,
            ror = MachineTelemetryEngine.currentState().liveRorCPerMin
                ?: ((rorInput.text.toString().toDoubleOrNull() ?: 0.0) * 60.0),
            elapsedSec = MachineTelemetryEngine.currentState().liveElapsedSec,
            environmentTemp = envTempInput.text.toString().toDoubleOrNull() ?: 25.0,
            environmentHumidity = envRhInput.text.toString().toDoubleOrNull() ?: 50.0
        )

        fun buildDecision(
            energyState: EnergyState,
            ror: Double
        ): DecisionEngine.DecisionResult {
            return when {
                energyState == EnergyState.DEFICIT || ror < 2.0 -> {
                    DecisionEngine.DecisionResult(
                        suggestion = "Increase heat",
                        severity = "HIGH",
                        reason = "Low energy or collapsing ROR"
                    )
                }

                energyState == EnergyState.LOW || ror < 4.5 -> {
                    DecisionEngine.DecisionResult(
                        suggestion = "Increase heat slightly",
                        severity = "MEDIUM",
                        reason = "Momentum is softer than desired"
                    )
                }

                energyState == EnergyState.HIGH && ror > 10.0 -> {
                    DecisionEngine.DecisionResult(
                        suggestion = "Reduce heat or increase airflow",
                        severity = "MEDIUM",
                        reason = "Energy is excessive and ROR is aggressive"
                    )
                }

                ror > 12.0 -> {
                    DecisionEngine.DecisionResult(
                        suggestion = "Reduce heat",
                        severity = "HIGH",
                        reason = "ROR is too aggressive"
                    )
                }

                else -> {
                    DecisionEngine.DecisionResult(
                        suggestion = "Maintain current settings",
                        severity = "LOW",
                        reason = "Roast progressing normally"
                    )
                }
            }
        }

        fun refreshAll() {
            val profile = MachineProfiles.HB_M2SE
            val capability = MachineControlCapabilities.defaultFor(profile)
            val telemetry = MachineTelemetryEngine.currentState()
            val machineState = currentMachineState()
            val energy = EnergyEngine.evaluate(profile, machineState)
            val decision = buildDecision(energy.stateEnum, machineState.ror)
            val plan = MachineControlPlanner.buildPlan(
                profile = profile,
                capability = capability,
                machineState = machineState,
                energy = energy,
                decision = decision
            )

            telemetryBody.text = MachineTelemetryEngine.summary()
            capabilityBody.text = capability.summary()
            energyBody.text = energy.summary

            decisionBody.text = """
Decision

Suggestion
${decision.suggestion}

Severity
${decision.severity}

Reason
${decision.reason}
            """.trimIndent()

            plannerBody.text = plan.summary

            statusBody.text = """
Cockpit Status

Profile
${profile.name}

Telemetry Mode
${telemetry.mode}

Connected
${if (telemetry.isConnected) "Yes" else "No"}

BT
${"%.1f".format(machineState.beanTemp)}℃

RoR
${"%.1f".format(machineState.ror)}℃/min

Power
${machineState.powerW}W

Airflow
${machineState.airflowPa}Pa

Drum
${machineState.drumRpm}rpm

Primary Command
${plan.primaryCommand.type} / ${plan.primaryCommand.status}

Secondary Command
${plan.secondaryCommand?.type ?: "-"}
            """.trimIndent()

            if (telemetry.liveBtC != null) {
                btInput.setText("%.1f".format(telemetry.liveBtC))
            }

            if (telemetry.liveRorCPerMin != null) {
                rorInput.setText("%.2f".format(telemetry.liveRorCPerMin / 60.0))
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
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            MachineTelemetryEngine.pushManualFrame(
                bt = btInput.text.toString().toDoubleOrNull() ?: 0.0,
                et = null,
                ror = (rorInput.text.toString().toDoubleOrNull()?.times(60.0)) ?: 0.0,
                powerW = powerInput.text.toString().toIntOrNull() ?: 680,
                airflowPa = airflowInput.text.toString().toIntOrNull() ?: 10,
                drumRpm = drumInput.text.toString().toIntOrNull() ?: 60,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                machineStateLabel = "Roasting",
                environmentTemp = envTempInput.text.toString().toDoubleOrNull() ?: 25.0,
                environmentHumidity = envRhInput.text.toString().toDoubleOrNull() ?: 50.0
            )
            refreshAll()
        }

        sim10Btn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsedSec += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)
            refreshAll()
        }

        sim30Btn.setOnClickListener {
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
