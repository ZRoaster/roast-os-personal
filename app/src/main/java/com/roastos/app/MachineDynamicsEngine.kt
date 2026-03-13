package com.roastos.app

object MachineDynamicsEngine {

    private var currentProfile: MachineCalibrationProfile? = null

    fun current(): MachineCalibrationProfile {
        val manual = currentProfile
        if (manual != null) return manual

        val matched = matchedProfileOrNull()
        if (matched != null) {
            currentProfile = matched
            return matched
        }

        return defaultProfile().also {
            currentProfile = it
        }
    }

    fun peek(): MachineCalibrationProfile? {
        return currentProfile
    }

    fun save(profile: MachineCalibrationProfile) {
        currentProfile = profile
    }

    fun reset() {
        currentProfile = null
    }

    fun defaultProfile(): MachineCalibrationProfile {
        return MachineCalibrationProfile(
            calibrationId = buildCalibrationId(),
            machineId = "hb_m2se_default",
            machineName = "HB M2SE",
            calibratedAtMillis = System.currentTimeMillis(),
            calibrationEnvironment = EnvironmentProfile(
                altitudeMeters = null,
                ambientTempC = 25.0,
                ambientHumidityRh = 50.0,
                barometricPressureHpa = null,
                note = "Default environment"
            ),
            delays = MachineDelayProfile(
                heatUpDelaySec = 8.0,
                heatDownDelaySec = 10.0,
                airflowDelaySec = 3.0,
                drumSpeedDelaySec = 2.0,
                coolingResponseDelaySec = 6.0
            ),
            inertia = MachineInertiaProfile(
                thermalInertiaScore = 0.50,
                airflowInertiaScore = 0.40,
                drumInertiaScore = 0.30
            ),
            note = "Default machine dynamics profile"
        )
    }

    fun summary(): String {
        return current().summary()
    }

    fun currentAdjustedForEnvironment(): MachineCalibrationProfile {
        val env = EnvironmentProfileEngine.current()

        return applyEnvironmentOffset(
            ambientTempC = env.ambientTempC,
            ambientHumidityRh = env.ambientHumidityRh,
            altitudeMeters = env.altitudeMeters
        )
    }

    fun adjustedSummary(): String {
        val adjusted = currentAdjustedForEnvironment()
        val envComp = EnvironmentCompensationEngine.evaluate()
        val matched = matchedProfileOrNull()

        return """
Machine Dynamics Adjusted

Machine
${adjusted.machineName}

Calibration ID
${adjusted.calibrationId}

Matched Calibration
${matched?.calibrationId ?: "manual/default"}

Adjusted Environment
Altitude: ${adjusted.calibrationEnvironment.altitudeMeters ?: "-"} m
Temp: ${adjusted.calibrationEnvironment.ambientTempC ?: "-"} °C
Humidity: ${adjusted.calibrationEnvironment.ambientHumidityRh ?: "-"} %

Adjusted Delays
Heat Up Delay: ${adjusted.delays.heatUpDelaySec ?: "-"} s
Heat Down Delay: ${adjusted.delays.heatDownDelaySec ?: "-"} s
Airflow Delay: ${adjusted.delays.airflowDelaySec ?: "-"} s
Drum Delay: ${adjusted.delays.drumSpeedDelaySec ?: "-"} s
Cooling Delay: ${adjusted.delays.coolingResponseDelaySec ?: "-"} s

Compensation
Heat Retention: ${formatOffset(envComp.heatRetentionOffset)}
Drying: ${formatOffset(envComp.dryingOffset)}
Airflow Efficiency: ${formatOffset(envComp.airflowEfficiencyOffset)}
Pressure: ${formatOffset(envComp.pressureOffset)}
        """.trimIndent()
    }

    fun applyEnvironmentOffset(
        ambientTempC: Double?,
        ambientHumidityRh: Double?,
        altitudeMeters: Int?
    ): MachineCalibrationProfile {
        val base = current()

        val baseTemp = base.calibrationEnvironment.ambientTempC
        val baseHumidity = base.calibrationEnvironment.ambientHumidityRh
        val baseAltitude = base.calibrationEnvironment.altitudeMeters

        val tempDelta = if (ambientTempC != null && baseTemp != null) {
            ambientTempC - baseTemp
        } else {
            0.0
        }

        val humidityDelta = if (ambientHumidityRh != null && baseHumidity != null) {
            ambientHumidityRh - baseHumidity
        } else {
            0.0
        }

        val altitudeDelta = if (altitudeMeters != null && baseAltitude != null) {
            (altitudeMeters - baseAltitude).toDouble()
        } else {
            0.0
        }

        val adjustedDelays = MachineDelayProfile(
            heatUpDelaySec = adjustDelay(
                baseValue = base.delays.heatUpDelaySec,
                tempDelta = tempDelta,
                humidityDelta = humidityDelta,
                altitudeDelta = altitudeDelta
            ),
            heatDownDelaySec = adjustDelay(
                baseValue = base.delays.heatDownDelaySec,
                tempDelta = -tempDelta * 0.5,
                humidityDelta = humidityDelta * 0.3,
                altitudeDelta = altitudeDelta
            ),
            airflowDelaySec = adjustDelay(
                baseValue = base.delays.airflowDelaySec,
                tempDelta = 0.0,
                humidityDelta = humidityDelta * 0.1,
                altitudeDelta = altitudeDelta * 0.002
            ),
            drumSpeedDelaySec = base.delays.drumSpeedDelaySec,
            coolingResponseDelaySec = adjustDelay(
                baseValue = base.delays.coolingResponseDelaySec,
                tempDelta = -tempDelta * 0.4,
                humidityDelta = humidityDelta * 0.2,
                altitudeDelta = altitudeDelta * 0.001
            )
        )

        return base.copy(
            calibrationEnvironment = base.calibrationEnvironment.copy(
                altitudeMeters = altitudeMeters ?: base.calibrationEnvironment.altitudeMeters,
                ambientTempC = ambientTempC ?: base.calibrationEnvironment.ambientTempC,
                ambientHumidityRh = ambientHumidityRh ?: base.calibrationEnvironment.ambientHumidityRh
            ),
            delays = adjustedDelays,
            note = buildAdjustedNote(base, matchedProfileOrNull())
        )
    }

    private fun matchedProfileOrNull(): MachineCalibrationProfile? {
        return RoastCalibrationMatcherEngine
            .matchBest(machineId = "hb_m2se_default")
            .matchedProfile
    }

    private fun buildAdjustedNote(
        base: MachineCalibrationProfile,
        matched: MachineCalibrationProfile?
    ): String {
        return when {
            currentProfile != null -> "Environment-adjusted profile from manual/current calibration"
            matched != null -> "Environment-adjusted profile from matched calibration ${matched.calibrationId}"
            else -> "Environment-adjusted profile from default calibration"
        }
    }

    private fun adjustDelay(
        baseValue: Double?,
        tempDelta: Double,
        humidityDelta: Double,
        altitudeDelta: Double
    ): Double? {
        if (baseValue == null) return null

        val adjusted =
            baseValue +
                (-tempDelta * 0.08) +
                (humidityDelta * 0.01) +
                (altitudeDelta * 0.0005)

        return adjusted.coerceAtLeast(0.5)
    }

    private fun formatOffset(value: Double): String {
        return if (value >= 0) {
            "+${String.format("%.2f", value)}"
        } else {
            String.format("%.2f", value)
        }
    }

    private fun buildCalibrationId(): String {
        return "cal-${System.currentTimeMillis()}"
    }
}
