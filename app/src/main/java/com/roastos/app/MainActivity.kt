package com.roastos.app

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.DashboardPage

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MachineBridge.start()

        rootContainer = LinearLayout(this)
        rootContainer.orientation = LinearLayout.VERTICAL

        setContentView(rootContainer as android.view.View)

        DashboardPage.show(this as Context, rootContainer)
    }
}
