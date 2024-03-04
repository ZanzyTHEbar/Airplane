package com.prometheontechnologies.aviationweatherwatchface.complication.data.dto

import kotlinx.coroutines.flow.StateFlow

interface ServicesInterface {
    companion object {
        enum class ActionType {
            START,
            ACTIVE,
            PAUSE,
            RESUME,
            STOP,
            NONE
        }

        data class ServiceState(
            var action: ActionType,
            var isPaused: Boolean,
            var isRunning: Boolean
        )

    }

    var serviceState: StateFlow<ServiceState>?

    fun <T> setSettings(newSettings: T)

    fun start()

    fun pause()

    fun resume()

    fun stop()

    fun restart()

}