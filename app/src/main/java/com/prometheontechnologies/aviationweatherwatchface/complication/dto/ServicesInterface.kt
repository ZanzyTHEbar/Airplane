package com.prometheontechnologies.aviationweatherwatchface.complication.dto

interface ServicesInterface {
    companion object {
        enum class ActionType {
            START,
            STOP
        }
    }

    fun start()
    fun stop()
}