package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.BatchActualInput
import com.roastos.app.CorrectionEngine
import com.roastos.app.PlannerInput
import com.roastos.app.RoastEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        val processInput = EditText(context)
        processInput.hint = "Process"
        processInput.setText("washed")

        val densityInput = EditText(context)
        densityInput.hint = "Density"
        densityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        densityInput.setText("840")

        val moistureInput = EditText(context)
        moistureInput.hint = "Moisture"
        moistureInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        moistureInput.setText("10.5")

        val awInput = EditText(context)
        awInput.hint = "aw"
        awInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        awInput.setText("0.55")

        val envTempInput = EditText(context)
        envTempInput.hint = "Environment Temp"
        envTempInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        envTempInput.setText("22")

        val humidityInput = EditText(context)
        humidityInput.hint = "Humidity
