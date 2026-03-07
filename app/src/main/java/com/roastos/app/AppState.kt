package com.roastos.app

object AppState {

    var lastPlannerInput: PlannerInput? = null
    var lastPlannerResult: PlannerResult? = null

    var liveActualTurningSec: Int? = null
    var liveActualYellowSec: Int? = null
    var liveActualFcSec: Int? = null
    var liveActualPreFcRor: Double? = null
    var liveActualDropSec: Int? = null
}
