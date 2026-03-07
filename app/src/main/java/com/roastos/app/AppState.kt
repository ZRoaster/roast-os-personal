package com.roastos.app

object AppState {

    // Planner
    var lastPlannerInput: PlannerInput? = null
    var lastPlannerResult: PlannerResult? = null

    // Live Assist actual anchors
    var liveActualTurningSec: Int? = null
    var liveActualYellowSec: Int? = null
    var liveActualFcSec: Int? = null
    var liveActualDropSec: Int? = null
    var liveActualPreFcRor: Double? = null


    /*
    Reset current roast batch
    Planner stays
     */
    fun resetBatch() {

        liveActualTurningSec = null
        liveActualYellowSec = null
        liveActualFcSec = null
        liveActualDropSec = null
        liveActualPreFcRor = null
    }


    /*
    Full reset (planner + batch)
     */
    fun resetAll() {

        resetBatch()

        lastPlannerInput = null
        lastPlannerResult = null
    }
}
