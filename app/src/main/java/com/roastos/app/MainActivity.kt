package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
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
    private lateinit var envTempInput: EditText
    private lateinit var humidityInput: EditText

    private lateinit var processSpinner: Spinner
    private lateinit var roastLevelSpinner: Spinner
    private lateinit var flavorGoalSpinner: Spinner
    private lateinit var freshnessSpinner: Spinner
    private lateinit var beanShapeSpinner: Spinner
    private lateinit var batchModeSpinner: Spinner

    private lateinit var resultCore: TextView
    private lateinit var resultControl: TextView
    private lateinit var resultRisk: TextView
    private lateinit var resultNotes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootScroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(24))
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootScroll.addView(root)

        root.addView(buildHeader())
        root.addView(sectionTitle("Bean Input"))
        root.addView(buildInputCard())

        root.addView(sectionTitle("Environment"))
        root.addView(buildEnvironmentCard())

        val generateButton = Button(this)
        generateButton.text = "生成 Roast OS 策略"
        generateButton.textSize = 16f
        generateButton.setPadding(dp(12), dp(14), dp(12), dp(14))
        generateButton.setOnClickListener { generateStrategy() }
        root.addView(spaced(generateButton, 16))

        root.addView(sectionTitle("Strategy Output"))
        root.addView(buildOutputCard())

        root.addView(sectionTitle("Roast Notes"))
        root.addView(buildNotesCard())

        setContentView(rootScroll)
    }

    private fun buildHeader(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(dp(4), dp(4), dp(4), dp(12))

        val titleView = TextView(this)
        titleView.text = "Roast OS"
        titleView.textSize = 28f

        val subTitleView = TextView(this)
        subTitleView.text = "HB M2SE · 200g · 单锅决策引擎"
        subTitleView.textSize = 14f

        layout.addView(titleView)
        layout.addView(spaced(subTitleView, 6))
        return layout
    }

    private fun buildInputCard(): LinearLayout {
        val card = cardContainer()

        processSpinner = spinnerOf(listOf("水洗", "日晒", "蜜处理", "厌氧", "湿刨", "其他"))
        roastLevelSpinner = spinnerOf(listOf("浅", "浅中", "中浅", "中", "中深"))
        flavorGoalSpinner = spinnerOf(listOf("平衡清晰", "甜感优先", "层次优先", "干净度优先", "body优先"))
        freshnessSpinner = spinnerOf(listOf("正常", "偏新", "偏陈"))
        beanShapeSpinner = spinnerOf(listOf("小粒", "中等", "大粒"))

        densityInput = numberInput("例如 818")
        moistureInput = decimalInput("例如 11.1")
        awInput = decimalInput("例如 0.550")

        card.addView(fieldLabel("处理法"))
        card.addView(processSpinner)

        card.addView(spaced(fieldLabel("密度（g/L）"), 12))
        card.addView(densityInput)

        card.addView(spaced(fieldLabel("含水率（%）"), 12))
        card.addView(moistureInput)

        card.addView(spaced(fieldLabel("aw"), 12))
        card.addView(awInput)

        card.addView(spaced(fieldLabel("新鲜度"), 12))
        card.addView(freshnessSpinner)

        card.addView(spaced(fieldLabel("豆形"), 12))
        card.addView(beanShapeSpinner)

        card.addView(spaced(fieldLabel("目标烘焙度"), 12))
        card.addView(roastLevelSpinner)

        card.addView(spaced(fieldLabel("风味目标"), 12))
        card.addView(flavorGoalSpinner)

        return card
    }

    private fun buildEnvironmentCard(): LinearLayout {
        val card = cardContainer()

        envTempInput = decimalInput("例如 21.7")
        humidityInput = numberInput("例如 35")
        batchModeSpinner = spinnerOf(listOf("单锅", "连续批"))

        card.addView(fieldLabel("环境温度（℃）"))
        card.addView(envTempInput)

        card.addView(spaced(fieldLabel("湿度（RH）"), 12))
        card.addView(humidityInput)

        card.addView(spaced(fieldLabel("批次状态"), 12))
        card.addView(batchModeSpinner)

        return card
    }

    private fun buildOutputCard(): LinearLayout {
        val card = cardContainer()

        resultCore = outputBlock("核心预测", "等待生成策略")
        resultControl = outputBlock("过程控制", "等待生成策略")
        resultRisk = outputBlock("风险提示", "等待生成策略")

        card.addView(resultCore)
        card.addView(spaced(resultControl, 14))
        card.addView(spaced(resultRisk, 14))

        return card
    }

    private fun buildNotesCard(): LinearLayout {
        val card = cardContainer()
        resultNotes = outputBlock("执行备注", "输入参数后点击“生成 Roast OS 策略”。")
        card.addView(resultNotes)
        return card
    }

    private fun generateStrategy() {
        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavorGoal = flavorGoalSpinner.selectedItem.toString()
        val freshness = freshnessSpinner.selectedItem.toString()
        val beanShape = beanShapeSpinner.selectedItem.toString()
        val batchMode = batchModeSpinner.selectedItem.toString()

        var thermalScore = 0.0
        thermalScore += (density - 800.0) / 25.0
        thermalScore += (moisture - 11.0) * 2.0
        thermalScore += (aw - 0.55) * 80.0
        if (process == "日晒" || process == "厌氧") thermalScore += 1.2
        if (batchMode == "连续批") thermalScore += 1.0
        thermalScore -= (envTemp - 22.0) / 4.0
        thermalScore -= (humidity - 40.0) / 20.0

        val reboundText = when {
            thermalScore >= 3.0 -> "回温偏慢，预计 1:35–1:50"
            thermalScore >= 1.0 -> "回温正常偏慢，预计 1:25–1:40"
            thermalScore >= -1.0 -> "回温正常，预计 1:15–1:30"
            else -> "回温偏快，预计 1:05–1:20"
        }

        val yellowText = when {
            roastLevel == "浅" || roastLevel == "浅中" -> "转黄建议 4:10–4:40"
            roastLevel == "中浅" -> "转黄建议 4:20–4:50"
            else -> "转黄建议 4:30–5:00"
        }

        val crackText = when {
            density >= 830 && moisture >= 11.3 -> "一爆锚点偏后，预计 8:50–9:25"
            process == "日晒" || process == "厌氧" -> "一爆锚点注意提前监听，预计 8:20–8:55"
            else -> "一爆锚点正常，预计 8:30–9:05"
        }

        val dropText = when (roastLevel) {
            "浅" -> "下豆区间建议 9:15–9:45"
            "浅中" -> "下豆区间建议 9:30–10:00"
            "中浅" -> "下豆区间建议 9:45–10:20"
            "中" -> "下豆区间建议 10:00–10:40"
            else -> "下豆区间建议 10:20–11:00"
        }

        val initialHeat = when {
            thermalScore >= 3.0 -> "初始火力略高于常规，避免前段吸热不足。"
            thermalScore >= 1.0 -> "初始火力中高，前段保持推动。"
            thermalScore >= -1.0 -> "初始火力按常规中值执行。"
            else -> "初始火力略收，防止回温过快。"
        }

        val initialAir = when (flavorGoal) {
            "干净度优先" -> "初始风压略高，脱水中段提前给风。"
            "body优先" -> "初始风压略低，避免前段失水过快。"
            else -> "初始风压中值，按回温走势微调。"
        }

        val drying = when {
            moisture >= 11.4 || aw >= 0.57 -> "脱水阶段避免急推，允许前段稍厚，但不要闷。"
            process == "日晒" -> "脱水阶段控制过猛对流，防止外干内湿。"
            else -> "脱水阶段目标是稳推，不追求表面快。"
        }

        val maillard = when (flavorGoal) {
            "甜感优先" -> "梅纳阶段延续稳定热量，避免过早大幅减火。"
            "层次优先" -> "梅纳阶段保持清晰推进，控制 ROR 连续下滑。"
            "body优先" -> "梅纳阶段可稍厚，但避免拖慢。"
            else -> "梅纳阶段以稳定转折为核心，不做大动作。"
        }

        val preCrack = when (roastLevel) {
            "浅", "浅中" -> "爆前重点稳 ROR，建议提前一刀小收火，避免冲爆。"
            else -> "爆前避免拖闷，收火不要过早，关注香气展开。"
        }

        val dev = when (roastLevel) {
            "浅" -> "发展阶段偏短，保留明亮与清晰。"
            "浅中" -> "发展阶段短中段，平衡甜感与清晰。"
            "中浅" -> "发展阶段中等，兼顾层次与圆润。"
            else -> "发展阶段适当拉长，但要防止尾端发木。"
        }

        val risks = ArrayList<String>()
        if (thermalScore >= 3.0) risks.add("前段吸热不足风险")
        if (thermalScore <= -1.5) risks.add("回温过快与爆前冲高风险")
        if (process == "日晒" || process == "厌氧") risks.add("表层发展快于内部的风险")
        if (flavorGoal == "body优先") risks.add("中后段拖闷风险")
        if (risks.isEmpty()) risks.add("系统惯性较均衡，重点防止人工过度修正")

        val notes = StringBuilder()
        notes.append("机器：HB M2SE · 批量：200g\n")
        notes.append("处理法：").append(process).append("，烘焙度：").append(roastLevel).append("，目标：").append(flavorGoal).append("\n")
        notes.append("新鲜度：").append(freshness).append("，豆形：").append(beanShape).append("，批次：").append(batchMode).append("\n\n")
        notes.append("执行原则：\n")
        notes.append("1. 第一刀不要太晚，优先让系统顺而不是追表面曲线。\n")
        notes.append("2. 爆前控制以“稳 ROR”优先，不要临近一爆再大修正。\n")
        notes.append("3. 若实际回温明显偏离预测，后续一爆预期要整体平移。")

        resultCore.text = buildOutputText("核心预测", listOf(reboundText, yellowText, crackText, dropText))
        resultControl.text = buildOutputText("过程控制", listOf(initialHeat, initialAir, drying, maillard, preCrack, dev))
        resultRisk.text = buildOutputText("风险提示", risks)
        resultNotes.text = buildOutputText("执行备注", notes.toString().split("\n"))
    }

    private fun buildOutputText(title: String, lines: List<String>): String {
        val builder = StringBuilder()
        builder.append(title).append("\n")
        for (line in lines) {
            if (line.isNotBlank()) {
                builder.append("• ").append(line).append("\n")
            }
        }
        return builder.toString().trim()
    }

    private fun sectionTitle(text: String): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = 18f
        view.setPadding(dp(2), dp(18), dp(2), dp(10))
        return view
    }

    private fun fieldLabel(text: String): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = 14f
        return view
    }

    private fun numberInput(valueHint: String): EditText {
        val input = EditText(this)
        input.hint = valueHint
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setSingleLine()
        input.setPadding(dp(12), dp(10), dp(12), dp(10))
        return input
    }

    private fun decimalInput(valueHint: String): EditText {
        val input = EditText(this)
        input.hint = valueHint
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setSingleLine()
        input.setPadding(dp(12), dp(10), dp(12), dp(10))
        return input
    }

    private fun spinnerOf(items: List<String>): Spinner {
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        return spinner
    }

    private fun cardContainer(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(dp(14), dp(14), dp(14), dp(14))
        layout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return layout
    }

    private fun outputBlock(title: String, text: String): TextView {
        val view = TextView(this)
        view.text = title + "\n" + text
        view.textSize = 15f
        return view
    }

    private fun spaced(view: android.view.View, top: Int): android.view.View {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = dp(top)
        view.layoutParams = params
        return view
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Roast OS"

        val rootScroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        rootScroll.addView(root)

        root.addView(buildHeader())
        root.addView(sectionTitle("Bean Input"))
        root.addView(buildInputCard())

        root.addView(sectionTitle("Environment"))
        root.addView(buildEnvironmentCard())

        val generateButton = Button(this).apply {
            text = "生成 Roast OS 策略"
            textSize = 16f
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener { generateStrategy() }
        }
        root.addView(spaced(generateButton, top = 16))

        root.addView(sectionTitle("Strategy Output"))
        root.addView(buildOutputCard())

        root.addView(sectionTitle("Roast Notes"))
        root.addView(buildNotesCard())

        setContentView(rootScroll)
    }

    private fun buildHeader(): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(12))
        }

        val titleView = TextView(this).apply {
            text = "Roast OS"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        }

        val subTitleView = TextView(this).apply {
            text = "HB M2SE · 200g · 单锅决策引擎"
            textSize = 14f
        }

        layout.addView(titleView)
        layout.addView(spaced(subTitleView, top = 6))
        return layout
    }

    private fun buildInputCard(): LinearLayout {
        val card = cardContainer()

        processSpinner = spinnerOf(
            listOf("水洗", "日晒", "蜜处理", "厌氧", "湿刨", "其他")
        )
        roastLevelSpinner = spinnerOf(
            listOf("浅", "浅中", "中浅", "中", "中深")
        )
        flavorGoalSpinner = spinnerOf(
            listOf("平衡清晰", "甜感优先", "层次优先", "干净度优先", "body优先")
        )
        freshnessSpinner = spinnerOf(
            listOf("正常", "偏新", "偏陈")
        )
        beanShapeSpinner = spinnerOf(
            listOf("小粒", "中等", "大粒")
        )

        densityInput = numberInput("密度（g/L）", "例如 818")
        moistureInput = decimalInput("含水率（%）", "例如 11.1")
        awInput = decimalInput("aw", "例如 0.550")

        card.addView(fieldLabel("处理法"))
        card.addView(processSpinner)

        card.addView(spaced(fieldLabel("密度（g/L）"), top = 12))
        card.addView(densityInput)

        card.addView(spaced(fieldLabel("含水率（%）"), top = 12))
        card.addView(moistureInput)

        card.addView(spaced(fieldLabel("aw"), top = 12))
        card.addView(awInput)

        card.addView(spaced(fieldLabel("新鲜度"), top = 12))
        card.addView(freshnessSpinner)

        card.addView(spaced(fieldLabel("豆形"), top = 12))
        card.addView(beanShapeSpinner)

        card.addView(spaced(fieldLabel("目标烘焙度"), top = 12))
        card.addView(roastLevelSpinner)

        card.addView(spaced(fieldLabel("风味目标"), top = 12))
        card.addView(flavorGoalSpinner)

        return card
    }

    private fun buildEnvironmentCard(): LinearLayout {
        val card = cardContainer()

        envTempInput = decimalInput("环境温度（℃）", "例如 21.7")
        humidityInput = numberInput("湿度（RH）", "例如 35")
        batchModeSpinner = spinnerOf(
            listOf("单锅", "连续批")
        )

        card.addView(fieldLabel("环境温度（℃）"))
        card.addView(envTempInput)

        card.addView(spaced(fieldLabel("湿度（RH）"), top = 12))
        card.addView(humidityInput)

        card.addView(spaced(fieldLabel("批次状态"), top = 12))
        card.addView(batchModeSpinner)

        return card
    }

    private fun buildOutputCard(): LinearLayout {
        val card = cardContainer()

        resultCore = outputBlock(
            "核心预测",
            "等待生成策略"
        )

        resultControl = outputBlock(
            "过程控制",
            "等待生成策略"
        )

        resultRisk = outputBlock(
            "风险提示",
            "等待生成策略"
        )

        card.addView(resultCore)
        card.addView(spaced(resultControl, top = 14))
        card.addView(spaced(resultRisk, top = 14))

        return card
    }

    private fun buildNotesCard(): LinearLayout {
        val card = cardContainer()

        resultNotes = outputBlock(
            "执行备注",
            "输入参数后点击“生成 Roast OS 策略”。"
        )

        card.addView(resultNotes)
        return card
    }

    private fun generateStrategy() {
        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavorGoal = flavorGoalSpinner.selectedItem.toString()
        val freshness = freshnessSpinner.selectedItem.toString()
        val beanShape = beanShapeSpinner.selectedItem.toString()
        val batchMode = batchModeSpinner.selectedItem.toString()

        var thermalScore = 0.0
        thermalScore += (density - 800.0) / 25.0
        thermalScore += (moisture - 11.0) * 2.0
        thermalScore += (aw - 0.55) * 80.0
        thermalScore += if (process == "日晒" || process == "厌氧") 1.2 else 0.0
        thermalScore += if (batchMode == "连续批") 1.0 else 0.0
        thermalScore -= (envTemp - 22.0) / 4.0
        thermalScore -= (humidity - 40.0) / 20.0

        val reboundText = when {
            thermalScore >= 3.0 -> "回温偏慢，预计 1:35–1:50"
            thermalScore >= 1.0 -> "回温正常偏慢，预计 1:25–1:40"
            thermalScore >= -1.0 -> "回温正常，预计 1:15–1:30"
            else -> "回温偏快，预计 1:05–1:20"
        }

        val yellowText = when {
            roastLevel == "浅" || roastLevel == "浅中" -> "转黄建议 4:10–4:40"
            roastLevel == "中浅" -> "转黄建议 4:20–4:50"
            else -> "转黄建议 4:30–5:00"
        }

        val crackText = when {
            density >= 830 && moisture >= 11.3 -> "一爆锚点偏后，预计 8:50–9:25"
            process == "日晒" || process == "厌氧" -> "一爆锚点注意提前监听，预计 8:20–8:55"
            else -> "一爆锚点正常，预计 8:30–9:05"
        }

        val dropText = when (roastLevel) {
            "浅" -> "下豆区间建议 9:15–9:45"
            "浅中" -> "下豆区间建议 9:30–10:00"
            "中浅" -> "下豆区间建议 9:45–10:20"
            "中" -> "下豆区间建议 10:00–10:40"
            else -> "下豆区间建议 10:20–11:00"
        }

        val initialHeat = when {
            thermalScore >= 3.0 -> "初始火力略高于常规，避免前段吸热不足。"
            thermalScore >= 1.0 -> "初始火力中高，前段保持推动。"
            thermalScore >= -1.0 -> "初始火力按常规中值执行。"
            else -> "初始火力略收，防止回温过快。"
        }

        val initialAir = when (flavorGoal) {
            "干净度优先" -> "初始风压略高，脱水中段提前给风。"
            "body优先" -> "初始风压略低，避免前段失水过快。"
            else -> "初始风压中值，按回温走势微调。"
        }

        val drying = when {
            moisture >= 11.4 || aw >= 0.57 -> "脱水阶段避免急推，允许前段稍厚，但不要闷。"
            process == "日晒" -> "脱水阶段控制过猛对流，防止外干内湿。"
            else -> "脱水阶段目标是稳推，不追求表面快。"
        }

        val maillard = when (flavorGoal) {
            "甜感优先" -> "梅纳阶段延续稳定热量，避免过早大幅减火。"
            "层次优先" -> "梅纳阶段保持清晰推进，控制 ROR 连续下滑。"
            "body优先" -> "梅纳阶段可稍厚，但避免拖慢。"
            else -> "梅纳阶段以稳定转折为核心，不做大动作。"
        }

        val preCrack = when (roastLevel) {
            "浅", "浅中" -> "爆前重点稳 ROR，建议提前一刀小收火，避免冲爆。"
            else -> "爆前避免拖闷，收火不要过早，关注香气展开。"
        }

        val dev = when (roastLevel) {
            "浅" -> "发展阶段偏短，保留明亮与清晰。"
            "浅中" -> "发展阶段短中段，平衡甜感与清晰。"
            "中浅" -> "发展阶段中等，兼顾层次与圆润。"
            else -> "发展阶段适当拉长，但要防止尾端发木。"
        }

        val risks = mutableListOf<String>()
        if (thermalScore >= 3.0) risks.add("前段吸热不足风险")
        if (thermalScore <= -1.5) risks.add("回温过快与爆前冲高风险")
        if (process == "日晒" || process == "厌氧") risks.add("表层发展快于内部的风险")
        if (flavorGoal == "body优先") risks.add("中后段拖闷风险")
        if (risks.isEmpty()) risks.add("系统惯性较均衡，重点防止人工过度修正")

        val notes = buildString {
            append("机器：HB M2SE · 批量：200g\n")
            append("处理法：$process，烘焙度：$roastLevel，目标：$flavorGoal\n")
            append("新鲜度：$freshness，豆形：$beanShape，批次：$batchMode\n\n")
            append("执行原则：\n")
            append("1. 第一刀不要太晚，优先让系统顺而不是追表面曲线。\n")
            append("2. 爆前控制以“稳 ROR”优先，不要临近一爆再大修正。\n")
            append("3. 若实际回温明显偏离预测，后续一爆预期要整体平移。")
        }

        resultCore.text = buildOutputText(
            "核心预测",
            listOf(
                reboundText,
                yellowText,
                crackText,
                dropText
            )
        )

        resultControl.text = buildOutputText(
            "过程控制",
            listOf(
                initialHeat,
                initialAir,
                drying,
                maillard,
                preCrack,
                dev
            )
        )

        resultRisk.text = buildOutputText(
            "风险提示",
            risks
        )

        resultNotes.text = buildOutputText(
            "执行备注",
            notes.split("\n")
        )
    }

    private fun buildOutputText(title: String, lines: List<String>): String {
        return buildString {
            append(title)
            append("\n")
            lines.filter { it.isNotBlank() }.forEach {
                append("• ")
                append(it)
                append("\n")
            }
        }.trim()
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(2), dp(18), dp(2), dp(10))
        }
    }

    private fun fieldLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
        }
    }

    private fun numberInput(hint: String, valueHint: String): EditText {
        return EditText(this).apply {
            this.hint = valueHint
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine()
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
    }

    private fun decimalInput(hint: String, valueHint: String): EditText {
        return EditText(this).apply {
            this.hint = valueHint
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine()
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
    }

    private fun spinnerOf(items: List<String>): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )
        }
    }

    private fun cardContainer(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun outputBlock(title: String, text: String): TextView {
        return TextView(this).apply {
            this.text = "$title\n$text"
            textSize = 15f
            setLineSpacing(0f, 1.25f)
            gravity = Gravity.START
        }
    }

    private fun spaced(view: android.view.View, top: Int = 0): android.view.View {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = dp(top)
        view.layoutParams = params
        return view
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
