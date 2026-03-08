package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.AppState
import com.roastos.app.BatchSessionEngine
import com.roastos.app.PlannerInput
import com.roastos.app.RoastEngine
import com.roastos.app.RoastStateModel
import com.roastos.app.RoastTimelineStore

object PlannerPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST PLANNER"))
        root.addView(UiKit.pageSubtitle(context, "Build baseline timeline, heat plan, and batch session"))
        root.addView(UiKit.spacer(context))

        val processInput = makeTextInput(
            context = context,
            hint = "Process: washed / honey_washed / natural / anaerobic",
            value = "washed"
        )

        val densityInput = makeNumberInput(
            context = context,
            hint = "Density (g/L)",
            value = "840"
        )

        val moistureInput = makeDecimalInput(
            context = context,
            hint = "Moisture %",
            value = "10.5"
        )

        val awInput = makeDecimalInput(
            context = context,
            hint = "Water Activity (aw)",
            value = "0.55"
        )

        root.addView(
            buildInputCard(
                context,
                "BEAN",
                listOf(processInput, densityInput, moistureInput, awInput)
            )
        )
        root.addView(UiKit.spacer(context))

        val envTempInput = makeDecimalInput(
            context = context,
            hint = "Environment Temp °C",
            value = "22"
        )

        val humidityInput = makeDecimalInput(
            context = context,
            hint = "Humidity %",
            value = "40"
        )

        root.addView(
            buildInputCard(
                context,
                "ENVIRONMENT",
                listOf(envTempInput, humidityInput)
            )
        )
        root.addView(UiKit.spacer(context))

        val roastLevelInput = makeTextInput(
            context = context,
            hint = "Roast Level",
            value = "light_medium"
        )

        val orientationInput = makeTextInput(
            context = context,
            hint = "Orientation",
            value = "clean"
        )

        val batchInput = makeNumberInput(
            context = context,
            hint = "Batch Number",
            value = "1"
        )

        root.addView(
            buildInputCard(
                context,
                "ROAST INTENT",
                listOf(roastLevelInput, orientationInput, batchInput)
            )
        )
        root.addView(UiKit.spacer(context))

        val advancedToggle = Button(context)
        advancedToggle.text = "Show Advanced Parameters"

        val advancedBlock = LinearLayout(context)
        advancedBlock.orientation = LinearLayout.VERTICAL
        advancedBlock.visibility = View.GONE
        advancedBlock.setPadding(0, 16, 0, 0)

        val ttInput = makeNumberInput(
            context = context,
            hint = "Turning Time sec",
            value = "80"
        )

        val tyInput = makeNumberInput(
            context = context,
            hint = "Yellow Time sec",
            value = "250"
        )

        advancedBlock.addView(ttInput)
        advancedBlock.addView(tyInput)

        val advancedCard = UiKit.card(context)
        advancedCard.addView(UiKit.cardTitle(context, "ADVANCED"))
        advancedCard.addView(advancedToggle)
        advancedCard.addView(advancedBlock)

        root.addView(advancedCard)
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Roast Card"
        actionCard.addView(UiKit.cardTitle(context, "ACTIONS"))
        actionCard.addView(calculateBtn)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val resultCard = UiKit.card(context)
        val resultBody = UiKit.bodyText(context, "No roast card generated yet.")
        resultCard.addView(UiKit.cardTitle(context, "RESULT"))
        resultCard.addView(resultBody)

        root.addView(resultCard)

        scroll.addView(root)
        container.addView(scroll)

        advancedToggle.setOnClickListener {
            if (advancedBlock.visibility == View.GONE) {
                advancedBlock.visibility = View.VISIBLE
                advancedToggle.text = "Hide Advanced Parameters"
            } else {
                advancedBlock.visibility = View.GONE
                advancedToggle.text = "Show Advanced Parameters"
            }
        }

        calculateBtn.setOnClickListener {
            val input = PlannerInput(
                process = processInput.text.toString().ifBlank { "washed" },
                density = densityInput.text.toString().toDoubleOrNull() ?: 840.0,
                moisture = moistureInput.text.toString().toDoubleOrNull() ?: 10.5,
                aw = awInput.text.toString().toDoubleOrNull() ?: 0.55,
                envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0,
                envRH = humidityInput.text.toString().toDoubleOrNull() ?: 40.0,
                roastLevel = roastLevelInput.text.toString().ifBlank { "light_medium" },
                orientation = orientationInput.text.toString().ifBlank { "clean" },
                purpose = "pourover",
                batchNum = batchInput.text.toString().toIntOrNull() ?: 1,
                beanSize = "normal",
                freshness = "fresh",
                mode = "M2",
                learnM = 5.5,
                learnK = 26.0,
                learnW = 0.65,
                ttSec = ttInput.text.toString().toIntOrNull() ?: 80,
                tySec = tyInput.text.toString().toIntOrNull() ?: 250
            )

            val plan = RoastEngine.calcCard(input)

            AppState.lastPlannerInput = input
            AppState.lastPlannerResult = plan

            RoastStateModel.syncPlannerInput(input)

            val turningSec = (plan.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val yellowSec = plan.h2Sec.toInt()
            val fcSec = plan.fcPredSec.toInt()
            val dropSec = plan.dropSec.toInt()

            RoastTimelineStore.reset()
            RoastTimelineStore.syncPredicted(
                turningSec = turningSec,
                yellowSec = yellowSec,
                fcSec = fcSec,
                dropSec = dropSec
            )

            BatchSessionEngine.resetCurrentSession()
            val session = BatchSessionEngine.startFromPlanner()

            val heatDemand = when {
                plan.chargeBT >= 206 -> "Very High"
                plan.chargeBT >= 205 -> "High"
                plan.chargeBT >= 203 -> "Medium"
                else -> "Low"
            }

            val processRisk = when (input.process) {
                "anaerobic" -> "Front-end pressure / overshoot risk"
                "natural" -> "Drying momentum control risk"
                "honey_washed" -> "Middle-stage thickness risk"
                else -> "Relatively clean process"
            }

            val executionFocus = when (input.orientation) {
                "clean" -> "Prioritize clarity and controlled pre-FC momentum"
                "stable" -> "Prioritize balance and replayability"
                "thick" -> "Prioritize body and sweetness, avoid over-exhaust"
                else -> "Balanced execution"
            }

            resultBody.text = """
ROAST OS EXECUTION CARD

Bean
Process    ${plan.ptLabel}
Density    ${"%.1f".format(input.density)}
Moisture   ${"%.1f".format(input.moisture)}
aw         ${"%.2f".format(input.aw)}

Environment
Temp       ${"%.1f".format(input.envTemp)}℃
RH         ${"%.1f".format(input.envRH)}%

Core Setup
Charge     ${plan.chargeBT}℃
RPM        ${plan.rpm}
Mode       ${input.mode}
Batch      ${input.batchNum}

Heat Demand
$heatDemand

Predicted Timeline
Turning    ${RoastEngine.toMMSS(turningSec.toDouble())}
Yellow     ${RoastEngine.toMMSS(yellowSec.toDouble())}
FC         ${RoastEngine.toMMSS(fcSec.toDouble())}
Drop       ${RoastEngine.toMMSS(dropSec.toDouble())}

Development
Dev        ${plan.devTime}s
DTR        ${"%.1f".format(plan.dtrPercent)}%

Heat Plan
H1         ${plan.h1W}W @ ${RoastEngine.toMMSS(plan.h1Sec)}
H2         ${plan.h2W}W @ ${RoastEngine.toMMSS(plan.h2Sec)}
H3         ${plan.h3W}W @ ${RoastEngine.toMMSS(plan.h3Sec)}
H4         ${plan.h4W}W @ ${RoastEngine.toMMSS(plan.h4Sec)}
H5         ${plan.h5W}W @ ${RoastEngine.toMMSS(plan.h5Sec)}

Air Plan
Preheat    ${plan.preheatPa}Pa
Wind1      ${plan.wind1Pa}Pa @ ${RoastEngine.toMMSS(plan.wind1Sec)}
Wind2      ${plan.wind2Pa}Pa @ ${RoastEngine.toMMSS(plan.wind2Sec)}
Dev        ${plan.devPa}Pa
Protect    @ ${RoastEngine.toMMSS(plan.protectSec)}

ROR Targets
${plan.rorTargets.joinToString(" / ") { "%.1f".format(it) }}

Execution Focus
$executionFocus

Risk Focus
$processRisk

Batch Session
${session.batchId}
Status ${session.status}

Timeline Sync
Predicted anchors written to RoastTimelineStore
            """.trimIndent()
        }
    }

    private fun buildInputCard(
        context: Context,
        titleText: String,
        views: List<View>
    ): LinearLayout {
        val card = UiKit.card(context)
        card.addView(UiKit.cardTitle(context, titleText))
        views.forEach { card.addView(it) }
        return card
    }

    private fun makeTextInput(
        context: Context,
        hint: String,
        value: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.setText(value)
        return input
    }

    private fun makeNumberInput(
        context: Context,
        hint: String,
        value: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(value)
        return input
    }

    private fun makeDecimalInput(
        context: Context,
        hint: String,
        value: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(value)
        return input
    }
}
