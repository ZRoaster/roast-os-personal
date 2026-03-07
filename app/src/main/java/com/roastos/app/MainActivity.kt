package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var densityInput: EditText
    private lateinit var moistureInput: EditText
    private lateinit var awInput: EditText
    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 30, 30, 30)

        val title = TextView(this)
        title.text = "Roast OS"
        title.textSize = 28f
        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "HB M2SE · 200g"
        subtitle.textSize = 14f
        root.addView(subtitle)

        val section1 = TextView(this)
        section1.text = "\nBean Input"
        section1.textSize = 18f
        root.addView(section1)

        val processLabel = TextView(this)
        processLabel.text = "处理法"
        root.addView(processLabel)

        val processSpinner = Spinner(this)
        val processAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("水洗", "日晒", "蜜处理", "厌氧")
        )
        processSpinner.adapter = processAdapter
        root.addView(processSpinner)

        val densityLabel = TextView(this)
        densityLabel.text = "密度"
        root.addView(densityLabel)

        densityInput = EditText(this)
        densityInput.hint = "例如 818"
        densityInput.inputType = InputType.TYPE_CLASS_NUMBER
        root.addView(densityInput)

        val moistureLabel = TextView(this)
        moistureLabel.text = "含水率"
        root.addView(moistureLabel)

        moistureInput = EditText(this)
        moistureInput.hint = "例如 11.1"
        moistureInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        root.addView(moistureInput)

        val awLabel = TextView(this)
        awLabel.text = "aw"
        root.addView(awLabel)

        awInput = EditText(this)
        awInput.hint = "例如 0.55"
        awInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        root.addView(awInput)

        val button = Button(this)
        button.text = "生成 Roast OS 策略"
        root.addView(button)

        resultView = TextView(this)
        resultView.text = "\n等待生成策略"
        resultView.textSize = 16f
        root.addView(resultView)

        button.setOnClickListener {
            val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
            val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
            val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55

            val rebound = if (density >= 830) "回温偏慢，预计 1:30–1:45" else "回温正常，预计 1:15–1:30"
            val yellow = if (moisture >= 11.3) "转黄建议 4:20–4:50" else "转黄建议 4:05–4:35"
            val crack = if (aw >= 0.56) "一爆预计 8:40–9:10" else "一爆预计 8:20–8:50"

            resultView.text =
                "\n核心预测\n" +
                "• $rebound\n" +
                "• $yellow\n" +
                "• $crack\n\n" +
                "过程控制\n" +
                "• 初始火力中高\n" +
                "• 脱水阶段稳推\n" +
                "• 爆前小收火，稳 ROR\n"
        }

        scroll.addView(root)
        setContentView(scroll)
    }
}
