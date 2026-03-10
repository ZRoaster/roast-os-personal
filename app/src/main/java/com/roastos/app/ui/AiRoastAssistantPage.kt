package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.EnergyEngine
import com.roastos.app.MachineControlCapabilities
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.RoastAiAttachment
import com.roastos.app.RoastAiBrain
import com.roastos.app.RoastAiDecisionEngine
import com.roastos.app.RoastAiInputModality
import com.roastos.app.RoastAiProviderType
import com.roastos.app.RoastAiService
import com.roastos.app.RoastAiStyleGoal
import com.roastos.app.TelemetrySourceMode
import kotlinx.coroutines.runBlocking

object AiRoastAssistantPage {

    private var simulatorElapsedSec = 0

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "AI ROAST ASSISTANT"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Realtime AI roast interpretation, decision support, and action guidance."
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

        val styleNameInput = textInput(context, "Style Name", "Balanced")
        val flavorInput = textInput(context, "Flavor Direction", "Sweet and clean")
        val acidityInput = textInput(context, "Acidity Preference", "Medium")
        val sweetnessInput = textInput(context, "Sweetness Preference", "High")
        val bodyInput = textInput(context, "Body Preference", "Medium")
        val clarityInput = textInput(context, "Clarity Preference", "High")
        val developmentInput = textInput(context, "Development Preference", "Medium")
        val promptInput = textInput(context, "Ask AI", "How should I manage this roast right now?")

        val pushManualBtn = Button(context)
        pushManualBtn.text = "Push Manual Frame"

        val sim10Btn = Button(context)
        sim10Btn.text = "Simulator +10s"

        val sim30Btn = Button(context)
        sim30Btn.text = "Simulator +30s"

        val simResetBtn = Button(context)
        simResetBtn.text = "Reset Simulator"

        val askAiBtn = Button(context)
        askAiBtn.text = "Ask AI"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Assistant"

        inputCard.addView(btInput)
        inputCard.addView(rorInput)
        inputCard.addView(powerInput)
        inputCard.addView(airflowInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(envTempInput)
        inputCard.addView(envRhInput)
        inputCard.addView(styleNameInput)
        inputCard.addView(flavorInput)
        inputCard.addView(acidityInput)
        inputCard.addView(sweetnessInput)
        inputCard.addView(bodyInput)
        inputCard.addView(clarityInput)
        inputCard.addView(developmentInput)
        inputCard.addView(promptInput)
        inputCard.addView(pushManualBtn)
        inputCard.addView(sim10Btn)
        inputCard.addView(sim30Btn)
        inputCard.addView(simResetBtn)
        inputCard.addView(askAiBtn)
        inputCard.addView(refreshBtn)

        root.addView(inputCard)
        root.addView(UiKit.spacer(context))

        val brainCard = UiKit.card(context)
        brainCard.addView(UiKit.cardTitle(context, "AI BRAIN"))
        val brainBody = UiKit.bodyText(context, "")
        brainCard.addView(brainBody)

        root.addView(brainCard)
        root.addView(UiKit.spacer(context))

        val aiResponseCard = UiKit.card(context)
        aiResponseCard.addView(UiKit.cardTitle(context, "AI RESPONSE"))
        val aiResponseBody = UiKit.bodyText(context, "")
        aiResponseCard.addView(aiResponseBody)

        root.addView(aiResponseCard)
        root.addView(UiKit.spacer(context))

        val aiDecisionCard = UiKit.card(context)
        aiDecisionCard.addView(UiKit.cardTitle(context, "AI DECISION"))
        val aiDecisionBody = UiKit.bodyText(context, "")
        aiDecisionCard.addView(aiDecisionBody)

        root.addView(aiDecisionCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)

        root.addView(statusCard)

        var latestAiResponseText = "No AI response yet."
        var latestAiDecisionText = "No AI decision yet."

        fun currentStyleGoal(): RoastAiStyleGoal {
            return RoastAiStyleGoal(
                styleName = styleNameInput.text.toString().ifBlank { "Balanced" },
                flavorDirection = flavorInput.text.toString().ifBlank { "Balanced sweetness" },
                acidityPreference = acidityInput.text.toString().ifBlank { "Medium" },
                sweetnessPreference = sweetnessInput.text.toString().ifBlank { "High" },
                bodyPreference = bodyInput.text.toString().ifBlank { "Medium" },
                clarityPreference = clarityInput.text.toString().ifBlank { "High" },
                developmentPreference = developmentInput.text.toString().ifBlank { "Medium" },
                notes = ""
            )
        }

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

        fun refreshAll() {
            val profile = MachineProfiles.HB_M2SE
            val capability = MachineControlCapabilities.defaultFor(profile)
            val telemetry = MachineTelemetryEngine.currentState()
            val machineState = currentMachineState()
            val energy = EnergyEngine.evaluate(profile, machineState)
            val styleGoal = currentStyleGoal()

            val brain = RoastAiBrain.build(
                profile = profile,
                capability = capability,
                machineState = machineState,
                energy = energy,
                stability = null,
                styleGoal = styleGoal
            )

            telemetryBody.text = MachineTelemetryEngine.summary()
            brainBody.text = brain.summary()
            aiResponseBody.text = latestAiResponseText
            aiDecisionBody.text = latestAiDecisionText

            statusBody.text = """
AI Roast Assistant Status

Machine
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

Style
${styleGoal.styleName}
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

        askAiBtn.setOnClickListener {
            val profile = MachineProfiles.HB_M2SE
            val capability = MachineControlCapabilities.defaultFor(profile)
            val machineState = currentMachineState()
            val energy = EnergyEngine.evaluate(profile, machineState)
            val styleGoal = currentStyleGoal()

            val contextPayload = RoastAiBrain.toContext(
                machineProfile = profile,
                machineState = machineState,
                telemetryFrame = null,
                controlCapability = capability,
                energySnapshot = energy,
                stabilityResult = null,
                drivingAdvice = null,
                decisionResult = null,
                controlPlan = null,
                executionSummary = null,
                styleGoal = styleGoal,
                userPrompt = promptInput.text.toString(),
                operatorNote = "Generated from AI Roast Assistant page",
                attachments = listOf(
                    RoastAiAttachment(
                        id = "manual_prompt",
                        modality = RoastAiInputModality.TEXT,
                        label = "Operator Prompt",
                        contentHint = promptInput.text.toString()
                    )
                )
            )

            try {
                val aiResponse = runBlocking {
                    RoastAiService.generateRealtimeCoaching(
                        context = contextPayload,
                        providerType = RoastAiProviderType.MOCK,
                        apiKey = null,
                        model = null
                    )
                }

                val aiDecision = RoastAiDecisionEngine.decide(
                    context = contextPayload,
                    aiResponse = aiResponse,
                    profile = profile,
                    capability = capability,
                    machineState = machineState,
                    energy = energy,
                    stability = null
                )

                latestAiResponseText = aiResponse.summary()
                latestAiDecisionText = aiDecision.detail()
            } catch (e: Exception) {
                latestAiResponseText = "AI request failed: ${e.message ?: "Unknown error"}"
                latestAiDecisionText = "No AI decision due to request failure."
            }

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

    private fun textInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(defaultText)
        return input
    }
}
