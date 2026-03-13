package com.roastos.app

data class RoastCalibrationSessionDraft(
    val machineId: String,
    val machineName: String,
    val startedAtMillis: Long,
    val environmentProfile: EnvironmentProfile,
    val heatUpDelaySec: Double?,
    val heatDownDelaySec: Double?,
    val airflowDelaySec: Double?,
    val drumSpeedDelaySec: Double?,
    val coolingResponseDelaySec: Double?,
    val thermalInertiaScore: Double?,
    val airflowInertiaScore: Double?,
    val drumInertiaScore: Double?,
    val note: String
) {
    fun summaryText(): String {
        return """
Calibration Draft

Machine
$machineName

Started
$startedAtMillis

Environment
Temp ${environmentProfile.ambientTempC ?: "-"} °C
Humidity ${environmentProfile.ambientHumidityRh ?: "-"} %RH
Altitude ${environmentProfile.altitudeMeters ?: "-"} m

Delays
Heat Up ${heatUpDelaySec ?: "-"} s
Heat Down ${heatDownDelaySec ?: "-"} s
Airflow ${airflowDelaySec ?: "-"} s
Drum ${drumSpeedDelaySec ?: "-"} s
Cooling ${coolingResponseDelaySec ?: "-"} s

Inertia
Thermal ${thermalInertiaScore ?: "-"}
Airflow ${airflowInertiaScore ?: "-"}
Drum ${drumInertiaScore ?: "-"}

Note
${if (note.isBlank()) "-" else note}
        """.trimIndent()
    }
}

object RoastCalibrationSessionEngine {

    private var currentDraft: RoastCalibrationSessionDraft? = null

    fun start(
        machineId: String = "hb_m2se_default",
        machineName: String = "HB M2SE"
    ): RoastCalibrationSessionDraft {
        val env = EnvironmentProfileEngine.current()

        val draft = RoastCalibrationSessionDraft(
            machineId = machineId,
            machineName = machineName,
            startedAtMillis = System.currentTimeMillis(),
            environmentProfile = env,
            heatUpDelaySec = null,
            heatDownDelaySec = null,
            airflowDelaySec = null,
            drumSpeedDelaySec = null,
            coolingResponseDelaySec = null,
            thermalInertiaScore = null,
            airflowInertiaScore = null,
            drumInertiaScore = null,
            note = ""
        )

        currentDraft = draft
        return draft
    }

    fun current(): RoastCalibrationSessionDraft? {
        return currentDraft
    }

    fun update(
        heatUpDelaySec: Double? = currentDraft?.heatUpDelaySec,
        heatDownDelaySec: Double? = currentDraft?.heatDownDelaySec,
        airflowDelaySec: Double? = currentDraft?.airflowDelaySec,
        drumSpeedDelaySec: Double? = currentDraft?.drumSpeedDelaySec,
        coolingResponseDelaySec: Double? = currentDraft?.coolingResponseDelaySec,
        thermalInertiaScore: Double? = currentDraft?.thermalInertiaScore,
        airflowInertiaScore: Double? = currentDraft?.airflowInertiaScore,
        drumInertiaScore: Double? = currentDraft?.drumInertiaScore,
        note: String = currentDraft?.note ?: ""
    ): RoastCalibrationSessionDraft {
        val draft = currentDraft ?: start()

        val updated = draft.copy(
            heatUpDelaySec = heatUpDelaySec,
            heatDownDelaySec = heatDownDelaySec,
            airflowDelaySec = airflowDelaySec,
            drumSpeedDelaySec = drumSpeedDelaySec,
            coolingResponseDelaySec = coolingResponseDelaySec,
            thermalInertiaScore = thermalInertiaScore,
            airflowInertiaScore = airflowInertiaScore,
            drumInertiaScore = drumInertiaScore,
            note = note
        )

        currentDraft = updated
        return updated
    }

    fun commit(): MachineCalibrationProfile {
        val draft = currentDraft ?: start()

        val profile = MachineCalibrationProfile(
            calibrationId = "cal-${System.currentTimeMillis()}",
            machineId = draft.machineId,
            machineName = draft.machineName,
            calibratedAtMillis = System.currentTimeMillis(),
            calibrationEnvironment = draft.environmentProfile,
            delays = MachineDelayProfile(
                heatUpDelaySec = draft.heatUpDelaySec ?: 8.0,
                heatDownDelaySec = draft.heatDownDelaySec ?: 10.0,
                airflowDelaySec = draft.airflowDelaySec ?: 3.0,
                drumSpeedDelaySec = draft.drumSpeedDelaySec ?: 2.0,
                coolingResponseDelaySec = draft.coolingResponseDelaySec ?: 6.0
            ),
            inertia = MachineInertiaProfile(
                thermalInertiaScore = draft.thermalInertiaScore ?: 0.50,
                airflowInertiaScore = draft.airflowInertiaScore ?: 0.40,
                drumInertiaScore = draft.drumInertiaScore ?: 0.30
            ),
            note = if (draft.note.isBlank()) {
                "Calibration committed from session"
            } else {
                draft.note
            }
        )

        MachineDynamicsEngine.save(profile)
        RoastCalibrationHistoryEngine.record(profile)
        currentDraft = null
        return profile
    }

    fun cancel() {
        currentDraft = null
    }
}
