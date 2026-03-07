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
import kotlin.math.roundToInt

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
        root.setPadding(30, 30, 30, 30)

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
            listOf("水洗", "日晒", "蜜处理", "厌氧")
        )
        root.addView(label("处理法"))
        root.addView(processSpinner)

        densityInput = numberInput("例如 818")
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

        envTempInput = decimalInput("例如 22")
        root.addView(label("环境温度"))
        root.addView(envTempInput)

        humidityInput = decimalInput("例如 40")
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
            listOf("浅烘", "浅中", "中烘", "中深")
        )
        root.addView(label("烘焙度"))
        root.addView(roastLevelSpinner)

        flavorSpinner = Spinner(this)
        flavorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("干净清晰", "平衡甜感", "高风味强度", "厚重Body")
        )
        root.addView(label("风味目标"))
        root.addView(flavorSpinner)

        batchSpinner = Spinner(this)
        batchSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("单锅", "连续批")
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

    private fun label(text: String): TextView {
        val t = TextView(this)
        t.text = text
        return t
    }

    private fun numberInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        return e
    }

    private fun decimalInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        return e
    }

    private fun generateStrategy() {
        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavor = flavorSpinner.selectedItem.toString()
        val batchMode = batchSpinner.selectedItem.toString()

        val thermalScore = calculateThermalScore(
            density = density,
            moisture = moisture,
            aw = aw,
            envTemp = envTemp,
            humidity = humidity,
            process = process,
            batchMode = batchMode
        )

        val rebound = predictRebound(thermalScore)
        val yellow = predictYellow(thermalScore, roastLevel)
        val crack = predictCrack(thermalScore, roastLevel, process)
        val drop = predictDrop(crack, roastLevel)

        val rorTrack = predictRorTrack(thermalScore, roastLevel)
        val control = buildControlAdvice(thermalScore, flavor, roastLevel, process)
        val risks = buildRiskAdvice(thermalScore, process, flavor)
        val card = buildExecutionCard(rebound, yellow, crack, drop, thermalScore)

        resultView.text =
            """
热惯性指数
• ${format1(thermalScore)}

核心预测
• 回温预计：$rebound
• 转黄建议：$yellow
• 一爆预计：$crack
• 下豆区间：$drop

ROR 轨迹
• 回温后 ROR：${rorTrack[0]}
• 转黄阶段 ROR：${rorTrack[1]}
• 梅纳阶段 ROR：${rorTrack[2]}
• 爆前 ROR：${rorTrack[3]}
• 发展期 ROR：${rorTrack[4]}

过程控制
• ${control[0]}
• ${control[1]}
• ${control[2]}
• ${control[3]}
• ${control[4]}

风险提示
• ${risks[0]}
• ${risks[1]}
• ${risks[2]}

执行卡
• $card
            """.trimIndent()
    }

    private fun calculateThermalScore(
        density: Double,
        moisture: Double,
        aw: Double,
        envTemp: Double,
        humidity: Double,
        process: String,
        batchMode: String
    ): Double {
        var score = 0.0

        score += (density - 800.0) / 18.0
        score += (moisture - 11.0) * 2.8
        score += (aw - 0.55) * 55.0
        score += (22.0 - envTemp) / 3.5
        score += (45.0 - humidity) / 18.0

        if (process == "日晒") score += 0.8
        if (process == "厌氧") score += 1.1
        if (process == "蜜处理") score += 0.4
        if (batchMode == "连续批") score -= 0.8

        return score
    }

    private fun predictRebound(score: Double): String {
        return when {
            score >= 3.0 -> "1:32-1:48"
            score >= 1.2 -> "1:22-1:36"
            score >= -0.8 -> "1:12-1:26"
            else -> "1:02-1:18"
        }
    }

    private fun predictYellow(score: Double, roastLevel: String): String {
        val baseStart: Double
        val baseEnd: Double

        when (roastLevel) {
            "浅烘" -> {
                baseStart = 4.05
                baseEnd = 4.30
            }
            "浅中" -> {
                baseStart = 4.15
                baseEnd = 4.40
            }
            "中烘" -> {
                baseStart = 4.25
                baseEnd = 4.50
            }
            else -> {
                baseStart = 4.35
                baseEnd = 5.00
            }
        }

        val offset = score * 0.06
        return "${minuteText(baseStart + offset)}-${minuteText(baseEnd + offset)}"
    }

    private fun predictCrack(score: Double, roastLevel: String, process: String): String {
        var start = 8.30
        var end = 8.55

        if (roastLevel == "中烘") {
            start += 0.15
            end += 0.15
        }
        if (roastLevel == "中深") {
            start += 0.30
            end += 0.30
        }
        if (process == "日晒" || process == "厌氧") {
            start -= 0.05
            end -= 0.05
        }

        val offset = score * 0.08
        return "${minuteText(start + offset)}-${minuteText(end + offset)}"
    }

    private fun predictDrop(crackText: String, roastLevel: String): String {
        val crackStart = crackText.split("-")[0]
        val base = minuteToDouble(crackStart)

        val devAdd = when (roastLevel) {
            "浅烘" -> 0.55
            "浅中" -> 0.75
            "中烘" -> 1.00
            else -> 1.25
        }

        return "${minuteText(base + devAdd)}-${minuteText(base + devAdd + 0.20)}"
    }

    private fun predictRorTrack(score: Double, roastLevel: String): List<String> {
        val offset = when {
            score >= 3.0 -> -1
            score >= 1.0 -> 0
            score >= -1.0 -> 1
            else -> 2
        }

        val devOffset = when (roastLevel) {
            "浅烘" -> 1
            "浅中" -> 0
            "中烘" -> -1
            else -> -2
        }

        val r1 = "${17 + offset}-${19 + offset}"
        val r2 = "${12 + offset}-${14 + offset}"
        val r3 = "${9 + offset}-${11 + offset}"
        val r4 = "${6 + offset + devOffset}-${7 + offset + devOffset}"
        val r5 = "${4 + devOffset}-${5 + devOffset}"

        return listOf(r1, r2, r3, r4, r5)
    }

    private fun buildControlAdvice(
        score: Double,
        flavor: String,
        roastLevel: String,
        process: String
    ): List<String> {
        val fire = when {
            score >= 3.0 -> "初始火力：偏高，优先补足前段吸热"
            score >= 1.0 -> "初始火力：中高，保持稳定推进"
            score >= -1.0 -> "初始火力：常规中值"
            else -> "初始火力：略收，防止前段过猛"
        }

        val drying = when {
            score >= 2.0 -> "脱水阶段：稳推，避免前段失速"
            score <= -1.0 -> "脱水阶段：注意别冲太快，避免表层先干"
            else -> "脱水阶段：按常规节奏推进"
        }

        val maillard = when (flavor) {
            "干净清晰" -> "梅纳阶段：保持连续热流，不要过厚"
            "平衡甜感" -> "梅纳阶段：中等厚度，甜感与清晰并重"
            "高风味强度" -> "梅纳阶段：可略积极，但不要粗暴推高"
            else -> "梅纳阶段：允许略厚，但严防拖闷"
        }

        val preCrack = when (roastLevel) {
            "浅烘", "浅中" -> "爆前：提前小收火，稳 ROR，不临时猛砍"
            else -> "爆前：适度收火，避免尾段发木"
        }

        val processNote = when (process) {
            "日晒" -> "风门策略：中值偏高，防止表层发展过快"
            "厌氧" -> "风门策略：中值，避免香气前冲过头"
            else -> "风门策略：中值，根据回温和 ROR 微调"
        }

        return listOf(fire, drying, maillard, preCrack, processNote)
    }

    private fun buildRiskAdvice(
        score: Double,
        process: String,
        flavor: String
    ): List<String> {
        val risk1 = when {
            score >= 2.5 -> "前段吸热不足风险"
            score <= -1.2 -> "回温过快风险"
            else -> "系统惯性基本均衡"
        }

        val risk2 = when (process) {
            "日晒", "厌氧" -> "表层发展快于内部的风险"
            else -> "爆前冲高风险"
        }

        val risk3 = when (flavor) {
            "厚重Body" -> "中后段拖闷风险"
            "高风味强度" -> "风味强但容易粗糙的风险"
            else -> "人工修正过度风险"
        }

        return listOf(risk1, risk2, risk3)
    }

    private fun buildExecutionCard(
        rebound: String,
        yellow: String,
        crack: String,
        drop: String,
        score: Double
    ): String {
        val preheat = when {
            score >= 2.5 -> "预热建议：206-208°C"
            score >= 0.5 -> "预热建议：204-206°C"
            else -> "预热建议：202-204°C"
        }

        return "$preheat｜回温 $rebound｜转黄 $yellow｜一爆 $crack｜下豆 $drop"
    }

    private fun minuteText(value: Double): String {
        val minutes = value.toInt()
        val seconds = ((value - minutes) * 100).roundToInt()
        val secText = if (seconds < 10) "0$seconds" else "$seconds"
        return "$minutes:$secText"
    }

    private fun minuteToDouble(text: String): Double {
        val parts = text.split(":")
        if (parts.size != 2) return 8.30
        val minute = parts[0].toDoubleOrNull() ?: 8.0
        val second = parts[1].toDoubleOrNull() ?: 30.0
        return minute + second / 100.0
    }

    private fun format1(value: Double): String {
        return String.format("%.1f", value)
    }
}
