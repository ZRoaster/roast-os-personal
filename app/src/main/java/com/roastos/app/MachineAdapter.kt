package com.roastos.app

enum class MachineAdapterConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class MachineAdapterHealth(
    val adapterId: String,
    val machineName: String,
    val connectionStatus: MachineAdapterConnectionStatus,
    val lastTelemetryAtMillis: Long?,
    val lastError: String?,
    val summary: String
)

data class MachineCommandValidationResult(
    val allowed: Boolean,
    val reason: String
)

data class MachineCommandExecutionResult(
    val success: Boolean,
    val status: MachineCommandStatus,
    val reason: String,
    val executedCommand: MachineControlCommand?
)

interface MachineAdapter {

    val adapterId: String
    val profile: MachineProfile
    val capability: MachineControlCapability

    fun connectionStatus(): MachineAdapterConnectionStatus

    fun connect(): Boolean

    fun disconnect()

    fun latestTelemetryFrame(): MachineTelemetryFrame?

    fun validateCommand(command: MachineControlCommand): MachineCommandValidationResult

    fun executeCommand(command: MachineControlCommand): MachineCommandExecutionResult

    fun health(): MachineAdapterHealth
}

abstract class BaseMachineAdapter(
    override val adapterId: String,
    override val profile: MachineProfile,
    override val capability: MachineControlCapability
) : MachineAdapter {

    protected var status: MachineAdapterConnectionStatus =
        MachineAdapterConnectionStatus.DISCONNECTED

    protected var lastFrame: MachineTelemetryFrame? = null
    protected var lastError: String? = null

    override fun connectionStatus(): MachineAdapterConnectionStatus = status

    override fun latestTelemetryFrame(): MachineTelemetryFrame? = lastFrame

    override fun connect(): Boolean {
        status = MachineAdapterConnectionStatus.CONNECTED
        lastError = null
        return true
    }

    override fun disconnect() {
        status = MachineAdapterConnectionStatus.DISCONNECTED
    }

    override fun validateCommand(
        command: MachineControlCommand
    ): MachineCommandValidationResult {

        if (command.machineName != profile.name) {
            return MachineCommandValidationResult(
                allowed = false,
                reason = "Command machine does not match adapter machine"
            )
        }

        if (status != MachineAdapterConnectionStatus.CONNECTED) {
            return MachineCommandValidationResult(
                allowed = false,
                reason = "Machine adapter is not connected"
            )
        }

        return when (command.type) {
            MachineCommandType.SET_HEAT -> {
                if (!capability.canSetHeat) {
                    MachineCommandValidationResult(false, "Heat control is not supported")
                } else if (command.targetHeatW == null) {
                    MachineCommandValidationResult(false, "Missing target heat")
                } else {
                    MachineCommandValidationResult(true, "Heat command accepted")
                }
            }

            MachineCommandType.SET_AIRFLOW -> {
                if (!capability.canSetAirflow) {
                    MachineCommandValidationResult(false, "Airflow control is not supported")
                } else if (command.targetAirflowPa == null) {
                    MachineCommandValidationResult(false, "Missing target airflow")
                } else {
                    MachineCommandValidationResult(true, "Airflow command accepted")
                }
            }

            MachineCommandType.SET_DRUM_SPEED -> {
                if (!capability.canSetDrumSpeed) {
                    MachineCommandValidationResult(false, "Drum speed control is not supported")
                } else if (command.targetDrumRpm == null) {
                    MachineCommandValidationResult(false, "Missing target drum speed")
                } else {
                    MachineCommandValidationResult(true, "Drum command accepted")
                }
            }

            MachineCommandType.HOLD -> {
                MachineCommandValidationResult(true, "Hold command accepted")
            }

            MachineCommandType.EMERGENCY_STOP -> {
                if (!capability.supportsEmergencyStop) {
                    MachineCommandValidationResult(false, "Emergency stop is not supported")
                } else {
                    MachineCommandValidationResult(true, "Emergency stop accepted")
                }
            }
        }
    }

    override fun executeCommand(
        command: MachineControlCommand
    ): MachineCommandExecutionResult {
        val validation = validateCommand(command)

        if (!validation.allowed) {
            lastError = validation.reason
            return MachineCommandExecutionResult(
                success = false,
                status = MachineCommandStatus.BLOCKED,
                reason = validation.reason,
                executedCommand = command.copy(status = MachineCommandStatus.BLOCKED)
            )
        }

        if (capability.requiresConfirmation) {
            return MachineCommandExecutionResult(
                success = false,
                status = MachineCommandStatus.PENDING,
                reason = "Command requires operator confirmation",
                executedCommand = command.copy(status = MachineCommandStatus.PENDING)
            )
        }

        return performExecution(command)
    }

    protected open fun performExecution(
        command: MachineControlCommand
    ): MachineCommandExecutionResult {
        return MachineCommandExecutionResult(
            success = true,
            status = MachineCommandStatus.EXECUTED,
            reason = "Command executed by adapter",
            executedCommand = command.copy(status = MachineCommandStatus.EXECUTED)
        )
    }

    override fun health(): MachineAdapterHealth {
        val summary = """
Machine Adapter Health

Adapter
$adapterId

Machine
${profile.name}

Connection
$status

Last Telemetry
${lastFrame?.timestampMillis ?: "-"}

Last Error
${lastError ?: "-"}
        """.trimIndent()

        return MachineAdapterHealth(
            adapterId = adapterId,
            machineName = profile.name,
            connectionStatus = status,
            lastTelemetryAtMillis = lastFrame?.timestampMillis,
            lastError = lastError,
            summary = summary
        )
    }
}

class NoOpMachineAdapter(
    profile: MachineProfile,
    capability: MachineControlCapability = MachineControlCapabilities.defaultFor(profile)
) : BaseMachineAdapter(
    adapterId = "noop.${profile.name.lowercase().replace(" ", "_")}",
    profile = profile,
    capability = capability
) {

    override fun connect(): Boolean {
        status = MachineAdapterConnectionStatus.CONNECTED
        lastError = null
        return true
    }

    fun ingestTelemetry(frame: MachineTelemetryFrame) {
        lastFrame = frame
        status = when (frame.connectionState) {
            TelemetryConnectionState.DISCONNECTED -> MachineAdapterConnectionStatus.DISCONNECTED
            TelemetryConnectionState.CONNECTING -> MachineAdapterConnectionStatus.CONNECTING
            TelemetryConnectionState.CONNECTED -> MachineAdapterConnectionStatus.CONNECTED
            TelemetryConnectionState.ERROR -> MachineAdapterConnectionStatus.ERROR
        }
    }
}

class HbM2seReadOnlyAdapter : BaseMachineAdapter(
    adapterId = "hb.m2se.readonly",
    profile = MachineProfiles.HB_M2SE,
    capability = MachineControlCapabilities.HB_M2SE_READ_ONLY
) {

    override fun connect(): Boolean {
        status = MachineAdapterConnectionStatus.CONNECTED
        lastError = null
        return true
    }

    fun updateTelemetry(
        bt: Double,
        et: Double?,
        ror: Double,
        powerW: Int,
        airflowPa: Int,
        drumRpm: Int,
        elapsedSec: Int,
        environmentTemp: Double,
        environmentHumidity: Double,
        source: String = "HB_ADAPTER",
        machineStateLabel: String = "RUNNING"
    ) {
        lastFrame = MachineTelemetryFrame(
            machineName = profile.name,
            source = source,
            connectionState = TelemetryConnectionState.CONNECTED,
            controlMode = TelemetryControlMode.READ_ONLY,
            timestampMillis = System.currentTimeMillis(),
            bt = bt,
            et = et,
            ror = ror,
            powerW = powerW.coerceIn(profile.minPowerW, profile.maxPowerW),
            airflowPa = airflowPa.coerceIn(profile.minAirflowPa, profile.maxAirflowPa),
            drumRpm = drumRpm.coerceIn(profile.minDrumRpm, profile.maxDrumRpm),
            elapsedSec = elapsedSec.coerceAtLeast(0),
            environmentTemp = environmentTemp,
            environmentHumidity = environmentHumidity,
            machineStateLabel = machineStateLabel
        )
        status = MachineAdapterConnectionStatus.CONNECTED
        lastError = null
    }

    override fun executeCommand(
        command: MachineControlCommand
    ): MachineCommandExecutionResult {
        return MachineCommandExecutionResult(
            success = false,
            status = MachineCommandStatus.BLOCKED,
            reason = "HB read-only adapter does not support command execution",
            executedCommand = command.copy(status = MachineCommandStatus.BLOCKED)
        )
    }
}

object MachineAdapters {

    fun defaultHbReadOnly(): MachineAdapter {
        return HbM2seReadOnlyAdapter()
    }

    fun noOpFor(profile: MachineProfile): MachineAdapter {
        return NoOpMachineAdapter(profile)
    }
}
