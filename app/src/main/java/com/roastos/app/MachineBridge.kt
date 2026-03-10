package com.roastos.app

import kotlin.concurrent.fixedRateTimer

object MachineBridge {

    private var running = false

    fun start() {
        if (running) return
        running = true

        fixedRateTimer(
            name = "machine-bridge",
            daemon = true,
            period = 1000
        ) {

            val machine = readMachine()

            MachineState.update(machine)

            val snapshot =
                RoastSessionEngine.update(machine)

            RoastInsightEngine.onSnapshot(snapshot)
        }
    }

    fun stop() {
        running = false
    }

    private fun readMachine(): MachineState {

        // TODO: 未来这里接真实烘焙机
        // 现在先模拟数据

        val elapsed =
            MachineState.current().elapsedSec + 1

        val bt =
            MachineState.current().beanTemp + (0.5..1.5).random()

        val ror =
            (4..12).random().toDouble()

        return MachineState(
            mode = MachineMode.MANUAL,
            connected = true,
            beanTemp = bt,
            ror = ror,
            powerW = 1200,
            airflowPa = 20,
            drumRpm = 55,
            elapsedSec = elapsed
        )
    }

}

private fun ClosedFloatingPointRange<Double>.random() =
    (Math.random() * (endInclusive - start)) + start
