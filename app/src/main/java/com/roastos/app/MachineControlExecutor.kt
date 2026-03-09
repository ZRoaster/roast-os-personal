package com.roastos.app

data class MachineControlExecutionSummary(
    val machineName: String,
    val success: Boolean,
    val primaryResult: MachineCommandExecutionResult,
    val secondaryResult: MachineCommandExecutionResult? = null,
    val overallStatus: MachineCommandStatus,
    val reason: String,
    val summary: String
)

object MachineControlExecutor {

    fun executePlan(
        adapter: MachineAdapter,
        plan: MachineControlPlan
    ): MachineControlExecutionSummary {

        val primaryResult = adapter.executeCommand(plan.primaryCommand)

        val secondaryResult = when {
            primaryResult.success &&
                primaryResult.status == MachineCommandStatus.EXECUTED &&
                plan.secondaryCommand != null -> {
                adapter.executeCommand(plan.secondaryCommand)
            }

            plan.secondaryCommand != null &&
                primaryResult.status == MachineCommandStatus.PENDING -> {
                MachineCommandExecutionResult(
                    success = false,
                    status = MachineCommandStatus.PENDING,
                    reason = "Secondary command not executed because primary command is pending confirmation",
                    executedCommand = plan.secondaryCommand.copy(
                        status = MachineCommandStatus.PENDING
                    )
                )
            }

            plan.secondaryCommand != null &&
                primaryResult.status == MachineCommandStatus.BLOCKED -> {
                MachineCommandExecutionResult(
                    success = false,
                    status = MachineCommandStatus.BLOCKED,
                    reason = "Secondary command blocked because primary command failed validation",
                    executedCommand = plan.secondaryCommand.copy(
                        status = MachineCommandStatus.BLOCKED
                    )
                )
            }

            else -> null
        }

        val overallStatus = buildOverallStatus(
            primaryResult = primaryResult,
            secondaryResult = secondaryResult
        )

        val success = overallStatus == MachineCommandStatus.EXECUTED ||
            overallStatus == MachineCommandStatus.READY

        val reason = buildReason(
            primaryResult = primaryResult,
            secondaryResult = secondaryResult
        )

        val summary = buildSummary(
            adapter = adapter,
            plan = plan,
            primaryResult = primaryResult,
            secondaryResult = secondaryResult,
            overallStatus = overallStatus,
            reason = reason
        )

        return MachineControlExecutionSummary(
            machineName = plan.machineName,
            success = success,
            primaryResult = primaryResult,
            secondaryResult = secondaryResult,
            overallStatus = overallStatus,
            reason = reason,
            summary = summary
        )
    }

    private fun buildOverallStatus(
        primaryResult: MachineCommandExecutionResult,
        secondaryResult: MachineCommandExecutionResult?
    ): MachineCommandStatus {

        if (primaryResult.status == MachineCommandStatus.FAILED) {
            return MachineCommandStatus.FAILED
        }

        if (primaryResult.status == MachineCommandStatus.BLOCKED) {
            return MachineCommandStatus.BLOCKED
        }

        if (primaryResult.status == MachineCommandStatus.PENDING) {
            return MachineCommandStatus.PENDING
        }

        if (secondaryResult == null) {
            return primaryResult.status
        }

        return when {
            secondaryResult.status == MachineCommandStatus.FAILED ->
                MachineCommandStatus.FAILED

            secondaryResult.status == MachineCommandStatus.BLOCKED ->
                MachineCommandStatus.BLOCKED

            secondaryResult.status == MachineCommandStatus.PENDING ->
                MachineCommandStatus.PENDING

            primaryResult.status == MachineCommandStatus.EXECUTED &&
                secondaryResult.status == MachineCommandStatus.EXECUTED ->
                MachineCommandStatus.EXECUTED

            else ->
                primaryResult.status
        }
    }

    private fun buildReason(
        primaryResult: MachineCommandExecutionResult,
        secondaryResult: MachineCommandExecutionResult?
    ): String {

        return if (secondaryResult == null) {
            primaryResult.reason
        } else {
            primaryResult.reason + " | " + secondaryResult.reason
        }
    }

    private fun buildSummary(
        adapter: MachineAdapter,
        plan: MachineControlPlan,
        primaryResult: MachineCommandExecutionResult,
        secondaryResult: MachineCommandExecutionResult?,
        overallStatus: MachineCommandStatus,
        reason: String
    ): String {

        return """
Machine Control Execution

Machine
${plan.machineName}

Adapter
${adapter.adapterId}

Primary Command
${plan.primaryCommand.type} / ${plan.primaryCommand.status}

Primary Result
${primaryResult.status}

Secondary Command
${plan.secondaryCommand?.type ?: "-"}

Secondary Result
${secondaryResult?.status ?: "-"}

Overall Status
$overallStatus

Reason
$reason

Advisory
${plan.advisoryText}

Confidence
${plan.confidence}
        """.trimIndent()
    }
}
