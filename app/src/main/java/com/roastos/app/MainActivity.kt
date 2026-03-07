package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {

    lateinit var densityInput: EditText
    lateinit var moistureInput: EditText
    lateinit var awInput: EditText
    lateinit var envTempInput: EditText
    lateinit var humidityInput: EditText

    lateinit var processSpinner: Spinner
    lateinit var roastLevelSpinner: Spinner
    lateinit var flavorSpinner: Spinner
    lateinit var batchSpinner: Spinner

    lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 30, 30, 30)

        val title = TextView(this)
        title.text = "Roast OS"
        title.textSize = 26f
        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "HB M2SE · 200g · Roaster Edition"
        root.addView(subtitle)

        val beanTitle = TextView(this)
        beanTitle.text = "\nBean Input"
        beanTitle.textSize = 20f
        root.addView(beanTitle)

        root.addView(label("处理法"))
        processSpinner = spinner(listOf("水洗", "日晒", "蜜处理", "厌氧"))
        root.addView(processSpinner)

        root.addView(label("密度"))
        densityInput = numberInput("例如 818")
        root.addView(densityInput)

        root.addView(label("含水率"))
        moistureInput = decimalInput("例如 11.1")
        root.addView(moistureInput)

        root.addView(label("aw"))
        awInput = decimalInput("例如 0.55")
        root.addView(awInput)

        val envTitle = TextView(this)
        envTitle.text = "\nEnvironment"
        envTitle.textSize = 20f
        root.addView(envTitle)

        root.addView(label("环境温度"))
        envTempInput = decimalInput("例如 22")
        root.addView(envTempInput)

        root.addView(label("湿度"))
        humidityInput = decimalInput("例如 40")
        root.addView(humidityInput)

        val roastTitle = TextView(this)
        roastTitle.text = "\nRoast Target"
        roastTitle.textSize = 20f
        root.addView(roastTitle)

        root.addView(label("烘焙度"))
        roastLevelSpinner = spinner(listOf("浅烘", "浅中", "中烘", "中深"))
        root.addView(roastLevelSpinner)

        root.addView(label("风味目标"))
        flavorSpinner = spinner(listOf("干净清晰", "平衡甜感", "高风味强度", "厚重Body"))
        root.addView(flavorSpinner)

        root.addView(label("批次模式"))
        batchSpinner = spinner(listOf("单锅", "连续批"))
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

    fun label(text: String): TextView {
        val t = TextView(this)
        t.text = text
        return t
    }

    fun spinner(list: List<String>): Spinner {
        val s = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        s.adapter = adapter
        return s
    }

    fun numberInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        return e
    }

    fun decimalInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        return e
    }

    fun generateStrategy() {

        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavor = flavorSpinner.selectedItem.toString()
        val batch = batchSpinner.selectedItem.toString()

        // 热惯性指数
        val inertia =
            (density - 800.0) / 60.0 +
            (moisture - 11.0) * 0.9 +
            (aw - 0.55) * 12.0

        // 热需求指数
        var heatDemand =
            (density - 800.0) / 40.0 +
            (moisture - 11.0) * 1.2 +
            (0.60 - aw) * 8.0 +
            (22.0 - envTemp) * 0.4 +
            (45.0 - humidity) * 0.06

        if (process == "日晒") heatDemand += 0.30
        if (process == "蜜处理") heatDemand += 0.15
        if (process == "厌氧") heatDemand += 0.40
        if (batch == "连续批") heatDemand -= 0.35

        val demandLevel = when {
            heatDemand < -0.2 -> "低"
            heatDemand < 0.8 -> "中"
            else -> "高"
        }

        val turning = when (demandLevel) {
            "低" -> "1:30-1:40"
            "中" -> "1:20-1:35"
            else -> "1:10-1:25"
        }

        val yellow = when (demandLevel) {
            "低" -> "4:20-4:40"
            "中" -> "4:05-4:35"
            else -> "3:50-4:20"
        }

        val firstCrack = when (demandLevel) {
            "低" -> "8:40-9:10"
            "中" -> "8:20-8:50"
            else -> "8:00-8:30"
        }

        val drop = when (roastLevel) {
            "浅烘" -> when (demandLevel) {
                "低" -> "9:20-9:50"
                "中" -> "9:10-9:40"
                else -> "8:50-9:20"
            }
            "浅中" -> when (demandLevel) {
                "低" -> "9:40-10:10"
                "中" -> "9:25-9:55"
                else -> "9:05-9:35"
            }
            "中烘" -> when (demandLevel) {
                "低" -> "9:55-10:25"
                "中" -> "9:40-10:10"
                else -> "9:20-9:50"
            }
            else -> when (demandLevel) {
                "低" -> "10:15-10:45"
                "中" -> "10:00-10:30"
                else -> "9:40-10:10"
            }
        }

        val rorText = when {
            inertia < -0.2 ->
                """
• 回温 20-22
• 转黄 15-17
• 梅纳 12-14
• 爆前 10-11
• 发展 6-7
                """.trimIndent()

            inertia < 0.5 ->
                """
• 回温 18-20
• 转黄 13-15
• 梅纳 10-12
• 爆前 8-9
• 发展 5-6
                """.trimIndent()

            else ->
                """
• 回温 16-18
• 转黄 11-13
• 梅纳 9-11
• 爆前 7-8
• 发展 4-5
                """.trimIndent()
        }

        val charge = when (demandLevel) {
            "低" -> "200-202"
            "中" -> "202-204"
            else -> "204-206"
        }

        val firePlan = when (demandLevel) {
            "低" ->
                """
• Charge后 900W
• 回温后 850W
• 转黄阶段 750W
• 爆前阶段 650W
                """.trimIndent()

            "中" ->
                """
• Charge后 1000W
• 回温后 900W
• 转黄阶段 800W
• 爆前阶段 700W
                """.trimIndent()

            else ->
                """
• Charge后 1100W
• 回温后 1000W
• 转黄阶段 850W
• 爆前阶段 750W
                """.trimIndent()
        }

        val airPlan = when (flavor) {
            "干净清晰" ->
                """
• 脱水 8-12 Pa
• 梅纳 12-16 Pa
• 爆前 16-20 Pa
                """.trimIndent()

            "厚重Body" ->
                """
• 脱水 5-8 Pa
• 梅纳 8-12 Pa
• 爆前 12-16 Pa
                """.trimIndent()

            else ->
                """
• 脱水 5-10 Pa
• 梅纳 10-15 Pa
• 爆前 15-20 Pa
                """.trimIndent()
        }

        val processControl = buildString {
            append("• 初始火力按热需求等级执行\n")
            append("• 脱水阶段稳推，不追表面快\n")

            when (flavor) {
                "干净清晰" -> append("• 梅纳阶段保持连续热流，不要过厚\n")
                "平衡甜感" -> append("• 梅纳阶段维持中等厚度，兼顾甜感与清晰\n")
                "高风味强度" -> append("• 梅纳阶段可略积极，但不要粗暴推热\n")
                "厚重Body" -> append("• 梅纳阶段允许略厚，但严防拖闷\n")
            }

            when (roastLevel) {
                "浅烘", "浅中" -> append("• 爆前提前小收火，稳 ROR，不临时猛砍")
                else -> append("• 爆前适度收火，避免尾段发木")
            }
        }

        val riskText = buildString {
            when {
                heatDemand > 0.8 -> append("• 前段吸热不足风险\n")
                heatDemand < -0.2 -> append("• 回温过快风险\n")
                else -> append("• 系统惯性基本均衡\n")
            }

            when (process) {
                "日晒", "厌氧" -> append("• 表层发展快于内部风险\n")
                else -> append("• 爆前冲高风险\n")
            }

            when (flavor) {
                "厚重Body" -> append("• 中后段拖闷风险\n")
                "高风味强度" -> append("• 风味强但容易粗糙的风险\n")
                else -> append("• 人工修正过度风险\n")
            }
        }.trimIndent()

        val executionCard = """
• 预热 ${charge}℃
• 回温 $turning
• 转黄 $yellow
• 一爆 $firstCrack
• 下豆 $drop
        """.trimIndent()

        val result = """
热惯性指数
• ${String.format("%.2f", inertia)}

热需求等级
• $demandLevel

核心预测
• 回温 $turning
• 转黄 $yellow
• 一爆 $firstCrack
• 下豆 $drop

ROR轨迹
$rorText

HB M2SE火力建议
• Charge ${charge}℃
$firePlan

风压建议
$airPlan

过程控制
$processControl

风险提示
$riskText

Roast OS执行卡
$executionCard
        """.trimIndent()

        resultView.text = result
    }
}
