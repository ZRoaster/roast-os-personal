package com.roastos.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
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

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(16, 16, 16, 16)

        val navBar = LinearLayout(this)
        navBar.orientation = LinearLayout.HORIZONTAL
        navBar.setPadding(0, 0, 0, 16)

        val navParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        dashboardBtn = Button(this).apply {
            text = "Dashboard"
            layoutParams = navParams
        }

        plannerBtn = Button(this).apply {
            text = "Planner"
            layoutParams = navParams
        }

        preheatBtn = Button(this).apply {
            text = "Preheat"
            layoutParams = navParams
        }

        roastBtn = Button(this).apply {
            text = "Roast"
            layoutParams = navParams
        }

        correctionBtn = Button(this).apply {
            text = "Correction"
            layoutParams = navParams
        }

        historyBtn = Button(this).apply {
            text = "History"
            layoutParams = navParams
        }

        profileBtn = Button(this).apply {
            text = "Profile"
            layoutParams = navParams
        }

        pageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(preheatBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)
        navBar.addView(historyBtn)
        navBar.addView(profileBtn)

        root.addView(navBar)
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
                it.setBackgroundColor(Color.parseColor("#222222"))
                it.setTextColor(Color.WHITE)
            } else {
                it.setBackgroundColor(Color.parseColor("#DDDDDD"))
                it.setTextColor(Color.BLACK)
            }
        }
    }
}
