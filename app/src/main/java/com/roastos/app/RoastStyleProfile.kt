package com.roastos.app

data class RoastStyleProfile(

    val id: String,

    val name: String,

    val description: String,

    val origin: String,

    val flavorGoal: String,

    val suitableProcess: String?,

    val turningTargetSec: Int?,

    val yellowTargetSec: Int?,

    val firstCrackTargetSec: Int?,

    val dropTargetSec: Int?,

    val developmentRatio: Double?,

    val rorTrend: String?,

    val airflowStrategy: String?,

    val drumStrategy: String?,

    val notes: String?
)
