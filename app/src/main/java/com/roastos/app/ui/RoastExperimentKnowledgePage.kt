package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastExperimentKnowledgeEngine
import com.roastos.app.UiKit

object RoastExperimentKnowledgePage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "EXPERIMENT KNOWLEDGE"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "Latest experiment knowledge"
            )
        )

        root.addView(UiKit.spacer(context))

        val backBtn =
            UiKit.secondaryButton(context, "BACK TO STUDIO")

        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val card = UiKit.card(context)

        card.addView(
            UiKit.cardTitle(
                context,
                "LATEST KNOWLEDGE"
            )
        )

        card.addView(
            UiKit.bodyText(
                context,
                RoastExperimentKnowledgeEngine.latestText()
            )
        )

        root.addView(card)

        backBtn.setOnClickListener {

            RoastStudioPage.show(context, container)

        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
