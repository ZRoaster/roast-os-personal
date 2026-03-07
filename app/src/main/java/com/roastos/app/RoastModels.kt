package com.roastos.app

data class PlannerInput(
    val origin: String = "",
    val process: String = "washed",          // washed / honey_washed / natural / honey_natural / anaerobic / fermented
    val moisture: Double = 10.5,
    val density: Double = 840.0,
    val aw: Double = 0.55,
    val beanSize: String = "normal",         // small / normal / large
    val freshness: String = "fresh",
    val purpose: String = "pourover",        // pourover / soe / espresso / american
    val roastLevel: String = "light_medium", // nordic / light / light_medium / medium / medium_dark / dark / french
    val orientation: String = "clean",       // clean / stable / thick
    val mode: String = "M1",                 // M1..M5
    val envTemp: Double = 22.0,
    val envRH: Double = 33.0,
    val batchNum: Int = 1,
    val learnM: Double = 5.50,
    val learnK: Double = 26.0,
    val learnW: Double = 0.65,
    val ttSec: Int,
    val tySec: Int? = null
)

data class PlannerResult(
    val chargeBT: Int,
    val rpm: Int,
    val preheatPa: Int,
    val devPa: Int,
    val fc1: Double,
    val fc2: Double?,
    val fcPredSec: Double,
    val devTime: Int,
    val dropSec: Double,
    val dtrPercent: Double,

    val h1W: Int,
    val h2W: Int,
    val h3W: Int,
    val h4W: Int,
    val h5W: Int,

    val h1Sec: Double,
    val h2Sec: Double,
    val h3Sec: Double,
    val h4Sec: Double,
    val h5Sec: Double,

    val wind1Sec: Double,
    val wind2Sec: Double,
    val protectSec: Double,
    val wind1Pa: Int,
    val wind2Pa: Int,

    val rorFull: List<Double>,
    val rorFull5: List<Double>,
    val rorTargets: List<Double>,
    val rorAnchors: List<Double>,

    val ptLabel: String,
    val awTol: Double,
    val m3Protected: Boolean,
    val m3LowDens: Boolean
)
