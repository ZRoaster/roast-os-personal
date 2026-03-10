package com.roastos.app

import java.util.Timer
import kotlin.concurrent.fixedRateTimer

object MachineBridge {

    private var running = false
    private var timer: Timer? = null

    private var simulatedElapsed = 0
    private var simulatedBT = 30.0
    private var simulatedROR = 8.0

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

            // 目前只驱动 SessionEngine
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

        simulatedElapsed += 1

        simulatedROR = randomDouble(4.0, 10.0)

        simulatedBT += randomDouble(0.4, 1.2)

        return MachineState(
            beanTemp = simulatedBT,
            ror = simulatedROR,
            powerW = 1200,
            airflowPa = 20,
            drumRpm = 55,
            elapsedSec = simulatedElapsed,
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
