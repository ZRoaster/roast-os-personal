package com.roastos.app

/*
Roast OS Core State Model
Single Source of Truth for the whole system

All Engines read data from here:
RoastEngine
EnergyEngine
RoastPhysicsEngine
DecisionEngine
LiveAssistEngine
AdaptiveCalibrationEngine
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

        // thermal characteristics
        var thermalMass: Double = 1.0,
        var drumMass: Double = 1.0,
        var heatRetention: Double = 1.0,

        // limits
        var maxPowerW: Int = 1200,
        var maxAirPa: Int = 30,
        var maxRpm: Int = 80,

        // response delay
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

        // machine learning memory
        var machineResponseFactor: Double = 1.0
    )

    var calibration = CalibrationState()


    /*
    ===============================
    Reset Functions
    ===============================
    */

    fun resetRoast() {
        roast = RoastState()
        control = ControlState()
    }

}
