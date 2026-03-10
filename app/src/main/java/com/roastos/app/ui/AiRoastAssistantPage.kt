package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.*
import kotlinx.coroutines.runBlocking

object AiRoastAssistantPage {

    private var simulatorElapsedSec = 0

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context,"AI ROAST ASSISTANT"))
        root.addView(UiKit.pageSubtitle(context,"Realtime AI roast analysis and coaching"))

        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context,"TELEMETRY"))

        val telemetryBody = UiKit.bodyText(context,"")

        val manualBtn = Button(context)
        manualBtn.text = "Manual"

        val simBtn = Button(context)
        simBtn.text = "Simulator"

        val machineBtn = Button(context)
        machineBtn.text = "Machine"

        telemetryCard.addView(manualBtn)
        telemetryCard.addView(simBtn)
        telemetryCard.addView(machineBtn)
        telemetryCard.addView(telemetryBody)

        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context,"INPUT"))

        val btInput = decimalInput(context,"BT","165")
        val rorInput = decimalInput(context,"RoR °C/s","0.12")
        val powerInput = intInput(context,"Power","700")
        val airflowInput = intInput(context,"Airflow","10")
        val drumInput = intInput(context,"Drum","60")
        val elapsedInput = intInput(context,"Elapsed sec","240")

        val askInput = textInput(context,"Ask AI","What should I do now?")

        val pushBtn = Button(context)
        pushBtn.text="Push Frame"

        val sim10Btn = Button(context)
        sim10Btn.text="+10s"

        val sim30Btn = Button(context)
        sim30Btn.text="+30s"

        val askBtn = Button(context)
        askBtn.text="Ask AI"

        inputCard.addView(btInput)
        inputCard.addView(rorInput)
        inputCard.addView(powerInput)
        inputCard.addView(airflowInput)
        inputCard.addView(drumInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(askInput)
        inputCard.addView(pushBtn)
        inputCard.addView(sim10Btn)
        inputCard.addView(sim30Btn)
        inputCard.addView(askBtn)

        root.addView(inputCard)

        root.addView(UiKit.spacer(context))

        val aiCard = UiKit.card(context)
        aiCard.addView(UiKit.cardTitle(context,"AI RESPONSE"))

        val aiBody = UiKit.bodyText(context,"")

        aiCard.addView(aiBody)

        root.addView(aiCard)

        root.addView(UiKit.spacer(context))

        fun currentMachineState(): MachineState {

            return MachineStateEngine.buildState(

                powerW = powerInput.text.toString().toIntOrNull() ?: 700,
                airflowPa = airflowInput.text.toString().toIntOrNull() ?: 10,
                drumRpm = drumInput.text.toString().toIntOrNull() ?: 60,
                beanTemp = btInput.text.toString().toDoubleOrNull() ?: 0.0,
                ror = (rorInput.text.toString().toDoubleOrNull() ?: 0.0) * 60.0,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                environmentTemp = 25.0,
                environmentHumidity = 50.0

            )
        }

        fun refresh() {

            telemetryBody.text = MachineTelemetryEngine.summary()

        }

        manualBtn.setOnClickListener {

            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refresh()

        }

        simBtn.setOnClickListener {

            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()

        }

        machineBtn.setOnClickListener {

            MachineTelemetryEngine.connectMachine()
            refresh()

        }

        pushBtn.setOnClickListener {

            MachineTelemetryEngine.pushManualFrame(

                bt = btInput.text.toString().toDoubleOrNull() ?: 0.0,
                et = null,
                ror = (rorInput.text.toString().toDoubleOrNull() ?: 0.0) * 60,
                powerW = powerInput.text.toString().toIntOrNull() ?: 700,
                airflowPa = airflowInput.text.toString().toIntOrNull() ?: 10,
                drumRpm = drumInput.text.toString().toIntOrNull() ?: 60,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                machineStateLabel = "Roasting",
                environmentTemp = 25.0,
                environmentHumidity = 50.0

            )

            refresh()

        }

        sim10Btn.setOnClickListener {

            simulatorElapsedSec += 10

            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)

            refresh()

        }

        sim30Btn.setOnClickListener {

            simulatorElapsedSec += 30

            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsedSec)

            refresh()

        }

        askBtn.setOnClickListener {

            val profile = MachineProfiles.HB_M2SE

            val capability = MachineControlCapabilities.defaultFor(profile)

            val machineState = currentMachineState()

            val energy = EnergyEngine.evaluate(profile,machineState)

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
                styleGoal = null,
                userPrompt = askInput.text.toString(),
                operatorNote = "",
                attachments = emptyList()

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

                aiBody.text = aiResponse.summary()

            }

            catch (e: Exception) {

                aiBody.text = "AI ERROR\n${e.message}"

            }

        }

        refresh()

        scroll.addView(root)

        container.addView(scroll)

    }

    private fun decimalInput(context: Context,hint:String,value:String):EditText{

        val input = EditText(context)

        input.hint = hint

        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        input.setText(value)

        return input
    }

    private fun intInput(context: Context,hint:String,value:String):EditText{

        val input = EditText(context)

        input.hint = hint

        input.inputType = InputType.TYPE_CLASS_NUMBER

        input.setText(value)

        return input
    }

    private fun textInput(context: Context,hint:String,value:String):EditText{

        val input = EditText(context)

        input.hint = hint

        input.setText(value)

        return input
    }

}
