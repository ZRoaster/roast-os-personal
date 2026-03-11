package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MyStyleEngine
import com.roastos.app.RoastStyleProfile
import com.roastos.app.UiKit

object MyStylePage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "MY STYLES"))
        root.addView(UiKit.pageSubtitle(context, "Custom roast style library"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                MyStyleEngine.summary()
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val learningCard = UiKit.card(context)
        val learningPanel = RoastStyleLearningPanel(context)

        learningCard.addView(UiKit.cardTitle(context, "STYLE LEARNING"))
        learningCard.addView(learningPanel)

        root.addView(learningCard)
        root.addView(UiKit.spacer(context))

        val styles = MyStyleEngine.all()

        if (styles.isEmpty()) {

            val emptyCard = UiKit.card(context)

            emptyCard.addView(UiKit.cardTitle(context, "NO CUSTOM STYLES"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No saved custom roast styles yet."
                )
            )

            root.addView(emptyCard)

        } else {

            styles.forEachIndexed { index, style ->

                val card = UiKit.card(context)

                card.addView(
                    UiKit.cardTitle(
                        context,
                        "STYLE ${index + 1}"
                    )
                )

                card.addView(
                    UiKit.bodyText(
                        context,
                        buildStyleText(style)
                    )
                )

                root.addView(card)

                if (index != styles.lastIndex) {
                    root.addView(UiKit.spacer(context))
                }
            }
        }

        backBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildStyleText(
        style: RoastStyleProfile
    ): String {

        return """
Name
${style.name}

Origin
${style.origin}

Description
${style.description}

Flavor Goal
${style.flavorGoal}

Suitable Process
${style.suitableProcess ?: "-"}

Turning
${style.turningTargetSec?.let { formatSec(it) } ?: "-"}

Yellow
${style.yellowTargetSec?.let { formatSec(it) } ?: "-"}

First Crack
${style.firstCrackTargetSec?.let { formatSec(it) } ?: "-"}

Drop
${style.dropTargetSec?.let { formatSec(it) } ?: "-"}

Development
${style.developmentRatio?.let { "${String.format("%.1f", it * 100)}%" } ?: "-"}

RoR Trend
${style.rorTrend ?: "-"}

Airflow Strategy
${style.airflowStrategy ?: "-"}

Drum Strategy
${style.drumStrategy ?: "-"}

Notes
${style.notes ?: "-"}
        """.trimIndent()
    }

    private fun formatSec(sec: Int): String {

        val m = sec / 60
        val s = sec % 60

        return "%d:%02d".format(m, s)
    }
}
