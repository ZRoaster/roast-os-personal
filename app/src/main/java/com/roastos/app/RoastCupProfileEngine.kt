package com.roastos.app

data class RoastCupProfile(
    val roastStyle: String,
    val bodyLevel: String,
    val acidityLevel: String,
    val sweetnessLevel: String,
    val clarityLevel: String,
    val flavorPrediction: String,

    val brewMethod: String,
    val brewTempC: Int,
    val brewRatio: String,
    val grindLevel: String,
    val brewNote: String
)

object RoastCupProfileEngine {

    fun evaluate(log: RoastLog): RoastCupProfile {

        val devRatio = log.developmentRatio ?: 0.0
        val finalRor = log.finalRor ?: 0.0
        val dropTemp = log.dropTemp ?: 0.0

        val roastStyle = detectRoastStyle(dropTemp, devRatio)
        val body = detectBody(devRatio)
        val acidity = detectAcidity(finalRor)
        val sweetness = detectSweetness(devRatio)
        val clarity = detectClarity(finalRor)

        val flavor = buildFlavorPrediction(
            roastStyle,
            body,
            acidity,
            sweetness,
            clarity
        )

        val brew = buildBrewRecommendation(
            roastStyle,
            body,
            acidity,
            clarity
        )

        return RoastCupProfile(
            roastStyle = roastStyle,
            bodyLevel = body,
            acidityLevel = acidity,
            sweetnessLevel = sweetness,
            clarityLevel = clarity,
            flavorPrediction = flavor,

            brewMethod = brew.first,
            brewTempC = brew.second,
            brewRatio = brew.third,
            grindLevel = brew.fourth,
            brewNote = brew.fifth
        )
    }

    private fun detectRoastStyle(
        dropTemp: Double,
        devRatio: Double
    ): String {

        return when {
            dropTemp < 198 -> "浅烘"
            dropTemp < 205 -> "浅中烘"
            dropTemp < 212 -> "中烘"
            else -> "中深烘"
        }
    }

    private fun detectBody(
        devRatio: Double
    ): String {

        return when {
            devRatio < 0.16 -> "轻"
            devRatio < 0.20 -> "中等"
            else -> "厚"
        }
    }

    private fun detectAcidity(
        ror: Double
    ): String {

        return when {
            ror > 6 -> "明亮"
            ror > 4 -> "柔和"
            else -> "低"
        }
    }

    private fun detectSweetness(
        devRatio: Double
    ): String {

        return when {
            devRatio < 0.15 -> "轻"
            devRatio < 0.19 -> "中"
            else -> "高"
        }
    }

    private fun detectClarity(
        ror: Double
    ): String {

        return when {
            ror > 5 -> "高"
            ror > 3 -> "中"
            else -> "低"
        }
    }

    private fun buildFlavorPrediction(
        roastStyle: String,
        body: String,
        acidity: String,
        sweetness: String,
        clarity: String
    ): String {

        return """
烘焙风格
$roastStyle

酸质
$acidity

甜感
$sweetness

醇厚度
$body

干净度
$clarity
        """.trimIndent()
    }

    private fun buildBrewRecommendation(
        roastStyle: String,
        body: String,
        acidity: String,
        clarity: String
    ): Quintuple<String, Int, String, String, String> {

        if (roastStyle == "浅烘") {

            return Quintuple(
                "V60",
                93,
                "1:15",
                "中细",
                "快速流速突出香气"
            )
        }

        if (roastStyle == "浅中烘") {

            return Quintuple(
                "V60 / Origami",
                92,
                "1:15",
                "中细",
                "平衡甜感与酸质"
            )
        }

        if (roastStyle == "中烘") {

            return Quintuple(
                "Kalita / 手冲",
                91,
                "1:16",
                "中",
                "突出甜感与结构"
            )
        }

        return Quintuple(
            "意式 / 美式",
            90,
            "1:2",
            "中细",
            "适合浓缩与奶咖"
        )
    }

    data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
