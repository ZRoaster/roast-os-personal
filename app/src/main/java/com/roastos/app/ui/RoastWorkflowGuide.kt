package com.roastos.app.ui

import com.roastos.app.AppState
import com.roastos.app.BatchSessionEngine

object RoastWorkflowGuide {

    data class WorkflowState(
        val currentStep: String,
        val nextStep: String,
        val progressLabel: String,
        val checklist: List<String>
    )

    fun build(): WorkflowState {
        val plannerReady = AppState.lastPlannerResult != null && AppState.lastPlannerInput != null
        val session = BatchSessionEngine.current()
        val sessionStatus = session?.status ?: "Idle"

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualDrop = AppState.liveActualDropSec

        if (!plannerReady) {
            return WorkflowState(
                currentStep = "Planning not ready",
                nextStep = "Run Planner to generate roast baseline",
                progressLabel = "Step 1 / 6",
                checklist = listOf(
                    "Run Planner",
                    "Review predicted anchors",
                    "Prepare to start batch"
                )
            )
        }

        if (session == null || sessionStatus == "Idle") {
            return WorkflowState(
                currentStep = "Planner ready",
                nextStep = "Start Batch when charging beans",
                progressLabel = "Step 2 / 6",
                checklist = listOf(
                    "Planner complete",
                    "Charge beans",
                    "Press Start Batch"
                )
            )
        }

        if (sessionStatus == "Running") {
            return when {
                actualTurning == null -> WorkflowState(
                    currentStep = "Batch running: pre-turning",
                    nextStep = "Record Turning point",
                    progressLabel = "Step 3 / 6",
                    checklist = listOf(
                        "Monitor turning",
                        "Watch front-end energy",
                        "Save actual Turning"
                    )
                )

                actualYellow == null -> WorkflowState(
                    currentStep = "Drying phase",
                    nextStep = "Record Yellow point",
                    progressLabel = "Step 3 / 6",
                    checklist = listOf(
                        "Guide drying phase",
                        "Manage heat momentum",
                        "Save actual Yellow"
                    )
                )

                actualFc == null -> WorkflowState(
                    currentStep = "Maillard / Pre-FC phase",
                    nextStep = "Monitor ROR and record FC",
                    progressLabel = "Step 4 / 6",
                    checklist = listOf(
                        "Watch ROR trend",
                        "Manage airflow and heat",
                        "Save actual FC"
                    )
                )

                actualDrop == null -> WorkflowState(
                    currentStep = "Development phase",
                    nextStep = "Prepare drop and record Drop point",
                    progressLabel = "Step 5 / 6",
                    checklist = listOf(
                        "Control development",
                        "Avoid crash / flick",
                        "Save actual Drop",
                        "Finish Batch"
                    )
                )

                else -> WorkflowState(
                    currentStep = "Batch complete",
                    nextStep = "Finish Batch and run Correction",
                    progressLabel = "Step 6 / 6",
                    checklist = listOf(
                        "Review actual anchors",
                        "Press Finish Batch",
                        "Go to Correction page"
                    )
                )
            }
        }

        if (sessionStatus == "Finished" || sessionStatus == "Corrected") {
            return WorkflowState(
                currentStep = "Batch finished",
                nextStep = "Run Correction, then Reset Batch for next roast",
                progressLabel = "Step 6 / 6",
                checklist = listOf(
                    "Review curve and deviations",
                    "Run Correction",
                    "Apply learning",
                    "Reset Batch before next roast"
                )
            )
        }

        return WorkflowState(
            currentStep = "Workflow standby",
            nextStep = "Check planner and batch state",
            progressLabel = "Step ? / 6",
            checklist = listOf(
                "Verify planner",
                "Verify batch session",
                "Verify live input"
            )
        )
    }

    fun buildText(): String {
        val state = build()

        return """
Progress
${state.progressLabel}

Current Step
${state.currentStep}

Next Step
${state.nextStep}

Checklist
${state.checklist.joinToString("\n") { "• $it" }}
        """.trimIndent()
    }
}
