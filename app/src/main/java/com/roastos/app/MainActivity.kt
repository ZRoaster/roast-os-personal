package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.RoastPage

class MainActivity : Activity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this@MainActivity)
        root.orientation = LinearLayout.VERTICAL

        container = LinearLayout(this@MainActivity)
        container.orientation = LinearLayout.VERTICAL

        val nav = LinearLayout(this@MainActivity)
        nav.orientation = LinearLayout.HORIZONTAL

        val dashBtn = Button(this@MainActivity)
        dashBtn.text = "DASHBOARD"

        val roastBtn = Button(this@MainActivity)
        roastBtn.text = "ROAST"

        val beansBtn = Button(this@MainActivity)
        beansBtn.text = "BEANS"

        val brewBtn = Button(this@MainActivity)
        brewBtn.text = "BREW"

        val aiBtn = Button(this@MainActivity)
        aiBtn.text = "AI"

        nav.addView(dashBtn)
        nav.addView(roastBtn)
        nav.addView(beansBtn)
        nav.addView(brewBtn)
        nav.addView(aiBtn)

        root.addView(container)
        root.addView(nav)

        setContentView(root)

        dashBtn.setOnClickListener {
            DashboardPage.show(this@MainActivity, container)
        }

        roastBtn.setOnClickListener {
            RoastPage.show(this@MainActivity, container)
        }

        beansBtn.setOnClickListener {
            container.removeAllViews()
        }

        brewBtn.setOnClickListener {
            container.removeAllViews()
        }

        aiBtn.setOnClickListener {
            container.removeAllViews()
        }

        DashboardPage.show(this@MainActivity, container)
    }
}
