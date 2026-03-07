package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {

    private lateinit var container: LinearLayout

    // Planner inputs
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

    // Correction inputs
    private lateinit var actualTurningInput: EditText
    private lateinit var actualYellowInput: EditText
    private lateinit var actualFcInput: EditText
    private lateinit var actualDropInput: EditText
    private lateinit var actualPreFcRorInput: EditText
    private lateinit var correctionResultView: TextView

    // Shared state
    private var lastPlannerInput: PlannerInput? = null
    private var lastPlannerResult: PlannerResult? = null

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
        t.setPadding(0, 16, 0, 10)
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

    private fun textInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        return e
    }

    private fun spinner(list: List<String>): Spinner {
        val s = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        s.adapter = adapter
        return s
    }

    private fun showDashboard() {
        clearPage()

        container.addView(sectionTitle("Dashboard"))

        container.addView(cardTitle("Machine"))
        container.addView(normalText("HB M2SE"))
        container.addView(normalText("Batch: 200g"))
        container.addView(normalText("Charge Base: 204℃"))
        container.addView(normalText("Max Power: 1450W"))

        container.addView(cardTitle("Quick Access"))
        container.addView(normalText("• Roast Planner：生成第一锅执行卡"))
        container.addView(normalText("• Live Assist：关键点即时修正"))
        container.addView(normalText("• Batch Correction：第二锅修正执行卡"))

        container.addView(cardTitle("Current Build"))
        container.addView(normalText("• Planner 已接原生 RoastEngine"))
        container.addView(normalText("• Live 已接 Live Assist v1"))
        container.addView(normalText("• Correction 已接原生 CorrectionEngine"))
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
            processSpinner = spinner(listOf("水洗", "蜜处理", "日晒", "厌氧"))
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

            generateBtn.setOnClickListener {
                generatePlannerStrategy()
            }
        }

        fun showLive() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Live Assist v1"))

            if (lastPlannerResult == null) {
                val hint = TextView(this)
                hint.text = "请先在 Planner 中生成第一锅策略，再进入 Live Assist。"
                subContainer.addView(hint)
                return
            }

            val predicted = lastPlannerResult!!

            val predTurning = (predicted.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val predYellow = predicted.h2Sec.toInt()
            val predFc = predicted.fcPredSec.toInt()
            val predDrop = predicted.dropSec.toInt()

            subContainer.addView(normalText("当前预测"))
            subContainer.addView(normalText("• Turning ${RoastEngine.toMMSS(predTurning.toDouble())}"))
            subContainer.addView(normalText("• Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}"))
            subContainer.addView(normalText("• FC ${RoastEngine.toMMSS(predFc.toDouble())}"))
            subContainer.addView(normalText("• Drop ${RoastEngine.toMMSS(predDrop.toDouble())}"))

            // Turning Assist
            subContainer.addView(cardTitle("Turning Assist"))
            val turningTimeInput = textInput("实际 Turning，例如 1:28")
            val turningRorInput = decimalInput("当前 ROR（可选，例如 18.5）")
            val turningBtn = Button(this)
            turningBtn.text = "修正到 Yellow"
            val turningResult = TextView(this)

            subContainer.addView(turningTimeInput)
            subContainer.addView(turningRorInput)
            subContainer.addView(turningBtn)
            subContainer.addView(turningResult)

            turningBtn.setOnClickListener {
                val actualTurning = RoastEngine.parseMMSS(turningTimeInput.text.toString())
                if (actualTurning == null) {
                    turningResult.text = "请输入正确的 Turning 时间，例如 1:28"
                    return@setOnClickListener
                }

                val diff = actualTurning - predTurning

                val message = when {
                    diff > 8 -> """
回温偏慢 ${diff}s

建议
• H2 +60W
• 1风门延后 10s
• Yellow 新预测：${RoastEngine.toMMSS((predYellow + diff / 2.0).coerceAtMost(predYellow + 25.0))}
• 目标：把转黄重新拉回窗口
                    """.trimIndent()

                    diff < -8 -> """
回温偏快 ${-diff}s

建议
• H2 -60W
• 1风门提前 10s
• Yellow 新预测：${RoastEngine.toMMSS((predYellow + diff / 2.0).coerceAtLeast(predYellow - 25.0))}
• 目标：防止中段推进过快
                    """.trimIndent()

                    else -> """
回温基本正常

建议
• 保持 H2
• 保持 1风门节奏
• Yellow 仍按原窗口观察
                    """.trimIndent()
                }

                turningResult.text = message
            }

            // Yellow Assist
            subContainer.addView(cardTitle("Yellow Assist"))
            val yellowTimeInput = textInput("实际 Yellow，例如 4:25")
            val yellowRorInput = decimalInput("当前 ROR，例如 13.5")
            val yellowBtn = Button(this)
            yellowBtn.text = "修正到 FC"
            val yellowResult = TextView(this)

            subContainer.addView(yellowTimeInput)
            subContainer.addView(yellowRorInput)
            subContainer.addView(yellowBtn)
            subContainer.addView(yellowResult)

            yellowBtn.setOnClickListener {
                val actualYellow = RoastEngine.parseMMSS(yellowTimeInput.text.toString())
                val currentRor = yellowRorInput.text.toString().toDoubleOrNull()

                if (actualYellow == null || currentRor == null) {
                    yellowResult.text = "请输入正确的 Yellow 时间和 ROR。"
                    return@setOnClickListener
                }

                val diff = actualYellow - predYellow

                val message = when {
                    diff > 15 -> """
转黄偏慢 ${diff}s

建议
• H3 +60W
• 2风门延后 10-15s
• FC 新预测：${RoastEngine.toMMSS((predFc + diff * 0.6).coerceAtMost(predFc + 35.0))}
• 目标：避免一爆过晚
                    """.trimIndent()

                    diff < -15 -> """
转黄偏快 ${-diff}s

建议
• H3 -60W
• 2风门提前 10-15s
• Protect 提前 10s
• FC 新预测：${RoastEngine.toMMSS((predFc + diff * 0.6).coerceAtLeast(predFc - 35.0))}
• 目标：避免爆前冲高
                    """.trimIndent()

                    currentRor > 14.0 -> """
转黄后 ROR 偏高

建议
• H3 -40W
• 风门 +2Pa
• Protect 提前 10s
• 目标：压制梅纳冲高
                    """.trimIndent()

                    else -> """
转黄阶段基本正常

建议
• H3 保持
• 风门保持
• FC 仍按原预测推进
                    """.trimIndent()
                }

                yellowResult.text = message
            }

            // FC Assist
            subContainer.addView(cardTitle("First Crack Assist"))
            val fcTimeInput = textInput("实际 FC，例如 8:42")
            val preFcRorInput = decimalInput("Pre-FC ROR，例如 9.5")
            val fcBtn = Button(this)
            fcBtn.text = "修正发展段"
            val fcResult = TextView(this)

            subContainer.addView(fcTimeInput)
            subContainer.addView(preFcRorInput)
            subContainer.addView(fcBtn)
            subContainer.addView(fcResult)

            fcBtn.setOnClickListener {
                val actualFc = RoastEngine.parseMMSS(fcTimeInput.text.toString())
                val preFcRor = preFcRorInput.text.toString().toDoubleOrNull()

                if (actualFc == null || preFcRor == null) {
                    fcResult.text = "请输入正确的 FC 时间和 Pre-FC ROR。"
                    return@setOnClickListener
                }

                val diff = actualFc - predFc

                val message = when {
                    preFcRor > 10.0 -> """
爆前 ROR 偏高

建议
• H4 -60W
• 风门 +2Pa
• 发展控制 75-85s
• Drop 参考：${RoastEngine.toMMSS((actualFc + 80.0))}
                    """.trimIndent()

                    preFcRor < 7.0 -> """
爆前能量不足

建议
• H4 +40W
• 风门保持
• 发展控制 85-95s
• Drop 参考：${RoastEngine.toMMSS((actualFc + 90.0))}
                    """.trimIndent()

                    diff > 15 -> """
FC 偏慢 ${diff}s

建议
• 发展段不要过长
• H4 保持或微增
• Drop 参考：${RoastEngine.toMMSS((actualFc + 85.0))}
                    """.trimIndent()

                    diff < -15 -> """
FC 偏快 ${-diff}s

建议
• H4 微降
• 风门微增
• 发展控制 70-80s
• Drop 参考：${RoastEngine.toMMSS((actualFc + 75.0))}
                    """.trimIndent()

                    else -> """
爆前状态正常

建议
• 保持当前火力
• 保持当前风门
• 发展控制 75-90s
• Drop 参考：${RoastEngine.toMMSS((actualFc + 82.0))}
                    """.trimIndent()
                }

                fcResult.text = message
            }
        }

        fun showCorrection() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Batch Correction"))

            subContainer.addView(normalText("实际 Turning (mm:ss)"))
            actualTurningInput = textInput("例如 1:28")
            subContainer.addView(actualTurningInput)

            subContainer.addView(normalText("实际 Yellow (mm:ss)"))
            actualYellowInput = textInput("例如 4:35")
            subContainer.addView(actualYellowInput)

            subContainer.addView(normalText("实际 First Crack (mm:ss)"))
            actualFcInput = textInput("例如 8:52")
            subContainer.addView(actualFcInput)

            subContainer.addView(normalText("实际 Drop (mm:ss)"))
            actualDropInput = textInput("例如 9:38")
            subContainer.addView(actualDropInput)

            subContainer.addView(normalText("实际 Pre-FC ROR"))
            actualPreFcRorInput = decimalInput("例如 9.5")
            subContainer.addView(actualPreFcRorInput)

            val correctionBtnInner = Button(this)
            correctionBtnInner.text = "生成 Batch 2 修正策略"
            subContainer.addView(correctionBtnInner)

            correctionResultView = TextView(this)
            correctionResultView.text = "\n等待生成 Batch 2 修正策略"
            correctionResultView.textSize = 16f
            subContainer.addView(correctionResultView)

            correctionBtnInner.setOnClickListener {
                generateCorrectionStrategy()
            }

            if (lastPlannerResult == null) {
                correctionResultView.text = "请先在 Planner 中生成第一锅策略。"
            }
        }

        fun showReplay() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Roast Replay"))
            subContainer.addView(normalText("预留位：未来做预测 vs 实际对比与复盘"))
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
        container.addView(normalText("预留位："))
        container.addView(normalText("• 名称"))
        container.addView(normalText("• 处理法"))
        container.addView(normalText("• 密度"))
        container.addView(normalText("• 含水率"))
        container.addView(normalText("• aw"))
        container.addView(normalText("• 推荐烘焙窗口"))
        container.addView(normalText("• 历史批次表现"))
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

    private fun buildPlannerInputFromUi(): PlannerInput {

    val density = densityInput.text.toString().toDoubleOrNull() ?: 820.0
    val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
    val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
    val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
    val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

    val processCn = processSpinner.selectedItem.toString()
    val roastLevelCn = roastLevelSpinner.selectedItem.toString()
    val flavorCn = flavorSpinner.selectedItem.toString()
    val batchCn = batchSpinner.selectedItem.toString()

    val process = when (processCn) {
        "水洗" -> "washed"
        "蜜处理" -> "honey_washed"
        "日晒" -> "natural"
        "厌氧" -> "anaerobic"
        else -> "washed"
    }

    val roastLevel = when (roastLevelCn) {
        "浅烘" -> "light"
        "浅中" -> "light_medium"
        "中烘" -> "medium"
        "中深" -> "medium_dark"
        else -> "light_medium"
    }

    val orientation = when (flavorCn) {
        "干净清晰" -> "clean"
        "均衡" -> "stable"
        "厚重" -> "thick"
        else -> "stable"
    }

    val batch = when (batchCn) {
        "第一锅" -> 1
        "第二锅" -> 2
        "第三锅" -> 3
        else -> 1
    }

    return PlannerInput(
        process = process,
        density = density,
        moisture = moisture,
        aw = aw,
        envTemp = envTemp,
        envRH = humidity,

        roastLevel = roastLevel,
        orientation = orientation,

        purpose = "pour_over",

        batchNum = batch,

        beanSize = "normal",
        mode = "M2",

        learnM = 0.8,
        learnK = 340.0,
        learnW = 0.6,

        ttSec = 85,
        tySec = null
    )
    }
