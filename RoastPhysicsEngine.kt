package com.roastos.app

data class PhysicsInput(
    val phase: String,
    val currentRor: Double,
    val heatLevelW: Int,
    val airflowPa: Int,
    val drumRpm: Int,

    val envTemp: Double,
    val humidity: Double,
    val pressureKpa: Double,

    val density: Double,
    val moisture: Double,
    val aw: Double
)

data class PhysicsOutput(
    val heatContribution: Double,
    val airLoss: Double,
    val beanLoad: Double,
    val envLoss: Double,
    val phaseAbsorption: Double,
    val netEnergy: Double,
    val predictedRor20s: Double,
    val predictedRor30s: Double,
    val summary: String
)

object RoastPhysicsEngine {

    fun simulate(input: PhysicsInput): PhysicsOutput {

        val heatContribution = heatContribution(input.heatLevelW)
        val airLoss = airLoss(input.airflowPa)
        val beanLoad = beanLoad(
            density = input.density,
            moisture = input.moisture,
            aw = input.aw
        )
        val envLoss = envLoss(
            envTemp = input.envTemp,
            humidity = input.humidity,
            pressureKpa = input.pressureKpa
        )
        val phaseAbsorption = phaseAbsorption(input.phase)
        val drumEffect = drumEffect(input.drumRpm)

        val netEnergy =
            heatContribution -
            airLoss -
            beanLoad -
            envLoss -
            phaseAbsorption +
            drumEffect

        val predictedRor20s =
            (input.currentRor + netEnergy * 0.35)
                .coerceIn(0.0, 30.0)

        val predictedRor30s =
            (input.currentRor + netEnergy * 0.55)
                .coerceIn(0.0, 30.0)

        val summary = when {
            netEnergy > 2.0 ->
                "System energy is rising. ROR likely to increase unless corrected."
            netEnergy < -2.0 ->
                "System energy is falling. ROR likely to drop unless supported."
            else ->
                "System energy is relatively balanced. ROR should stay near current path."
        }

        return PhysicsOutput(
            heatContribution = heatContribution,
            airLoss = airLoss,
            beanLoad = beanLoad,
            envLoss = envLoss,
            phaseAbsorption = phaseAbsorption,
            netEnergy = netEnergy,
            predictedRor20s = predictedRor20s,
            predictedRor30s = predictedRor30s,
            summary = summary
        )
    }

    private fun heatContribution(heatLevelW: Int): Double {
        return when {
            heatLevelW >= 1400 -> 6.0
            heatLevelW >= 1320 -> 5.0
            heatLevelW >= 1240 -> 4.0
            heatLevelW >= 1160 -> 3.0
            heatLevelW >= 1080 -> 2.0
            else -> 1.0
        }
    }

    private fun airLoss(airflowPa: Int): Double {
        return when {
            airflowPa >= 20 -> 4.0
            airflowPa >= 18 -> 3.2
            airflowPa >= 16 -> 2.5
            airflowPa >= 14 -> 1.8
            airflowPa >= 12 -> 1.2
            else -> 0.8
        }
    }

    private fun beanLoad(
        density: Double,
        moisture: Double,
        aw: Double
    ): Double {
        val densityTerm = (density - 800.0) * 0.015
        val moistureTerm = (moisture - 10.5) * 0.55
        val awTerm = (aw - 0.55) * 8.0

        return 2.5 + densityTerm + moistureTerm + awTerm
    }

    private fun envLoss(
        envTemp: Double,
        humidity: Double,
        pressureKpa: Double
    ): Double {
        val tempTerm = (22.0 - envTemp) * 0.08
        val humidityTerm = (humidity - 40.0) * 0.015
        val pressureTerm = (101.3 - pressureKpa) * 0.03

        return 1.5 + tempTerm + humidityTerm + pressureTerm
    }

    private fun phaseAbsorption(phase: String): Double {
        return when (phase) {
            "Drying" -> 3.5
            "Maillard / Pre-FC" -> 2.6
            "Development" -> 1.6
            "Finished" -> 0.0
            else -> 2.4
        }
    }

    private fun drumEffect(drumRpm: Int): Double {
        return when {
            drumRpm >= 8 -> 0.4
            drumRpm == 7 -> 0.2
            drumRpm == 6 -> 0.0
            else -> -0.2
        }
    }
}
