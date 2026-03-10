package com.roastos.app

import java.util.Timer
import kotlin.concurrent.fixedRateTimer

object MachineBridge {

    private var running = false
    private var timer: Timer? = null

    private var simulatedElapsedSec = 0
    private var simulatedBeanTemp = 30.0
    private var simulatedRor = 8.0

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

    fun resetSimulation() {
        simulatedElapsedSec = 0
        simulatedBeanTemp = 30.0
        simulatedRor = 8.0
    }

    private fun readMachine(): MachineState {
        simulatedElapsedSec += 1

        simulatedRor = when {
            simulatedElapsedSec < 30 -> randomDouble(6.0, 10.0)
            simulatedElapsedSec < 120 -> randomDouble(8.0, 12.0)
            simulatedElapsedSec < 300 -> randomDouble(6.0, 9.0)
            simulatedElapsedSec < 480 -> randomDouble(4.0, 7.0)
            else -> randomDouble(2.0, 5.0)
        }

        simulatedBeanTemp += when {
            simulatedElapsedSec < 60 -> randomDouble(0.6, 1.2)
            simulatedElapsedSec < 180 -> randomDouble(0.8, 1.5)
            simulatedElapsedSec < 360 -> randomDouble(0.5, 1.1)
            simulatedElapsedSec < 540 -> randomDouble(0.3, 0.8)
            else -> randomDouble(0.1, 0.4)
        }

        return MachineState(
            beanTemp = simulatedBeanTemp,
            ror = simulatedRor,
            powerW = 1200,
            airflowPa = 20,
            drumRpm = 55,
            elapsedSec = simulatedElapsedSec,
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
