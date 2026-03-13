package com.roastos.app

object RoastMachineProfileSyncEngine {

    fun syncFromBestMatch(
        machineId: String = "hb_m2se_default"
    ): RoastStateModel.MachineState? {
        val match = RoastCalibrationMatcherEngine.matchBest(machineId = machineId)
        val profile = match.matchedProfile ?: return null

        val current = RoastStateModel.machine

        val synced = RoastStateModel.MachineState(
            thermalMass = profile.inertia.thermalInertiaScore ?: current.thermalMass,
            drumMass = profile.inertia.drumInertiaScore ?: current.drumMass,
            heatRetention = profile.inertia.airflowInertiaScore ?: current.heatRetention,

            maxPowerW = current.maxPowerW,
            maxAirPa = current.maxAirPa,
            maxRpm = current.maxRpm,

            powerResponseDelay = profile.delays.heatUpDelaySec ?: current.powerResponseDelay,
            airflowResponseDelay = profile.delays.airflowDelaySec ?: current.airflowResponseDelay,
            rpmResponseDelay = profile.delays.drumSpeedDelaySec ?: current.rpmResponseDelay
        )

        RoastStateModel.machine = synced
        return synced
    }

    fun summaryText(
        machineId: String = "hb_m2se_default"
    ): String {
        val match = RoastCalibrationMatcherEngine.matchBest(machineId = machineId)
        val profile = match.matchedProfile

        if (profile == null) {
            return """
Machine Profile Sync

No matched calibration available.
            """.trimIndent()
        }

        return """
Machine Profile Sync

Matched Calibration
${profile.machineName}

Calibration ID
${profile.calibrationId}

Score
${"%.2f".format(match.score)}

Synced State
Power Response Delay ${profile.delays.heatUpDelaySec ?: "-"} s
Airflow Response Delay ${profile.delays.airflowDelaySec ?: "-"} s
RPM Response Delay ${profile.delays.drumSpeedDelaySec ?: "-"} s

Thermal Mass ${profile.inertia.thermalInertiaScore ?: "-"}
Heat Retention ${profile.inertia.airflowInertiaScore ?: "-"}
Drum Mass ${profile.inertia.drumInertiaScore ?: "-"}
        """.trimIndent()
    }
}
