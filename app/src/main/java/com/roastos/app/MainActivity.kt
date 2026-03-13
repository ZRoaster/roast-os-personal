package com.roastos.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.RoastOperatorPage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restoredMachine = MachineStateStorage.load(this)
        if (restoredMachine != null) {
            RoastStateModel.machine = restoredMachine
        }

        RoastMachineProfileSyncEngine.syncFromBestMatch()
        MachineStateStorage.save(this, RoastStateModel.machine)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        setContentView(root)

        RoastOperatorPage.show(this, root)
    }
}
