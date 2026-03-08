package com.roastos.app

/*
Roast OS Core State Model
Single Source of Truth for the whole system
*/

object RoastStateModel {

    /*
    ===============================
    Bean State
    ===============================
    */
    data class BeanState(
        var density: Double = 0.0,
        var moisture: Double = 0.0,
        var aw: Double = 0.0,
        var process: String = "",
        var size: Double = 0.0,
        var ageDays: Int = 0
    )

    var bean = BeanState()

    /*
    ===============================
    Machine State
    ===============================
    */
    data class MachineState(
        var thermalMass: Double = 1.0,
        var drumMass: Double = 1.0,
        var heatRetention: Double = 1.0,

        var maxPowerW: Int = 1450,
        var maxAirPa: Int = 30,
        var maxRpm: Int = 80,

        var powerResponseDelay: Double = 6.0,
        var airflowResponseDelay: Double = 3.0,
        var rpmResponseDelay: Double = 2.0
    )

    var machine = MachineState()

    /*
    ===============================
    Environment State
    ===============================
    */
    data class EnvironmentState(
        var ambientTemp: Double = 20.0,
        var ambientHumidity: Double = 50.0,
        var ambientPressure: Double = 1013.0
    )

    var environment = EnvironmentState()

    /*
    ===============================
    Control State
    ===============================
    */
    data class ControlState(
        var powerW: Int = 0,
        var airflowPa: Int = 0,
        var drumRpm: Int = 0
    )

    var control = ControlState()

    /*
    ===============================
    Roast State
    ===============================
    */
    data class RoastState(
        var timeSec: Int = 0,
        var beanTemp: Double = 0.0,
        var ror: Double = 0.0,
        var phase: String = "Idle",

        var turningSec: Int? = null,
        var yellowSec: Int? = null,
        var fcSec: Int? = null,
        var dropSec: Int? = null
    )

    var roast = RoastState()

    /*
    ===============================
    Calibration State
    ===============================
    */
    data class CalibrationState(
        var fcBias: Double = 0.0,
        var dropBias: Double = 0.0,
        var rorBias: Double = 0.0,

        var heatBias: Double = 0.0,
        var airBias: Double = 0.0,
        var beanBias: Double = 0.0,

        var machineResponseFactor: Double = 1.0,
        var learningCount: Int = 0
    )

    var calibration = CalibrationState()

    /*
    ===============================
    Planner Sync
    ===============================
    */
    fun syncPlannerInput(input: PlannerInput) {
        bean.density = input.density
        bean.moisture = input.moisture
        bean.aw = input.aw
        bean.process = input.process

        environment.ambientTemp = input.envTemp
        environment.ambientHumidity = input.envRH
        environment.ambientPressure = 1013.0
    }

    /*
    ===============================
    Live Sync
    ===============================
    */
    fun syncLiveState(
        phase: String,
        ror: Double,
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?,
        powerW: Int,
        airflowPa: Int,
        drumRpm: Int
    ) {
        roast.phase = phase
        roast.ror = ror
        roast.turningSec = turningSec
        roast.yellowSec = yellowSec
        roast.fcSec = fcSec
        roast.dropSec = dropSec

        control.powerW = powerW
        control.airflowPa = airflowPa
        control.drumRpm = drumRpm
    }

    /*
    ===============================
    Calibration Sync
    ===============================
    */
    fun syncCalibration(fromAppState: com.roastos.app.CalibrationState) {
        calibration.fcBias = fromAppState.fcBiasSec
        calibration.dropBias = fromAppState.dropBiasSec
        calibration.rorBias = 0.0

        calibration.heatBias = fromAppState.heatResponseBias
        calibration.airBias = fromAppState.airResponseBias
        calibration.beanBias = fromAppState.beanLoadBias

        calibration.machineResponseFactor =
            (1.0 + fromAppState.heatResponseBias * 0.08 - fromAppState.airResponseBias * 0.04)
                .coerceIn(0.75, 1.25)

        calibration.learningCount = fromAppState.learningCount
    }

    /*
    ===============================
    Reset Functions
    ===============================
    */
    fun resetRoast() {
        roast = RoastState()
        control = ControlState()
    }

    fun resetCalibration() {
        calibration = CalibrationState()
    }

    fun resetAll() {
        bean = BeanState()
        environment = EnvironmentState()
        roast = RoastState()
        control = ControlState()
        calibration = CalibrationState()
    }
}
