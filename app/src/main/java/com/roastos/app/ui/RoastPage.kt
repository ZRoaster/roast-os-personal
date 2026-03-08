package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastCurveEngine

object RoastPage {

    private var autoRefreshEnabled = false
    private var autoRefreshRunnable: Runnable? = null

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(UiKit.pageSubtitle(context, "Live assist, timeline tracking, roast curve, and actual input"))
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "ACTIONS"))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Curve"

        val autoRefreshBtn = Button(context)
        autoRefreshBtn.text = "Auto Refresh OFF"

        actionCard.addView(refreshBtn)
        actionCard.addView(autoRefreshBtn)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "CURVE ENGINE SUMMARY"))
        val summaryBody = UiKit.bodyText(context, "")
        summaryCard.addView(summaryBody)

        val curveCard = UiKit.card(context)
        curveCard.addView(UiKit.cardTitle(context, "ROAST CURVE"))
        val curveView = RoastCurveView(context)
        curveCard.addView(curveView)

        val liveAssistCard = UiKit.card(context)
        liveAssistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))
        val liveAssistBody = UiKit.bodyText(context, "")
        liveAssistCard.addView(liveAssistBody)

        fun refreshAll() {
            val curve = RoastCurveEngine.buildFromCurrentState()
            summaryBody.text = curve.summary
            curveView.setCurve(curve)
            liveAssistBody.text = LiveAssistPage.buildLiveAssist()
        }

        LiveAssistPage.attachLiveInputPanel(
            context = context,
            parent = root,
            onDataChanged = {
                refreshAll()
            }
        )
        root.addView(UiKit.spacer(context))

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))
        root.addView(curveCard)
        root.addView(UiKit.spacer(context))
        root.addView(liveAssistCard)

        fun stopAutoRefresh() {
            autoRefreshRunnable?.let { root.removeCallbacks(it) }
            autoRefreshRunnable = null
            autoRefreshEnabled = false
            autoRefreshBtn.text = "Auto Refresh OFF"
        }

        fun startAutoRefresh() {
            stopAutoRefresh()
            autoRefreshEnabled = true
            autoRefreshBtn.text = "Auto Refresh ON"

            val runnable = object : Runnable {
                override fun run() {
                    if (!autoRefreshEnabled) return
                    refreshAll()
                    root.postDelayed(this, 2000)
                }
            }

            autoRefreshRunnable = runnable
            root.post(runnable)
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        autoRefreshBtn.setOnClickListener {
            if (autoRefreshEnabled) {
                stopAutoRefresh()
            } else {
                startAutoRefresh()
            }
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }
}
