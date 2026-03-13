package com.roastos.app

import android.content.Context
import org.json.JSONObject

object MachineStateStorage {

    private const val PREFS_NAME = "roast_os_machine_state"
    private const val KEY_MACHINE_STATE = "machine_state_json"

    fun save(
        context: Context,
        state: RoastStateModel.MachineState
    ) {
        val json = JSONObject().apply {
            put("thermalMass", state.thermalMass)
            put("drumMass", state.drumMass)
            put("heatRetention", state.heatRetention)

            put("maxPowerW", state.maxPowerW)
            put("maxAirPa", state.maxAirPa)
            put("maxRpm", state.maxRpm)

            put("powerResponseDelay", state.powerResponseDelay)
            put("airflowResponseDelay", state.airflowResponseDelay)
            put("rpmResponseDelay", state.rpmResponseDelay)
        }

        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MACHINE_STATE, json.toString())
            .apply()
    }

    fun load(
        context: Context
    ): RoastStateModel.MachineState? {
        val raw = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MACHINE_STATE, null)
            ?: return null

        return try {
            val json = JSONObject(raw)

            RoastStateModel.MachineState(
                thermalMass = json.optDouble("thermalMass", 1.0),
                drumMass = json.optDouble("drumMass", 1.0),
                heatRetention = json.optDouble("heatRetention", 1.0),

                maxPowerW = json.optInt("maxPowerW", 1450),
                maxAirPa = json.optInt("maxAirPa", 30),
                maxRpm = json.optInt("maxRpm", 80),

                powerResponseDelay = json.optDouble("powerResponseDelay", 6.0),
                airflowResponseDelay = json.optDouble("airflowResponseDelay", 3.0),
                rpmResponseDelay = json.optDouble("rpmResponseDelay", 2.0)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clear(
        context: Context
    ) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_MACHINE_STATE)
            .apply()
    }
}
