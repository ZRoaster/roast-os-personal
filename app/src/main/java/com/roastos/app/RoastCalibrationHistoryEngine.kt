package com.roastos.app

object RoastCalibrationHistoryEngine {

    private val history = mutableListOf<MachineCalibrationProfile>()

    fun record(
        profile: MachineCalibrationProfile
    ) {
        history.removeAll { it.calibrationId == profile.calibrationId }
        history.add(0, profile)
    }

    fun latest(): MachineCalibrationProfile? {
        return history.firstOrNull()
    }

    fun all(): List<MachineCalibrationProfile> {
        return history.toList()
    }

    fun count(): Int {
        return history.size
    }

    fun clear() {
        history.clear()
    }

    fun findByMachineId(
        machineId: String
    ): List<MachineCalibrationProfile> {
        return history.filter { it.machineId == machineId }
    }

    fun latestForMachine(
        machineId: String
    ): MachineCalibrationProfile? {
        return history.firstOrNull { it.machineId == machineId }
    }
}
