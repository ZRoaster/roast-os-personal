package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private lateinit var container: LinearLayout

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

    private fun showDashboard() {
        clearPage()

        container.addView(sectionTitle("Dashboard"))

        container.addView(cardTitle("Machine"))
        container.addView(normalText("HB M2SE"))
        container.addView(normalText("Batch: 200g"))
        container.addView(normalText("Charge Base: 204℃"))
        container.addView(normalText("Max Power: 1450W"))

        container.addView(cardTitle("Quick Access"))
        container.addView(normalText("• Roast Planner：调用原生 RoastEngine"))
        container.addView(normalText("• Batch Correction：下一步实装"))
        container.addView(normalText("• Bean Library：架构预留位"))

        container.addView(cardTitle("Current Build"))
        container.addView(normalText("• Android 原生多页面骨架"))
        container.addView(normalText("• Planner 已切到 RoastEngine"))
        container.addView(normalText("• Beans / Brew / AI 先保留架构位"))
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
            subContainer.addView(cardTitle("Roast Live"))
            subContainer.addView(normalText("预留位：未来接入 BT / ET / ROR 实时数据"))
            subContainer.addView(normalText("未来显示：阶段、目标 ROR、FC 预测、下一步动作"))
        }

        fun showCorrection() {
            subContainer.removeAllViews()
            subContainer.addView(cardTitle("Batch Correction"))
            subContainer.addView(normalText("下一步实装："))
            subContainer.addView(normalText("• 实际 Turning"))
            subContainer.addView(normalText("• 实际 Yellow"))
            subContainer.addView(normalText("• 实际 FC"))
            subContainer.addView(normalText("• 实际 Drop"))
            subContainer.addView(normalText("• 实际 Pre-FC ROR"))
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

    private fun generatePlannerStrategy() {
        val density = densityInput.text.toString().toDoubleOrNull() ?: 820.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 10.5
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 35.0

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
            "平衡甜感" -> "stable"
            "高风味强度" -> "stable"
            "厚重Body" -> "thick"
            else -> "clean"
        }

        val input = PlannerInput(
            process = process,
            density = density,
            moisture = moisture,
            aw = aw,
            envTemp = envTemp,
            envRH = humidity,
            roastLevel = roastLevel,
            purpose = "pourover",
            orientation = orientation,
            mode = if (batchCn == "连续批") "M2" else "M1",
            ttSec = 80,
            tySec = 250
        )

        val result = RoastEngine.calcCard(input)

        plannerResultView.text = """
Bean Process
${result.ptLabel}

Charge BT
${result.chargeBT}℃

RPM
${result.rpm}

Preheat / Dev PA
${result.preheatPa}Pa / ${result.devPa}Pa

Predicted First Crack
FC1 ${RoastEngine.toMMSS(result.fc1)}
FC2 ${result.fc2?.let { RoastEngine.toMMSS(it) } ?: "-"}
FC Pred ${RoastEngine.toMMSS(result.fcPredSec)}

Drop / Development
Drop ${RoastEngine.toMMSS(result.dropSec)}
Dev ${result.devTime}s
DTR ${"%.1f".format(result.dtrPercent)}%

Heat Plan
H1 ${result.h1W}W @ ${RoastEngine.toMMSS(result.h1Sec)}
H2 ${result.h2W}W @ ${RoastEngine.toMMSS(result.h2Sec)}
H3 ${result.h3W}W @ ${RoastEngine.toMMSS(result.h3Sec)}
H4 ${result.h4W}W @ ${RoastEngine.toMMSS(result.h4Sec)}
H5 ${result.h5W}W @ ${RoastEngine.toMMSS(result.h5Sec)}

Air Plan
Wind1 ${result.wind1Pa}Pa @ ${RoastEngine.toMMSS(result.wind1Sec)}
Wind2 ${result.wind2Pa}Pa @ ${RoastEngine.toMMSS(result.wind2Sec)}
Protect @ ${RoastEngine.toMMSS(result.protectSec)}

ROR Targets
Target 1 ${"%.1f".format(result.rorTargets[0])}
Target 2 ${"%.1f".format(result.rorTargets[1])}
Target 3 ${"%.1f".format(result.rorTargets[2])}

ROR Full
${result.rorFull.joinToString(" / ") { "%.1f".format(it) }}

Flags
awTol ${"%.1f".format(result.awTol)}
M3 Protected ${if (result.m3Protected) "YES" else "NO"}
LowDens Assist ${if (result.m3LowDens) "YES" else "NO"}
        """.trimIndent()
    }
}
