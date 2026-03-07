package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {

    private lateinit var densityInput: EditText
    private lateinit var moistureInput: EditText
    private lateinit var awInput: EditText
    private lateinit var envTempInput: EditText
    private lateinit var humidityInput: EditText

    private lateinit var processSpinner: Spinner
    private lateinit var roastLevelSpinner: Spinner
    private lateinit var flavorSpinner: Spinner
    private lateinit var batchSpinner: Spinner

    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30,30,30,30)

        val title = TextView(this)
        title.text = "Roast OS"
        title.textSize = 26f
        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "HB M2SE · 200g · Roaster Edition"
        subtitle.textSize = 14f
        root.addView(subtitle)

        val beanTitle = TextView(this)
        beanTitle.text = "\nBean Input"
        beanTitle.textSize = 20f
        root.addView(beanTitle)

        processSpinner = Spinner(this)
        processSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("水洗","日晒","蜜处理","厌氧")
        )
        root.addView(label("处理法"))
        root.addView(processSpinner)

        densityInput = numberInput("密度 例如 818")
        root.addView(label("密度"))
        root.addView(densityInput)

        moistureInput = decimalInput("例如 11.1")
        root.addView(label("含水率"))
        root.addView(moistureInput)

        awInput = decimalInput("例如 0.55")
        root.addView(label("aw"))
        root.addView(awInput)

        val envTitle = TextView(this)
        envTitle.text = "\nEnvironment"
        envTitle.textSize = 20f
        root.addView(envTitle)

        envTempInput = decimalInput("环境温度 例如 22")
        root.addView(label("环境温度"))
        root.addView(envTempInput)

        humidityInput = decimalInput("湿度 例如 40")
        root.addView(label("湿度"))
        root.addView(humidityInput)

        val roastTitle = TextView(this)
        roastTitle.text = "\nRoast Target"
        roastTitle.textSize = 20f
        root.addView(roastTitle)

        roastLevelSpinner = Spinner(this)
        roastLevelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("浅烘","浅中","中烘","中深")
        )
        root.addView(label("烘焙度"))
        root.addView(roastLevelSpinner)

        flavorSpinner = Spinner(this)
        flavorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("干净清晰","平衡甜感","高风味强度","厚重Body")
        )
        root.addView(label("风味目标"))
        root.addView(flavorSpinner)

        batchSpinner = Spinner(this)
        batchSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("单锅","连续批")
        )
        root.addView(label("批次模式"))
        root.addView(batchSpinner)

        val button = Button(this)
        button.text = "\n生成 ROAST OS 策略"
        root.addView(button)

        resultView = TextView(this)
        resultView.text = "\n等待生成策略"
        resultView.textSize = 16f
        root.addView(resultView)

        button.setOnClickListener { generateStrategy() }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun label(text:String):TextView{
        val t = TextView(this)
        t.text = text
        return t
    }

    private fun numberInput(hint:String):EditText{
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        return e
    }

    private fun decimalInput(hint:String):EditText{
        val e = EditText(this)
        e.hint = hint
        e.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        return e
    }

    private fun generateStrategy(){

        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55

        val rebound =
            if(density > 830) "回温预计 1:30-1:45"
            else "回温预计 1:15-1:30"

        val yellow =
            if(moisture > 11.3) "转黄建议 4:25-4:50"
            else "转黄建议 4:05-4:35"

        val crack =
            if(aw > 0.56) "一爆预计 8:40-9:10"
            else "一爆预计 8:20-8:50"

        resultView.text =
            """
核心预测
• $rebound
• $yellow
• $crack

过程控制
• 初始火力：中高
• 脱水阶段：稳推
• 梅纳阶段：维持连续热流
• 爆前：提前小收火稳ROR

风险提示
• 前段吸热不足风险
• 爆前冲高风险
• 中后段拖闷风险
            """.trimIndent()
    }
}
