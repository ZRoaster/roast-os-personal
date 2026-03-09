package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.AppState
import com.roastos.app.RoastEngine
import com.roastos.app.RoastProfileEngine
import com.roastos.app.RoastProfilePlanSuggestion
import com.roastos.app.RoastProfilePlannerBridge

object PlannerPage {

    private var appliedSuggestion: RoastProfilePlanSuggestion? = null

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "PLANNER CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Planner view with profile suggestion entry and apply action"
            )
        )
        root.addView(UiKit.spacer(context))

        val profileCard = UiKit.card(context)
        profileCard.addView(UiKit.cardTitle(context, "PROFILE SUGGESTION"))

        val refreshSuggestionBtn = Button(context)
        refreshSuggestionBtn.text = "Refresh Suggestion"

        val applySuggestionBtn = Button(context)
        applySuggestionBtn.text = "Apply Latest Profile Suggestion"

        val clearAppliedBtn = Button(context)
        clearAppliedBtn.text = "Clear Applied Suggestion"

        val profileSuggestionBody = UiKit.bodyText(context, "")

        profileCard.addView(refreshSuggestionBtn)
        profileCard.addView(applySuggestionBtn)
        profileCard.addView(clearAppliedBtn)
        profileCard.addView(profileSuggestionBody)

        root.addView(profileCard)
        root.addView(UiKit.spacer(context))

        val plannerViewCard = UiKit.card(context)
        plannerViewCard.addView(UiKit.cardTitle(context, "PLANNER VIEW"))
        val plannerViewBody = UiKit.bodyText(context, "")
        plannerViewCard.addView(plannerViewBody)
        root.addView(plannerViewCard)
        root.addView(UiKit.spacer(context))

        val plannerInputCard = UiKit.card(context)
        plannerInputCard.addView(UiKit.cardTitle(context, "CURRENT PLANNER INPUT"))
        val plannerInputBody = UiKit.bodyText(context, "")
        plannerInputCard.addView(plannerInputBody)
        root.addView(plannerInputCard)
        root.addView(UiKit.spacer(context))

        val plannerResultCard = UiKit.card(context)
        plannerResultCard.addView(UiKit.cardTitle(context, "CURRENT RAW PLANNER RESULT"))
        val plannerResultBody = UiKit.bodyText(context, "")
        plannerResultCard.addView(plannerResultBody)
        root.addView(plannerResultCard)

        fun latestSuggestionOrNull(): RoastProfilePlanSuggestion? {
            val latestProfile = RoastProfileEngine.latest() ?: return null
            return RoastProfilePlannerBridge.buildFromProfile(latestProfile)
        }

        fun buildProfileSuggestionText(): String {
            val latestProfile = RoastProfileEngine.latest()
            val latestSuggestion = latestSuggestionOrNull()

            if (latestProfile == null || latestSuggestion == null) {
                return """
Status
No profile suggestion available

Next Step
Save a good batch as Profile first
Then open Profile → Plan Suggestion
Then return here and apply it
                """.trimIndent()
            }

            val appliedText = if (appliedSuggestion?.profileId == latestSuggestion.profileId) {
                "Applied"
            } else {
                "Not applied"
            }

            return """
Latest Profile
${latestProfile.name}

Profile ID
${latestProfile.profileId}

Source Batch
${latestProfile.sourceBatchId}

Applied
$appliedText

${latestSuggestion.summary}
            """.trimIndent()
        }

        fun buildPlannerViewText(): String {
            val suggestion = appliedSuggestion
            if (suggestion != null) {
                return """
Planner Source
Applied Profile Suggestion

Profile
${suggestion.profileName}

Profile ID
${suggestion.profileId}

Source Batch
${suggestion.sourceBatchId}

Suggested Anchors
Turning   ${suggestion.suggestedTurningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${suggestion.suggestedYellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${suggestion.suggestedFcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${suggestion.suggestedDropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Suggested Development
Dev       ${suggestion.suggestedDevSec?.let { "${it}s" } ?: "-"}
DTR       ${suggestion.suggestedDtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}

Replayability
${suggestion.replayability}

Risk
${suggestion.risk}
                """.trimIndent()
            }

            val planner = AppState.lastPlannerResult
            if (planner == null) {
                return """
Planner Source
No applied suggestion
No planner result available

Next Step
Run your normal planner flow
or apply latest profile suggestion
                """.trimIndent()
            }

            val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val predYellow = planner.h2Sec.toInt()
            val predFc = planner.fcPredSec.toInt()
            val predDrop = planner.dropSec.toInt()

            return """
Planner Source
Current Planner Result

Charge
${planner.chargeBT}℃

Predicted Anchors
Turning   ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow    ${RoastEngine.toMMSS(predYellow.toDouble())}
FC        ${RoastEngine.toMMSS(predFc.toDouble())}
Drop      ${RoastEngine.toMMSS(predDrop.toDouble())}

Development
Dev       ${planner.devTime}s
DTR       ${"%.1f".format(planner.dtrPercent)}%
            """.trimIndent()
        }

        fun buildPlannerInputText(): String {
            val input = AppState.lastPlannerInput ?: return """
No planner input available
            """.trimIndent()

            return """
Process
${input.process}

Bean
Density    ${"%.1f".format(input.density)}
Moisture   ${"%.1f".format(input.moisture)}
aw         ${"%.2f".format(input.aw)}

Environment
Temp       ${"%.1f".format(input.envTemp)}℃
RH         ${"%.1f".format(input.envRH)}%

Intent
Roast      ${input.roastLevel}
Direction  ${input.orientation}
Mode       ${input.mode}
Batch No   ${input.batchNum}
            """.trimIndent()
        }

        fun buildRawPlannerResultText(): String {
            val planner = AppState.lastPlannerResult ?: return """
No planner result available
            """.trimIndent()

            val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val predYellow = planner.h2Sec.toInt()
            val predFc = planner.fcPredSec.toInt()
            val predDrop = planner.dropSec.toInt()

            return """
Charge
${planner.chargeBT}℃

Predicted Anchors
Turning   ${predTurning}s
Yellow    ${predYellow}s
FC        ${predFc}s
Drop      ${predDrop}s

Development
Dev       ${planner.devTime}s
DTR       ${"%.1f".format(planner.dtrPercent)}%
            """.trimIndent()
        }

        fun refreshAll() {
            profileSuggestionBody.text = buildProfileSuggestionText()
            plannerViewBody.text = buildPlannerViewText()
            plannerInputBody.text = buildPlannerInputText()
            plannerResultBody.text = buildRawPlannerResultText()
        }

        refreshSuggestionBtn.setOnClickListener {
            refreshAll()
        }

        applySuggestionBtn.setOnClickListener {
            appliedSuggestion = latestSuggestionOrNull()
            refreshAll()
        }

        clearAppliedBtn.setOnClickListener {
            appliedSuggestion = null
            refreshAll()
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }
}
