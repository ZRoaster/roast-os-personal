package com.roastos.app

import kotlin.concurrent.fixedRateTimer
import java.util.Timer

object MachineBridge {

    private var running = false
    private var timer: Timer? = null

    fun start() {
        if (running) return

        running = true

        timer = fixedRateTimer(
            name = "machine-bridge",
            daemon = true,
            initialDelay = 0L,
            period = 1000L
        ) {

            if (!running) {
                cancel()
                return@fixedRateTimer
            }

            val machine = readMachine()

            MachineState.update(machine)

            RoastSessionEngine.update(machine)
        }
    }

    fun stop() {
        running = false
        timer?.cancel()
        timer = null
    }

    fun isRunning(): Boolean {
        return running
    }

    private fun readMachine(): MachineState {

        val current = MachineState.current()

        val elapsed = current.elapsedSec + 1

        val nextBt =
            if (current.beanTemp <= 0.0)
                30.0
            else
                current.beanTemp + randomDouble(0.5, 1.5)

        val nextRor =
            randomDouble(4.0, 12.0)

        return MachineState(
            beanTemp = nextBt,
            ror = nextRor,
            powerW = 1200,
            airflowPa = 20,
            drumRpm = 55,
            elapsedSec = elapsed,
            environmentTemp = 25.0,
            environmentHumidity = 40.0
        )
    }

    private fun randomDouble(
        min: Double,
        max: Double
    ): Double {
        return min + Math.random() * (max - min)
    }
}
