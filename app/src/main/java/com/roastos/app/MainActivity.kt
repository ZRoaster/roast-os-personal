package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    lateinit var densityInput: EditText
    lateinit var moistureInput: EditText
    lateinit var awInput: EditText
    lateinit var envTempInput: EditText
    lateinit var humidityInput: EditText

    lateinit var processSpinner: Spinner
    lateinit var roastLevelSpinner: Spinner
    lateinit var flavorGoalSpinner: Spinner
    lateinit var batchModeSpinner: Spinner

    lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40,40,40,40)

        scroll.addView(root)

        fun sectionTitle(text:String):TextView{
            val t = TextView(this)
            t.text = text
            t.textSize = 20f
            t.setPadding(0,30,0,10)
            return t
        }

        fun input(label:String):EditText{
            val e = EditText(this)
            e.hint = label
            e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            return e
        }

        fun spinner(items:Array<String>):Spinner{
            val s = Spinner(this)
            val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            s.adapter = adapter
            return s
        }

        //标题
        val title = TextView(this)
        title.text = "Roast OS"
        title.textSize = 26f

        val sub = TextView(this)
        sub.text = "HB M2SE · 200g · Roaster Edition"

        root.addView(title)
        root.addView(sub)

        // Bean Input
        root.addView(sectionTitle("Bean Input"))

        processSpinner = spinner(arrayOf("水洗","日晒","蜜处理"))
        root.addView(processSpinner)

        densityInput = input("密度 例如 818")
        root.addView(densityInput)

        moistureInput = input("含水率 例如 11.1")
        root.addView(moistureInput)

        awInput = input("aw 例如 0.55")
        root.addView(awInput)

        // Environment
        root.addView(sectionTitle("Environment"))

        envTempInput = input("环境温度 例如 22")
        root.addView(envTempInput)

        humidityInput = input("湿度 例如 40")
        root.addView(humidityInput)

        // Roast Target
        root.addView(sectionTitle("Roast Target"))

        roastLevelSpinner = spinner(arrayOf("浅焙","中浅","中焙"))
        root.addView(roastLevelSpinner)

        flavorGoalSpinner = spinner(arrayOf("干净清晰","甜感","厚重"))
        root.addView(flavorGoalSpinner)

        batchModeSpinner = spinner(arrayOf("单锅","连续批"))
        root.addView(batchModeSpinner)

        // Generate Button
        val btn = Button(this)
        btn.text = "生成 ROAST OS 策略"
        btn.setOnClickListener {
            generateStrategy()
        }

        root.addView(btn)

        // Result
        resultView = TextView(this)
        resultView.setPadding(0,40,0,40)
        resultView.textSize = 16f

        root.addView(resultView)

        setContentView(scroll)
    }

    fun generateStrategy(){

        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55

        val env = envTempInput.text.toString().toDoubleOrNull() ?: 22.0

        val thermal = (density-800)/50 + (moisture-11)*0.8 + (aw-0.55)*10

        val turning = "1:20-1:35"
        val yellow = "4:05-4:35"
        val firstCrack = "8:20-8:50"
        val drop = "9:10-9:40"

        val result = """
热惯性指数
$thermal

核心预测
• 回温 $turning
• 转黄 $yellow
• 一爆 $firstCrack
• 下豆 $drop

ROR轨迹
• 回温 18-20
• 转黄 13-15
• 梅纳 10-12
• 爆前 8-9
• 发展 5-6

HB M2SE火力建议
• Charge 1000W
• 回温后 900W
• 转黄阶段 800W
• 爆前阶段 700W

风压建议
• 脱水 5-10Pa
• 梅纳 10-15Pa
• 爆前 15-20Pa

Roast OS执行卡
Charge 204℃
Turning $turning
Yellow $yellow
First Crack $firstCrack
Drop $drop
""".trimIndent()

        resultView.text = result
    }
}
