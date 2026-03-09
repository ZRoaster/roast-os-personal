package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.RoastProfile
import com.roastos.app.RoastProfileEngine
import com.roastos.app.RoastProfilePlannerBridge

object ProfilePage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST PROFILE LIBRARY"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "View saved roast profiles, inspect planner suggestions, delete single profiles, or clear all profiles"
            )
        )
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "PROFILE ACTIONS"))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Profiles"

        val clearBtn = Button(context)
        clearBtn.text = "Clear All Profiles"

        val summaryBody = UiKit.bodyText(context, RoastProfileEngine.summary())

        actionCard.addView(refreshBtn)
        actionCard.addView(clearBtn)
        actionCard.addView(summaryBody)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val listHost = LinearLayout(context)
        listHost.orientation = LinearLayout.VERTICAL
        root.addView(listHost)

        fun renderList() {
            summaryBody.text = RoastProfileEngine.summary()
            listHost.removeAllViews()

            val items = RoastProfileEngine.all()

            if (items.isEmpty()) {
                listHost.addView(
                    UiKit.buildCard(
                        context,
                        "NO PROFILES",
                        "No roast profiles saved yet."
                    )
                )
                return
            }

            items.forEachIndexed { index, profile ->
                listHost.addView(buildProfileCard(context, profile, ::renderList))
                if (index != items.lastIndex) {
                    listHost.addView(UiKit.spacer(context))
                }
            }
        }

        refreshBtn.setOnClickListener {
            renderList()
        }

        clearBtn.setOnClickListener {
            RoastProfileEngine.clear()
            renderList()
        }

        renderList()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildProfileCard(
        context: Context,
        profile: RoastProfile,
        onChanged: () -> Unit
    ): LinearLayout {
        val card = UiKit.card(context)

        val title = TextView(context)
        title.text = profile.name
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)

        val body = TextView(context)
        body.textSize = 15f
        body.text = buildProfileBody(profile)
        body.setPadding(0, UiKit.INNER_GAP, 0, 0)

        val openSuggestionBtn = Button(context)
        openSuggestionBtn.text = "Open Plan Suggestion"

        val deleteBtn = Button(context)
        deleteBtn.text = "Delete This Profile"

        val suggestionBody = TextView(context)
        suggestionBody.textSize = 14f
        suggestionBody.setPadding(0, UiKit.INNER_GAP, 0, 0)
        suggestionBody.text = ""
        suggestionBody.visibility = TextView.GONE

        openSuggestionBtn.setOnClickListener {
            if (suggestionBody.visibility == TextView.GONE) {
                val suggestion = RoastProfilePlannerBridge.buildFromProfile(profile)
                suggestionBody.visibility = TextView.VISIBLE
                suggestionBody.text = suggestion.summary
                openSuggestionBtn.text = "Hide Plan Suggestion"
            } else {
                suggestionBody.visibility = TextView.GONE
                suggestionBody.text = ""
                openSuggestionBtn.text = "Open Plan Suggestion"
            }
        }

        deleteBtn.setOnClickListener {
            RoastProfileEngine.delete(profile.profileId)
            onChanged()
        }

        card.addView(title)
        card.addView(body)
        card.addView(openSuggestionBtn)
        card.addView(deleteBtn)
        card.addView(suggestionBody)

        return card
    }

    private fun buildProfileBody(profile: RoastProfile): String {
        val predicted = """
Predicted
Turning ${profile.predictedTurningSec?.toString() ?: "-"}
Yellow  ${profile.predictedYellowSec?.toString() ?: "-"}
FC      ${profile.predictedFcSec?.toString() ?: "-"}
Drop    ${profile.predictedDropSec?.toString() ?: "-"}
        """.trimIndent()

        val actual = """
Actual
Turning ${profile.actualTurningSec?.toString() ?: "-"}
Yellow  ${profile.actualYellowSec?.toString() ?: "-"}
FC      ${profile.actualFcSec?.toString() ?: "-"}
Drop    ${profile.actualDropSec?.toString() ?: "-"}
ROR     ${profile.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

        return """
Profile ID
${profile.profileId}

Source Batch
${profile.sourceBatchId}

Created
${profile.createdAtMillis}

Replayability
${profile.replayability}

Risk
${profile.risk}

Evaluation
${if (profile.evaluationSaved) "Saved" else "Not saved"}

Bean
${profile.process.ifBlank { "-" }}
Density  ${"%.1f".format(profile.density)}
Moisture ${"%.1f".format(profile.moisture)}
aw       ${"%.2f".format(profile.aw)}

Environment
Temp ${"%.1f".format(profile.envTemp)}℃
RH   ${"%.1f".format(profile.envRh)}%

$predicted

$actual

Note
${profile.note.ifBlank { "-" }}
        """.trimIndent()
    }
}
