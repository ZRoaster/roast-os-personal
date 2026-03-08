package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastCurveEngine
import com.roastos.app.RoastEngine

object RoastPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val curve = RoastCurveEngine.buildFromCurrentState()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(UiKit.pageSubtitle(context, "Live assist, timeline tracking, and roast curve preview"))
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "CURVE ENGINE SUMMARY",
                curve.summary
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "CURVE ANCHORS",
                buildAnchorSummary(curve)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "CURVE POINT PREVIEW",
                buildPointPreview(curve)
            )
        )
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

    private fun buildAnchorSummary(curve: com.roastos.app.RoastCurveResult): String {
        if (curve.anchors.isEmpty()) return "No anchors available"

        return curve.anchors.joinToString("\n") { anchor ->
            val type = if (anchor.isActual) "Actual" else "Pred"
            "$type ${anchor.label}  ${RoastEngine.toMMSS(anchor.timeSec.toDouble())}"
        }
    }

    private fun buildPointPreview(curve: com.roastos.app.RoastCurveResult): String {
        if (curve.points.isEmpty()) return "No curve points available"

        val previewIndexes = listOf(
            0,
            curve.points.size / 6,
            curve.points.size / 3,
            curve.points.size / 2,
            (curve.points.size * 2) / 3,
            (curve.points.size * 5) / 6,
            curve.points.lastIndex
        ).distinct().filter { it in curve.points.indices }

        return previewIndexes.joinToString("\n\n") { index ->
            val p = curve.points[index]
            """
t    ${RoastEngine.toMMSS(p.timeSec.toDouble())}
BT   ${"%.1f".format(p.bt)}
ROR  ${"%.1f".format(p.ror)}
            """.trimIndent()
        }
    }
}
