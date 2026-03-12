package com.roastos.app

object EnvironmentProfileEngine {

    private var currentProfile: EnvironmentProfile? = null

    fun current(): EnvironmentProfile {
        return currentProfile ?: defaultProfile().also {
            currentProfile = it
        }
    }

    fun peek(): EnvironmentProfile? {
        return currentProfile
    }

    fun save(profile: EnvironmentProfile) {
        currentProfile = profile
    }

    fun reset() {
        currentProfile = defaultProfile()
    }

    fun defaultProfile(): EnvironmentProfile {
        return EnvironmentProfile(
            altitudeMeters = null,
            ambientTempC = 25.0,
            ambientHumidityRh = 50.0,
            barometricPressureHpa = null,
            note = "Default environment profile"
        )
    }

    fun updateTodayEnvironment(
        ambientTempC: Double?,
        ambientHumidityRh: Double?,
        altitudeMeters: Int? = null,
        barometricPressureHpa: Double? = null,
        note: String? = null
    ): EnvironmentProfile {
        val base = current()

        val updated = base.copy(
            altitudeMeters = altitudeMeters ?: base.altitudeMeters,
            ambientTempC = ambientTempC ?: base.ambientTempC,
            ambientHumidityRh = ambientHumidityRh ?: base.ambientHumidityRh,
            barometricPressureHpa = barometricPressureHpa ?: base.barometricPressureHpa,
            note = note ?: base.note
        )

        currentProfile = updated
        return updated
    }

    fun summary(): String {
        val profile = current()

        return """
Environment

Altitude
${profile.altitudeMeters ?: "-"}

Ambient Temp
${profile.ambientTempC ?: "-"}

Humidity
${profile.ambientHumidityRh ?: "-"}

Pressure
${profile.barometricPressureHpa ?: "-"}

Note
${profile.note ?: "-"}
        """.trimIndent()
    }
}
