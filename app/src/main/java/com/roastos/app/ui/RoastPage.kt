package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastCurveEngine

object RoastPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val curve = RoastCurveEngine.buildFromCurrentState()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(UiKit.pageSubtitle(context, "Live assist, timeline tracking, and roast curve preview"))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "CURVE ENGINE SUMMARY", curve.summary))
        root.addView(UiKit.spacer(context))

        val curveCard = UiKit.card(context)
        curveCard.addView(UiKit.cardTitle(context, "ROAST CURVE"))

        val curveView = RoastCurveView(context)
        curveView.setCurve(curve)
        curveCard.addView(curveView)

        root.addView(curveCard)
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
