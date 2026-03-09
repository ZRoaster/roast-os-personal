package com.roastos.app

enum class MachineCommandType {
    SET_HEAT,
    SET_AIRFLOW,
    SET_DRUM_SPEED,
    HOLD,
    EMERGENCY_STOP
}

enum class MachineCommandStatus {
    PENDING,
    READY,
    BLOCKED,
    EXECUTED,
    FAILED
}

data class MachineControlCommand(
    val machineName: String,
    val type: MachineCommandType,

    val targetHeatW: Int? = null,
    val targetAirflowPa: Int? = null,
    val targetDrumRpm: Int? = null,

    val reason: String,
    val requiresConfirmation: Boolean,
    val status: MachineCommandStatus = MachineCommandStatus.PENDING
) {
    fun summary(): String {
        return """
Machine Control Command

Machine
$machineName

Type
$type

Target Heat
${targetHeatW?.toString()?.plus("W") ?: "-"}

Target Airflow
${targetAirflowPa?.toString()?.plus("Pa") ?: "-"}

Target Drum
${targetDrumRpm?.toString()?.plus("rpm") ?: "-"}

Reason
$reason

Requires Confirmation
${if (requiresConfirmation) "Yes" else "No"}

Status
$status
        """.trimIndent()
    }
}

object MachineControlCommands {

    fun buildHeatCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        targetHeatW: Int,
        reason: String
    ): MachineControlCommand {
        val allowed = capability.canSetHeat

        return MachineControlCommand(
            machineName = profile.name,
            type = MachineCommandType.SET_HEAT,
            targetHeatW = targetHeatW.coerceIn(profile.minPowerW, profile.maxPowerW),
            targetAirflowPa = null,
            targetDrumRpm = null,
            reason = reason,
            requiresConfirmation = capability.requiresConfirmation,
            status = if (allowed) MachineCommandStatus.READY else MachineCommandStatus.BLOCKED
        )
    }

    fun buildAirflowCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        targetAirflowPa: Int,
        reason: String
    ): MachineControlCommand {
        val allowed = capability.canSetAirflow

        return MachineControlCommand(
            machineName = profile.name,
            type = MachineCommandType.SET_AIRFLOW,
            targetHeatW = null,
            targetAirflowPa = targetAirflowPa.coerceIn(profile.minAirflowPa, profile.maxAirflowPa),
            targetDrumRpm = null,
            reason = reason,
            requiresConfirmation = capability.requiresConfirmation,
            status = if (allowed) MachineCommandStatus.READY else MachineCommandStatus.BLOCKED
        )
    }

    fun buildDrumCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        targetDrumRpm: Int,
        reason: String
    ): MachineControlCommand {
        val allowed = capability.canSetDrumSpeed

        return MachineControlCommand(
            machineName = profile.name,
            type = MachineCommandType.SET_DRUM_SPEED,
            targetHeatW = null,
            targetAirflowPa = null,
            targetDrumRpm = targetDrumRpm.coerceIn(profile.minDrumRpm, profile.maxDrumRpm),
            reason = reason,
            requiresConfirmation = capability.requiresConfirmation,
            status = if (allowed) MachineCommandStatus.READY else MachineCommandStatus.BLOCKED
        )
    }

    fun buildHoldCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        reason: String = "Maintain current settings"
    ): MachineControlCommand {
        return MachineControlCommand(
            machineName = profile.name,
            type = MachineCommandType.HOLD,
            targetHeatW = null,
            targetAirflowPa = null,
            targetDrumRpm = null,
            reason = reason,
            requiresConfirmation = false,
            status = MachineCommandStatus.READY
        )
    }

    fun buildEmergencyStopCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        reason: String = "Emergency stop requested"
    ): MachineControlCommand {
        val allowed = capability.supportsEmergencyStop

        return MachineControlCommand(
            machineName = profile.name,
            type = MachineCommandType.EMERGENCY_STOP,
            targetHeatW = null,
            targetAirflowPa = null,
            targetDrumRpm = null,
            reason = reason,
            requiresConfirmation = false,
            status = if (allowed) MachineCommandStatus.READY else MachineCommandStatus.BLOCKED
        )
    }
}
