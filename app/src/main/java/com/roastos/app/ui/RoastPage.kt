package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView

object RoastPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(UiKit.pageSubtitle(context, "Live assist, timeline tracking, and roast control analysis"))
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "LIVE ASSIST",
                LiveAssistPage.buildLiveAssist()
            )
        )

        scroll.addView(root)
        container.addView(scroll)
    }
}
