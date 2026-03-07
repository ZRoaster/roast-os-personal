package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private lateinit var container: LinearLayout

    // Roast Planner inputs
    private lateinit var densityInput: EditText
    private lateinit var moistureInput: EditText
    private lateinit var awInput: EditText
    private lateinit var envTempInput: EditText
    private lateinit var humidityInput: EditText

    private lateinit var processSpinner: Spinner
    private lateinit var roastLevelSpinner: Spinner
    private lateinit var flavorSpinner: Spinner
    private lateinit var batchSpinner: Spinner

    private lateinit var plannerResultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.VERTICAL
        topBar.setPadding(30, 30, 30, 20)

        val title = TextView(this)
        title.text = "Roast OS"
        title.textSize = 28f
        topBar.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "HB M2SE · 200g · Charge 204℃ · 1450W"
        topBar.addView(subtitle)

        root.addView(topBar)

        val scrollView = ScrollView(this)
        container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(30, 20, 30, 20)
        scrollView.addView(container)

        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        scrollParams.weight = 1f
        root.addView(scrollView, scrollParams)

        val navBar = LinearLayout(this)
        navBar.orientation = LinearLayout.HORIZONTAL
        navBar.weightSum = 5f

        val dashboardBtn = navButton("Dashboard")
        val roastBtn = navButton("Roast")
        val beansBtn = navButton("Beans")
        val brewBtn = navButton("Brew")
        val aiBtn = navButton("AI")

        navBar.addView(dashboardBtn)
        navBar.addView(roastBtn)
        navBar.addView(beansBtn)
        navBar.addView(brewBtn)
        navBar.addView(aiBtn)

        root.addView(navBar)

        setContentView(root)

        dashboardBtn.setOnClickListener { showDashboard() }
        roastBtn.setOnClickListener { showRoastPage() }
        beansBtn.setOnClickListener { showBeansPage() }
        brewBtn.setOnClickListener { showBrewPage() }
        aiBtn.setOnClickListener { showAiPage() }

        showDashboard()
    }

    private fun navButton(text: String): Button {
        val btn = Button(this)
        btn.text = text
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        btn.layoutParams = params
        return btn
    }

    private fun clearPage() {
        container.removeAllViews()
    }

    private fun sectionTitle(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 20f
        t.setPadding(0, 20, 0, 10)
        return t
    }

    private fun cardTitle(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 18f
        t.setPadding(0, 10, 0, 10)
        return t
    }

    private fun normalText(text: String): TextView {
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
        e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        return e
    }

    private fun spinner(list: List<String>): Spinner {
        val s = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        s.adapter = adapter
        return s
    }

    private fun addDivider() {
        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        container.addView(divider)
    }

    private fun showDashboard() {
        clearPage()

        container.addView(sectionTitle("Dashboard"))

        container.addView(cardTitle("Machine"))
        container.addView(normalText("HB M2SE"))
        container.addView(normalText("Batch: 200g"))
        container.addView(normalText("Charge Base: 204℃"))
        container.addView(normalText("Max Power: 1450W"))

        container.addView(cardTitle("Today Bean"))
        container.addView(normalText("当前版本先从 Roast Planner 进入选择与生成策略。"))

        container.addView(cardTitle("Quick Access"))
        container.addView(normalText("• Roast Planner：生成第一锅执行卡"))
        container.addView(normalText("• Batch Correction：修正第二锅"))
        container.addView(normalText("• Bean Library：管理豆子 DNA"))

        container.addView(cardTitle("Current Build Status"))
        container.addView(normalText("• 多页面导航骨架已建立"))
        container.addView(normalText("• Roast Planner 已可用"))
        container.addView(normalText("• Beans / Brew / AI 为架构预留位"))
    }

    private fun showRoastPage() {
        clearPage()

        container.addView(sectionTitle("Roast Center"))

        val tabs = LinearLayout(this)
        tabs.orientation = LinearLayout.HORIZONTAL
        tabs.weightSum = 4f

        val plannerBtn = Button(this)
        plannerBtn.text = "Planner"
        plannerBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val liveBtn = Button(this)
        liveBtn.text = "Live"
        liveBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val correctionBtn = Button(this)
        correctionBtn.text = "Correction"
        correctionBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val replayBtn = Button(this)
        replayBtn.text = "Replay"
        replayBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        tabs.addView(plannerBtn)
        tabs.addView(liveBtn)
        tabs.addView(correctionBtn)
        tabs.addView(replayBtn)

        container.addView(tabs)

        val subContainer = LinearLayout(this)
        subContainer.orientation = LinearLayout.VERTICAL
        subContainer.setPadding(0, 20, 0, 0)
        container.addView(subContainer)

        fun showPlanner() {
            subContainer.removeAllViews()

            subContainer.addView(cardTitle("Roast Planner"))

            subContainer.addView(normalText("处理法"))
            processSpinner = spinner(listOf("水洗", "日晒", "蜜处理", "厌氧"))
            subContainer.addView(processSpinner)

            subContainer.addView(normalText("密度"))
            densityInput = numberInput("例如 818")
            subContainer.addView(densityInput)

            subContainer.addView(normalText("含水率"))
            moistureInput = decimalInput("例如 11.1")
            subContainer.addView(moistureInput)

            subContainer.addView(normalText("aw"))
            awInput = decimalInput("例如 0.55")
            subContainer.addView(awInput)

            subContainer.addView(normalText("环境温度"))
            envTempInput = decimalInput("例如 22")
            subContainer.addView(envTempInput)

            subContainer.addView(normalText("湿度"))
            humidityInput = decimalInput("例如 40")
            subContainer.addView(humidityInput)

            subContainer.addView(normalText("烘焙度"))
            roastLevelSpinner = spinner(listOf("浅烘", "浅中", "中烘", "中深"))
            subContainer.addView(roastLevelSpinner)

            subContainer.addView(normalText("风味目标"))
            flavorSpinner = spinner(listOf("干净清晰", "平衡甜感", "高风味强度", "厚重Body"))
            subContainer.addView(flavorSpinner)

            subContainer.addView(normalText("批次模式"))
            batchSpinner = spinner(listOf("单锅", "连续批"))
            subContainer.addView(batchSpinner)

            val generateBtn = Button(this)
            generateBtn.text = "生成 ROAST OS 策略"
            subContainer.addView(generateBtn)

            plannerResultView = TextView(this)
            plannerResultView.text = "\n等待生成策略"
            plannerResultView.textSize = 16f
            subContainer.addView(plannerResultView)

            generateBtn.setOnClickListener { generatePlannerStrategy() }
        }

        fun showLive() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Roast Live"))
            subContainer.addView(normalText("预留位：未来接入 HB M2SE 实时数据"))
            subContainer.addView(normalText("未来显示：BT / ET / ROR / 阶段 / FC 预测 / 下一步动作"))
        }

        fun showCorrection() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Batch Correction"))
            subContainer.addView(normalText("预留位：下一版加入实际 Turning / Yellow / FC / Drop / Pre-FC ROR 输入"))
            subContainer.addView(normalText("目标：生成 Batch 2 修正执行卡"))
        }

        fun showReplay() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Roast Replay"))
            subContainer.addView(normalText("预留位：未来做预测 vs 实际对比、偏差解释、下一锅修正建议"))
        }

        plannerBtn.setOnClickListener { showPlanner() }
        liveBtn.setOnClickListener { showLive() }
        correctionBtn.setOnClickListener { showCorrection() }
        replayBtn.setOnClickListener { showReplay() }

        showPlanner()
    }

    private fun showBeansPage() {
        clearPage()

        container.addView(sectionTitle("Bean Library"))

        container.addView(cardTitle("Bean DNA Center"))
        container.addView(normalText("这里未来存放："))
        container.addView(normalText("• 名称"))
        container.addView(normalText("• 处理法"))
        container.addView(normalText("• 密度"))
        container.addView(normalText("• 含水率"))
        container.addView(normalText("• aw"))
        container.addView(normalText("• 推荐烘焙窗口"))
        container.addView(normalText("• 过往批次表现"))

        container.addView(cardTitle("Current Stage"))
        container.addView(normalText("当前版本先保留 Bean Library 架构位。"))
    }

    private fun showBrewPage() {
        clearPage()

        container.addView(sectionTitle("Brew Center"))
        container.addView(normalText("预留位："))
        container.addView(normalText("• xBloom"))
        container.addView(normalText("• Pour Over"))
        container.addView(normalText("• Espresso"))
        container.addView(normalText("• 研磨建议"))
        container.addView(normalText("• 出品参数"))
    }

    private fun showAiPage() {
        clearPage()

        container.addView(sectionTitle("AI Assistant"))
        container.addView(normalText("预留位："))
        container.addView(normalText("• 为什么这样预测"))
        container.addView(normalText("• 为什么这样修正"))
        container.addView(normalText("• 当前最大风险"))
        container.addView(normalText("• 机器 DNA 学到了什么"))
    }

    private fun generatePlannerStrategy() {
        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavor = flavorSpinner.selectedItem.toString()
        val batch = batchSpinner.selectedItem.toString()

        val inertia =
            (density - 800.0) / 60.0 +
            (moisture - 11.0) * 0.9 +
            (aw - 0.55) * 12.0

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
• Charge后 1380W
• 回温后 1320W
• 转黄阶段 1260W
• 梅纳阶段 1200W
• 爆前阶段 1160W
                """.trimIndent()

            "中" ->
                """
• Charge后 1450W
• 回温后 1380W
• 转黄阶段 1320W
• 梅纳阶段 1260W
• 爆前阶段 1200W
                """.trimIndent()

            else ->
                """
• Charge后 1450W
• 回温后 1450/1380W
• 转黄阶段 1380W
• 梅纳阶段 1320W
• 爆前阶段 1260W
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

        plannerResultView.text =
            """
热惯性指数
• ${String.format("%.2f", inertia)}

热需求等级
• $demandLevel

核心预测
• Turning $turning
• Yellow $yellow
• First Crack $firstCrack
• Drop $drop

ROR轨迹
$rorText

HB M2SE 火力建议
• Charge ${charge}℃
$firePlan

风压建议
$airPlan

Execution Card
• Charge ${charge}℃
• Turning $turning
• Yellow $yellow
• FC $firstCrack
• Drop $drop
            """.trimIndent()
    }
}
