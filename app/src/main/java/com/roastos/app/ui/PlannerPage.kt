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

        val roastLevelInput = EditText(context)
        roastLevelInput.hint = "Roast Level: light / light_medium / medium / medium_dark"
        roastLevelInput.setText("light_medium")

        val orientationInput = EditText(context)
        orientationInput.hint = "Orientation: clean / stable / thick"
        orientationInput.setText("clean")

        val batchInput = EditText(context)
        batchInput.hint = "Batch Number"
        batchInput.inputType = InputType.TYPE_CLASS_NUMBER
        batchInput.setText("1")

        val ttInput = EditText(context)
        ttInput.hint = "Turning Time sec"
        ttInput.inputType = InputType.TYPE_CLASS_NUMBER
        ttInput.setText("80")

        val tyInput = EditText(context)
        tyInput.hint = "Yellow Time sec"
        tyInput.inputType = InputType.TYPE_CLASS_NUMBER
        tyInput.setText("250")

        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Plan"

        val resultView = TextView(context)

        root.addView(title)
        root.addView(processInput)
        root.addView(densityInput)
        root.addView(moistureInput)
        root.addView(awInput)
        root.addView(envTempInput)
        root.addView(humidityInput)
        root.addView(roastLevelInput)
        root.addView(orientationInput)
        root.addView(batchInput)
        root.addView(ttInput)
        root.addView(tyInput)
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

            resultView.text = """
Process
${plan.ptLabel}

Charge
${plan.chargeBT}℃

Predicted FC
${RoastEngine.toMMSS(plan.fcPredSec)}

Drop
${RoastEngine.toMMSS(plan.dropSec)}

Development
${plan.devTime}s
DTR ${"%.1f".format(plan.dtrPercent)}%

Heat Plan
H1 ${plan.h1W}W @ ${RoastEngine.toMMSS(plan.h1Sec)}
H2 ${plan.h2W}W @ ${RoastEngine.toMMSS(plan.h2Sec)}
H3 ${plan.h3W}W @ ${RoastEngine.toMMSS(plan.h3Sec)}
H4 ${plan.h4W}W @ ${RoastEngine.toMMSS(plan.h4Sec)}
H5 ${plan.h5W}W @ ${RoastEngine.toMMSS(plan.h5Sec)}

Air Plan
Wind1 ${plan.wind1Pa}Pa @ ${RoastEngine.toMMSS(plan.wind1Sec)}
Wind2 ${plan.wind2Pa}Pa @ ${RoastEngine.toMMSS(plan.wind2Sec)}
Dev ${plan.devPa}Pa
Protect @ ${RoastEngine.toMMSS(plan.protectSec)}

ROR Targets
${plan.rorTargets.joinToString(" / ") { "%.1f".format(it) }}

ROR Full
${plan.rorFull.joinToString(" / ") { "%.1f".format(it) }}

Flags
awTol ${"%.1f".format(plan.awTol)}
M3 Protected ${if (plan.m3Protected) "YES" else "NO"}
LowDens Assist ${if (plan.m3LowDens) "YES" else "NO"}

State
Planner result saved for Live / Correction
            """.trimIndent()
        }
    }
}
