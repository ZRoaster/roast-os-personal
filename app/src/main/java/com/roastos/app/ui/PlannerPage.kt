package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object PlannerPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "ROAST PLANNER"
        title.textSize = 22f

        val densityInput = EditText(context)
        densityInput.hint = "Density (g/L)"

        val moistureInput = EditText(context)
        moistureInput.hint = "Moisture %"

        val awInput = EditText(context)
        awInput.hint = "Water Activity (aw)"

        val envTempInput = EditText(context)
        envTempInput.hint = "Environment Temp °C"

        val humidityInput = EditText(context)
        humidityInput.hint = "Humidity %"

        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Plan"

        val resultView = TextView(context)

        root.addView(title)
        root.addView(densityInput)
        root.addView(moistureInput)
        root.addView(awInput)
        root.addView(envTempInput)
        root.addView(humidityInput)
        root.addView(calculateBtn)
        root.addView(resultView)

        container.addView(root)

        calculateBtn.setOnClickListener {

            val density = densityInput.text.toString()
            val moisture = moistureInput.text.toString()
            val aw = awInput.text.toString()
            val envTemp = envTempInput.text.toString()
            val humidity = humidityInput.text.toString()

            resultView.text =
                """
Planner Input

Density: $density
Moisture: $moisture
aw: $aw
EnvTemp: $envTemp
Humidity: $humidity

(Engine integration next step)
""".trimIndent()
        }
    }
}
