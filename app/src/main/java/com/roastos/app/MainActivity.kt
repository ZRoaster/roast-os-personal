package com.roastos.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.DashboardPage

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动 RoastOS 机器桥接
        MachineBridge.start()

        rootContainer = LinearLayout(this)
        rootContainer.orientation = LinearLayout.VERTICAL

        setContentView(rootContainer)

        DashboardPage.show(this, rootContainer)
    }
}
