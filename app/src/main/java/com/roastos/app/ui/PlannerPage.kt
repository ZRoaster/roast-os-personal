package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.PlannerInput
import com.roastos.app.RoastEngine

object PlannerPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "ROAST PLANNER"
        title.textSize = 22f

        val processInput = EditText(context)
        processInput.hint = "Process: washed / honey_washed / natural / anaerobic"
        processInput.setText("washed")

        val densityInput = EditText(context)
        densityInput.hint = "Density (g/L)"
        densityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        densityInput.setText("840")

        val moistureInput = EditText(context)
        moistureInput.hint = "Moisture %"
        moistureInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        moistureInput.setText("10.5")

        val awInput = EditText(context)
        awInput.hint = "Water Activity (aw)"
        awInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        awInput.setText("0.55")

        val envTempInput = EditText(context)
        envTempInput.hint = "Environment Temp °C"
        envTempInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        envTempInput.setText("22")

        val humidityInput = EditText(context)
        humidityInput.hint = "Humidity %"
        humidityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        humidityInput.setText("40")

        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Roast Card"

        val resultView = TextView(context)

        root.addView(title)
        root.addView(processInput)
        root.addView(densityInput)
        root.addView(moistureInput)
        root.addView(awInput)
        root.addView(envTempInput)
        root.addView(humidityInput)
        root.addView(calculateBtn)
        root.addView(resultView)

        container.addView(root)

        calculateBtn.setOnClickListener {

            val input = PlannerInput(
                process = processInput.text.toString().ifBlank { "washed" },
                density = densityInput.text.toString().toDoubleOrNull() ?: 840.0,
                moisture = moistureInput.text.toString().toDoubleOrNull() ?: 10.5,
                aw = awInput.text.toString().toDoubleOrNull() ?: 0.55,
                envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0,
                envRH = humidityInput.text.toString().toDoubleOrNull() ?: 40.0,

                roastLevel = "light_medium",
                orientation = "clean",
                purpose = "pourover",
                batchNum = 1,
                beanSize = "normal",
                freshness = "fresh",
                mode = "M2",

                learnM = 5.5,
                learnK = 26.0,
                learnW = 0.65,

                ttSec = 80,
                tySec = 250
            )

            val plan = RoastEngine.calcCard(input)

            AppState.lastPlannerInput = input
            AppState.lastPlannerResult = plan

            val turningSec = (plan.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val yellowSec = plan.h2Sec.toInt()
            val fcSec = plan.fcPredSec.toInt()
            val dropSec = plan.dropSec.toInt()

            val heatDemand = when {
                plan.chargeBT >= 206 -> "Very High"
                plan.chargeBT >= 205 -> "High"
                plan.chargeBT >= 203 -> "Medium"
                else -> "Low"
            }

            val riskFocus = when (input.process) {
                "anaerobic" -> "Front-end pressure / overshoot risk"
                "natural" -> "Drying momentum control risk"
                "honey_washed" -> "Middle-stage thickness risk"
                else -> "Relatively clean process"
            }

            resultView.text = """
ROAST OS EXECUTION CARD

Bean
Process ${plan.ptLabel}
Density ${"%.1f".format(input.density)}
Moisture ${"%.1f".format(input.moisture)}
aw ${"%.2f".format(input.aw)}

Environment
Temp ${"%.1f".format(input.envTemp)}℃
RH ${"%.1f".format(input.envRH)}%

Core Setup
Charge ${plan.chargeBT}℃
RPM ${plan.rpm}
Mode ${input.mode}

Heat Demand
$heatDemand

Timeline
Turning ${RoastEngine.toMMSS(turningSec.toDouble())}
Yellow ${RoastEngine.toMMSS(yellowSec.toDouble())}
FC ${RoastEngine.toMMSS(fcSec.toDouble())}
Drop ${RoastEngine.toMMSS(dropSec.toDouble())}

Development
Dev ${plan.devTime}s
DTR ${"%.1f".format(plan.dtrPercent)}%

Heat Plan
H1 ${plan.h1W}W @ ${RoastEngine.toMMSS(plan.h1Sec)}
H2 ${plan.h2W}W @ ${RoastEngine.toMMSS(plan.h2Sec)}
H3 ${plan.h3W}W @ ${RoastEngine.toMMSS(plan.h3Sec)}
H4 ${plan.h4W}W @ ${RoastEngine.toMMSS(plan.h4Sec)}
H5 ${plan.h5W}W @ ${RoastEngine.toMMSS(plan.h5Sec)}

Air Plan
Preheat ${plan.preheatPa}Pa
Wind1 ${plan.wind1Pa}Pa @ ${RoastEngine.toMMSS(plan.wind1Sec)}
Wind2 ${plan.wind2Pa}Pa @ ${RoastEngine.toMMSS(plan.wind2Sec)}
Dev ${plan.devPa}Pa
Protect @ ${RoastEngine.toMMSS(plan.protectSec)}

ROR Targets
${plan.rorTargets.joinToString(" / ") { "%.1f".format(it) }}

Risk Focus
$riskFocus

State
Planner saved for Live / Correction
            """.trimIndent()
        }
    }
}
