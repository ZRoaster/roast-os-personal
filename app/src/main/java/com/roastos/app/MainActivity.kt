package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*
import kotlin.math.roundToInt

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
        processSpinner = spinner(listOf("水洗","日晒","蜜处理","厌氧"))
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
        roastLevelSpinner = spinner(listOf("浅烘","浅中","中烘","中深"))
        root.addView(roastLevelSpinner)

        root.addView(label("风味目标"))
        flavorSpinner = spinner(listOf("干净清晰","平衡甜感","高风味强度","厚重Body"))
        root.addView(flavorSpinner)

        root.addView(label("批次模式"))
        batchSpinner = spinner(listOf("单锅","连续批"))
        root.addView(batchSpinner)

        val button = Button(this)
        button.text = "\n生成 ROAST OS 策略"
        root.addView(button)

        resultView = TextView(this)
        resultView.text = "\n等待生成策略"
        root.addView(resultView)

        button.setOnClickListener { generateStrategy() }

        scroll.addView(root)
        setContentView(scroll)
    }

    fun label(text:String):TextView{
        val t = TextView(this)
        t.text = text
        return t
    }

    fun spinner(list:List<String>):Spinner{
        val s = Spinner(this)
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,list)
        s.adapter = adapter
        return s
    }

    fun numberInput(hint:String):EditText{
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        return e
    }

    fun decimalInput(hint:String):EditText{
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        return e
    }

    fun generateStrategy(){

        val density = densityInput.text.toString().toDoubleOrNull() ?: 800.0
        val moisture = moistureInput.text.toString().toDoubleOrNull() ?: 11.0
        val aw = awInput.text.toString().toDoubleOrNull() ?: 0.55
        val envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0
        val humidity = humidityInput.text.toString().toDoubleOrNull() ?: 40.0

        val process = processSpinner.selectedItem.toString()
        val roastLevel = roastLevelSpinner.selectedItem.toString()
        val flavor = flavorSpinner.selectedItem.toString()
        val batch = batchSpinner.selectedItem.toString()

        var thermal = 0.0

        thermal += (density - 800) / 18
        thermal += (moisture - 11) * 2.8
        thermal += (aw - 0.55) * 55
        thermal += (22 - envTemp) / 3
        thermal += (45 - humidity) / 18

        if(process=="日晒") thermal+=0.8
        if(process=="厌氧") thermal+=1.2
        if(batch=="连续批") thermal-=0.8

        val rebound = if(thermal>2) "1:30-1:45" else if(thermal>0) "1:20-1:35" else "1:10-1:25"
        val yellow = if(thermal>2) "4:25-4:55" else "4:05-4:35"
        val crack = if(thermal>2) "8:40-9:10" else "8:20-8:50"
        val drop = if(roastLevel=="浅烘") "9:10-9:40" else "9:30-10:00"

        val ror1 = "18-20"
        val ror2 = "13-15"
        val ror3 = "10-12"
        val ror4 = "8-9"
        val ror5 = "5-6"

        resultView.text = """

热惯性指数
• ${String.format("%.1f",thermal)}

核心预测
• 回温预计 $rebound
• 转黄建议 $yellow
• 一爆预计 $crack
• 下豆区间 $drop

ROR轨迹
• 回温后ROR $ror1
• 转黄ROR $ror2
• 梅纳ROR $ror3
• 爆前ROR $ror4
• 发展ROR $ror5

HB M2SE火力建议
• 0:00 1000W
• 回温后 900W
• 转黄阶段 800W
• 爆前阶段 700W

风压建议
• 脱水阶段 5-10 Pa
• 梅纳阶段 10-15 Pa
• 爆前阶段 15-20 Pa

过程控制
• 初始火力根据热惯性调整
• 脱水阶段稳推
• 梅纳阶段保持连续热流
• 爆前提前小收火稳ROR

风险提示
• 前段吸热不足风险
• 爆前冲高风险
• 中后段拖闷风险

Roast OS执行卡
• 预热 202-206°C
• 回温 $rebound
• 转黄 $yellow
• 一爆 $crack
• 下豆 $drop

        """.trimIndent()
    }
}
