package com.roastos.app

data class RoastCorrectionV2Result(
    val headline: String,
    val processDirection: String,
    val flavorDirection: String,
    val priority: List<String>,
    val adjustments: List<String>,
    val summary: String
)

object RoastCorrectionBridgeV2 {

    fun buildFromBatch(batchId: String): RoastCorrectionV2Result {

        val entry = RoastHistoryEngine.findByBatchId(batchId)

        if (entry == null) {
            return RoastCorrectionV2Result(
                headline = "Batch not found",
                processDirection = "-",
                flavorDirection = "-",
                priority = listOf("Open valid batch"),
                adjustments = listOf("-"),
                summary = "No roast history"
            )
        }

        val flavorBridge = RoastFlavorBridge.buildFromEntry(entry)

        val predictedFc = entry.predictedFcSec
        val actualFc = entry.actualFcSec

        val predictedDrop = entry.predictedDropSec
        val actualDrop = entry.actualDropSec

        val ror = entry.actualPreFcRor

        val priority = mutableListOf<String>()
        val adjustments = mutableListOf<String>()

        var processDirection = "Stable process"
        var headline = "Roast close to plan"

        if (predictedFc != null && actualFc != null) {

            val delta = actualFc - predictedFc

            if (delta > 20) {

                headline = "FC landed late"

                processDirection = "Increase energy before first crack"

                priority.add("Front-end momentum")

                adjustments.add("Delay heat reduction slightly")
                adjustments.add("Avoid excessive airflow before FC")

            }

            if (delta < -20) {

                headline = "FC landed early"

                processDirection = "Reduce early energy"

                priority.add("Control early heat push")

                adjustments.add("Reduce early heat slightly")
                adjustments.add("Stabilize airflow earlier")
            }
        }

        if (predictedDrop != null && actualDrop != null) {

            val delta = actualDrop - predictedDrop

            if (delta > 20) {

                priority.add("Finish control")

                adjustments.add("Reduce development time slightly")
            }

            if (delta < -20) {

                priority.add("Finish extension")

                adjustments.add("Extend development slightly")
            }
        }

        if (ror != null) {

            if (ror >= 10.5) {

                priority.add("Late acceleration")

                adjustments.add("Reduce heat earlier before FC")
            }

            if (ror <= 7.0) {

                priority.add("Energy collapse")

                adjustments.add("Carry more energy into FC")
            }
        }

        priority.addAll(flavorBridge.priority)

        adjustments.addAll(flavorBridge.suggestions)

        if (priority.isEmpty()) {

            priority.add("Maintain structure")

            adjustments.add("Change one variable at a time")
        }

        val flavorDirection = flavorBridge.flavorDirection

        val summary = """
Unified Correction Bridge

Headline
$headline

Process Direction
$processDirection

Flavor Direction
$flavorDirection

Priority
${priority.distinct().joinToString("\n"){ "• $it" }}

Next Batch Adjustments
${adjustments.distinct().joinToString("\n"){ "• $it" }}
""".trimIndent()

        return RoastCorrectionV2Result(
            headline = headline,
            processDirection = processDirection,
            flavorDirection = flavorDirection,
            priority = priority.distinct(),
            adjustments = adjustments.distinct(),
            summary = summary
        )
    }
}
