package com.roastos.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object RoastHistoryStorage {

    private const val PREFS_NAME = "roast_os_history"
    private const val KEY_HISTORY_JSON = "history_json"

    fun save(
        context: Context,
        entries: List<RoastHistoryEntry>
    ) {
        val array = JSONArray()

        entries
            .sortedByDescending { it.createdAtMillis }
            .forEach { entry ->
                array.put(entryToJson(entry))
            }

        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY_JSON, array.toString())
            .apply()
    }

    fun load(
        context: Context
    ): List<RoastHistoryEntry> {
        val raw = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY_JSON, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    jsonToEntry(item)?.let { add(it) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(
        context: Context
    ) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY_JSON)
            .apply()
    }

    private fun entryToJson(
        entry: RoastHistoryEntry
    ): JSONObject {
        return JSONObject().apply {
            put("batchId", entry.batchId)
            put("createdAtMillis", entry.createdAtMillis)
            put("title", entry.title)

            put("process", entry.process)
            put("density", entry.density)
            put("moisture", entry.moisture)
            put("aw", entry.aw)

            put("envTemp", entry.envTemp)
            put("envRh", entry.envRh)

            putNullable("predictedTurningSec", entry.predictedTurningSec)
            putNullable("predictedYellowSec", entry.predictedYellowSec)
            putNullable("predictedFcSec", entry.predictedFcSec)
            putNullable("predictedDropSec", entry.predictedDropSec)

            putNullable("actualTurningSec", entry.actualTurningSec)
            putNullable("actualYellowSec", entry.actualYellowSec)
            putNullable("actualFcSec", entry.actualFcSec)
            putNullable("actualDropSec", entry.actualDropSec)
            putNullable("actualPreFcRor", entry.actualPreFcRor)

            put("batchStatus", entry.batchStatus)
            put("reportText", entry.reportText)
            put("diagnosisText", entry.diagnosisText)
            put("correctionText", entry.correctionText)

            putNullable("baselineSource", entry.baselineSource)
            putNullable("baselineLabel", entry.baselineLabel)
            putNullable("baselineMatchGrade", entry.baselineMatchGrade)
            putNullable("baselineSourceProfileId", entry.baselineSourceProfileId)
            putNullable("baselineSourceBatchId", entry.baselineSourceBatchId)

            put("roastHealthHeadline", entry.roastHealthHeadline)
            put("roastHealthDetail", entry.roastHealthDetail)

            if (entry.evaluation == null) {
                put("evaluation", JSONObject.NULL)
            } else {
                put("evaluation", evaluationToJson(entry.evaluation))
            }
        }
    }

    private fun jsonToEntry(
        json: JSONObject
    ): RoastHistoryEntry? {
        return try {
            RoastHistoryEntry(
                batchId = json.optString("batchId", ""),
                createdAtMillis = json.optLong("createdAtMillis", 0L),
                title = json.optString("title", ""),

                process = json.optString("process", ""),
                density = json.optDouble("density", 0.0),
                moisture = json.optDouble("moisture", 0.0),
                aw = json.optDouble("aw", 0.0),

                envTemp = json.optDouble("envTemp", 0.0),
                envRh = json.optDouble("envRh", 0.0),

                predictedTurningSec = json.optNullableInt("predictedTurningSec"),
                predictedYellowSec = json.optNullableInt("predictedYellowSec"),
                predictedFcSec = json.optNullableInt("predictedFcSec"),
                predictedDropSec = json.optNullableInt("predictedDropSec"),

                actualTurningSec = json.optNullableInt("actualTurningSec"),
                actualYellowSec = json.optNullableInt("actualYellowSec"),
                actualFcSec = json.optNullableInt("actualFcSec"),
                actualDropSec = json.optNullableInt("actualDropSec"),
                actualPreFcRor = json.optNullableDouble("actualPreFcRor"),

                batchStatus = json.optString("batchStatus", ""),
                reportText = json.optString("reportText", ""),
                diagnosisText = json.optString("diagnosisText", ""),
                correctionText = json.optString("correctionText", ""),
                evaluation = json.optJSONObject("evaluation")?.let { jsonToEvaluation(it) },

                baselineSource = json.optNullableString("baselineSource"),
                baselineLabel = json.optNullableString("baselineLabel"),
                baselineMatchGrade = json.optNullableString("baselineMatchGrade"),
                baselineSourceProfileId = json.optNullableString("baselineSourceProfileId"),
                baselineSourceBatchId = json.optNullableString("baselineSourceBatchId"),

                roastHealthHeadline = json.optString("roastHealthHeadline", "稳定"),
                roastHealthDetail = json.optString("roastHealthDetail", "当前未检测到明显风险。")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun evaluationToJson(
        evaluation: RoastEvaluation
    ): JSONObject {
        return JSONObject().apply {
            putNullable("beanColor", evaluation.beanColor)
            putNullable("groundColor", evaluation.groundColor)
            putNullable("roastedAw", evaluation.roastedAw)
            putNullable("sweetness", evaluation.sweetness)
            putNullable("acidity", evaluation.acidity)
            putNullable("body", evaluation.body)
            putNullable("flavorClarity", evaluation.flavorClarity)
            putNullable("balance", evaluation.balance)
            put("notes", evaluation.notes)
        }
    }

    private fun jsonToEvaluation(
        json: JSONObject
    ): RoastEvaluation {
        return RoastEvaluation(
            beanColor = json.optNullableDouble("beanColor"),
            groundColor = json.optNullableDouble("groundColor"),
            roastedAw = json.optNullableDouble("roastedAw"),
            sweetness = json.optNullableInt("sweetness"),
            acidity = json.optNullableInt("acidity"),
            body = json.optNullableInt("body"),
            flavorClarity = json.optNullableInt("flavorClarity"),
            balance = json.optNullableInt("balance"),
            notes = json.optString("notes", "")
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optNullableString(
        key: String
    ): String? {
        return if (isNull(key)) null else optString(key, "")
    }

    private fun JSONObject.optNullableInt(
        key: String
    ): Int? {
        return if (isNull(key) || !has(key)) null else optInt(key)
    }

    private fun JSONObject.optNullableDouble(
        key: String
    ): Double? {
        return if (isNull(key) || !has(key)) null else optDouble(key)
    }
}
