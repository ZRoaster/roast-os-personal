package com.roastos.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.RoastPage
import com.roastos.app.ui.CorrectionPage

class MainActivity : ComponentActivity() {

    lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL

        val btnDashboard = Button(this)
        btnDashboard.text = "Dashboard"

        val btnPlanner = Button(this)
        btnPlanner.text = "Planner"

        val btnRoast = Button(this)
        btnRoast.text = "Roast"

        val btnCorrection = Button(this)
        btnCorrection.text = "Correction"

        container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        nav.addView(btnDashboard)
        nav.addView(btnPlanner)
        nav.addView(btnRoast)
        nav.addView(btnCorrection)

        root.addView(nav)
        root.addView(container)

        setContentView(root)

        btnDashboard.setOnClickListener {
            DashboardPage.show(this@MainActivity, container)
        }

        btnPlanner.setOnClickListener {
            PlannerPage.show(this@MainActivity, container)
        }

        btnRoast.setOnClickListener {
            RoastPage.show(this@MainActivity, container)
        }

        btnCorrection.setOnClickListener {
            CorrectionPage.show(this@MainActivity, container)
        }

        // 默认页面
        DashboardPage.show(this@MainActivity, container)
    }
}
