package com.roastos.app

enum class MachineControlLevel {
    READ_ONLY,
    ASSISTED,
    FULL_CONTROL
}

data class MachineControlCapability(
    val machineName: String,

    val controlLevel: MachineControlLevel,

    val canReadTelemetry: Boolean,
    val canSetHeat: Boolean,
    val canSetAirflow: Boolean,
    val canSetDrumSpeed: Boolean,

    val canAutoExecute: Boolean,
    val requiresConfirmation: Boolean,
    val supportsEmergencyStop: Boolean,

    val notes: String = ""
) {
    fun summary(): String {
        return """
Machine Control Capability

Machine
$machineName

Control Level
$controlLevel

Read Telemetry
${yesNo(canReadTelemetry)}

Set Heat
${yesNo(canSetHeat)}

Set Airflow
${yesNo(canSetAirflow)}

Set Drum Speed
${yesNo(canSetDrumSpeed)}

Auto Execute
${yesNo(canAutoExecute)}

Requires Confirmation
${yesNo(requiresConfirmation)}

Emergency Stop
${yesNo(supportsEmergencyStop)}

Notes
${if (notes.isBlank()) "-" else notes}
        """.trimIndent()
    }

    private fun yesNo(value: Boolean): String {
        return if (value) "Yes" else "No"
    }
}

object MachineControlCapabilities {

    val HB_M2SE_READ_ONLY = MachineControlCapability(
        machineName = "HB M2SE",
        controlLevel = MachineControlLevel.READ_ONLY,
        canReadTelemetry = true,
        canSetHeat = false,
        canSetAirflow = false,
        canSetDrumSpeed = false,
        canAutoExecute = false,
        requiresConfirmation = false,
        supportsEmergencyStop = false,
        notes = "Current integration is telemetry-first. Control path reserved for future adapter upgrade."
    )

    val HB_M2SE_ASSISTED = MachineControlCapability(
        machineName = "HB M2SE",
        controlLevel = MachineControlLevel.ASSISTED,
        canReadTelemetry = true,
        canSetHeat = true,
        canSetAirflow = true,
        canSetDrumSpeed = false,
        canAutoExecute = false,
        requiresConfirmation = true,
        supportsEmergencyStop = true,
        notes = "Suggested future mode for semi-automatic control with operator confirmation."
    )

    val GENERIC_FULL_CONTROL = MachineControlCapability(
        machineName = "Generic Full Control Machine",
        controlLevel = MachineControlLevel.FULL_CONTROL,
        canReadTelemetry = true,
        canSetHeat = true,
        canSetAirflow = true,
        canSetDrumSpeed = true,
        canAutoExecute = true,
        requiresConfirmation = false,
        supportsEmergencyStop = true,
        notes = "Reference profile for future fully automated machines."
    )

    fun defaultFor(profile: MachineProfile): MachineControlCapability {
        return when (profile.name) {
            "HB M2SE" -> HB_M2SE_READ_ONLY
            else -> MachineControlCapability(
                machineName = profile.name,
                controlLevel = MachineControlLevel.READ_ONLY,
                canReadTelemetry = true,
                canSetHeat = false,
                canSetAirflow = false,
                canSetDrumSpeed = false,
                canAutoExecute = false,
                requiresConfirmation = false,
                supportsEmergencyStop = false,
                notes = "Fallback capability profile."
            )
        }
    }
}
