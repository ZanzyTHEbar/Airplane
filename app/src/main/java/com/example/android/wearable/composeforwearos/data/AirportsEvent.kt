package com.example.android.wearable.composeforwearos.data

/**
 * AirportsEvent is a sealed interface that represents the different events that can be emitted by the UI.
 */
sealed interface AirportsEvent {
    object Refresh : AirportsEvent
    object Loading : AirportsEvent
    object Cancel : AirportsEvent
    object ShowDialog : AirportsEvent
    object HideDialog : AirportsEvent
    data class Delete(val airport: Airport) : AirportsEvent
    data class Error(val message: String) : AirportsEvent
    data class Success(val message: String) : AirportsEvent
    //data class SortAirports(val sortType: SortType) : AirportsEvent
}