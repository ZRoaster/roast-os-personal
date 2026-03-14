package com.roastos.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.MainShellPage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RoastHistoryEngine.initialize(this)

        val restoredMachine = MachineStateStorage.load(this)
        if (restoredMachine != null) {
            RoastStateModel.machine = restoredMachine
        }

        RoastMachineProfileSyncEngine.syncFromBestMatch()
        MachineStateStorage.save(this, RoastStateModel.machine)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F3F2EF"))
        }

        setContentView(root)

        window.statusBarColor = Color.parseColor("#F3F2EF")
        window.navigationBarColor = Color.parseColor("#F3F2EF")

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        MainShellPage.show(this, root)
    }
}
