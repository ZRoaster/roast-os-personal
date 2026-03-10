package com.roastos.app

import android.os.Bundle
import android.content.Context
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.DashboardPage

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动机器桥接模拟
        MachineBridge.start()

        rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        setContentView(rootContainer)

        DashboardPage.show(this as Context, rootContainer)
    }
}
