package com.roastos.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.roastos.app.ui.CorrectionPage
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.HistoryPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.PreheatPage
import com.roastos.app.ui.ProfilePage
import com.roastos.app.ui.RoastPage

class MainActivity : Activity() {

    private lateinit var pageContainer: LinearLayout

    private lateinit var dashboardBtn: Button
    private lateinit var plannerBtn: Button
    private lateinit var preheatBtn: Button
    private lateinit var roastBtn: Button
    private lateinit var correctionBtn: Button
    private lateinit var historyBtn: Button
    private lateinit var profileBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        val navScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, 0, 0, 18)
        }

        val navBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        dashboardBtn = buildNavButton("Dashboard")
        plannerBtn = buildNavButton("Planner")
        preheatBtn = buildNavButton("Preheat")
        roastBtn = buildNavButton("Roast")
        correctionBtn = buildNavButton("Correction")
        historyBtn = buildNavButton("History")
        profileBtn = buildNavButton("Profile")

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(preheatBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)
        navBar.addView(historyBtn)
        navBar.addView(profileBtn)

        navScroll.addView(navBar)

        pageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(navScroll)
        root.addView(pageContainer)

        setContentView(root)

        dashboardBtn.setOnClickListener {
            setSelected(dashboardBtn)
            DashboardPage.show(this, pageContainer)
        }

        plannerBtn.setOnClickListener {
            setSelected(plannerBtn)
            PlannerPage.show(this, pageContainer)
        }

        preheatBtn.setOnClickListener {
            setSelected(preheatBtn)
            PreheatPage.show(this, pageContainer)
        }

        roastBtn.setOnClickListener {
            setSelected(roastBtn)
            RoastPage.show(this, pageContainer)
        }

        correctionBtn.setOnClickListener {
            setSelected(correctionBtn)
            CorrectionPage.show(this, pageContainer)
        }

        historyBtn.setOnClickListener {
            setSelected(historyBtn)
            HistoryPage.show(this, pageContainer)
        }

        profileBtn.setOnClickListener {
            setSelected(profileBtn)
            ProfilePage.show(this, pageContainer)
        }

        setSelected(dashboardBtn)
        DashboardPage.show(this, pageContainer)
    }

    private fun buildNavButton(label: String): Button {
        return Button(this).apply {
            text = label
            setAllCaps(false)
            textSize = 14f
            setPadding(26, 16, 26, 16)
            minWidth = 0
            minimumWidth = 0
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E6E6E6"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }
        }
    }

    private fun setSelected(btn: Button) {
        val buttons = listOf(
            dashboardBtn,
            plannerBtn,
            preheatBtn,
            roastBtn,
            correctionBtn,
            historyBtn,
            profileBtn
        )

        buttons.forEach {
            if (it == btn) {
                it.setBackgroundColor(Color.parseColor("#202124"))
                it.setTextColor(Color.WHITE)
                it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            } else {
                it.setBackgroundColor(Color.parseColor("#E6E6E6"))
                it.setTextColor(Color.parseColor("#202124"))
                it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
        }
    }
}
