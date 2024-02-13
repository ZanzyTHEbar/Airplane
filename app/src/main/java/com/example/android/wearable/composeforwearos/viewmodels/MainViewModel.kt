package com.example.android.wearable.composeforwearos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.wearable.composeforwearos.data.Airport
import com.example.android.wearable.composeforwearos.data.AirportDAO
import com.example.android.wearable.composeforwearos.data.AirportsEvent
import com.example.android.wearable.composeforwearos.data.AirportsState
import com.example.android.wearable.composeforwearos.dto.AppCardData
import com.example.android.wearable.composeforwearos.utilities.LocationUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val dao: AirportDAO,
    private val locationUtil: LocationUtil
) : ViewModel() {

    private val _airport = MutableStateFlow(
        Airport(
            id = 0,
            ident = "",
            type = "",
            name = "",
            latitudeDeg = 0.0,
            longitudeDeg = 0.0,
            elevationFt = 0.0,
            continent = "",
            isoCountry = "",
            isoRegion = "",
            municipality = "",
            scheduledService = "",
            gpsCode = "",
            iataCode = "",
            localCode = "",
            homeLink = "",
            wikipediaLink = "",
            keywords = ""
        )
    )

    private val _weather = MutableStateFlow(
        AppCardData(
            location = "",
            temp = 0,
            windDirection = 0,
            windSpeed = 0,
            time = ""
        )
    )

    private val _state = MutableStateFlow(
        AirportsState(

        )
    )

    var state = combine(_state, _airport, _weather) { state, airport, weather ->
        state.copy(nearestAirport = Pair(airport, weather))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AirportsState())

    init {
        updateAirports()
    }

    private fun updateAirports() {

        _state.update {
            it.copy(loading = true)
        }

        viewModelScope.launch {
            try {
                if (!locationUtil.locationDataLoaded.value) {
                    Log.d(TAG, "No location found")
                    return@launch
                }
                if (!locationUtil.nearestAirportLoaded.value) {
                    Log.d(TAG, "No nearest airport found")
                    return@launch
                }

                Log.d(
                    TAG,
                    "Closest Airport: ${locationUtil.nearestAirportData.value.nearestAirport} with a distance of ${locationUtil.nearestAirportData.value.distance} meters"
                )
                _state.update {
                    it.copy(
                        loading = false,
                        distance = locationUtil.nearestAirportData.value.distance,
                        nearestAirport = Pair(
                            locationUtil.nearestAirportData.value.nearestAirport,
                            locationUtil.locationData.value
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, msg = e.message ?: "Error updating airports")
                }
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    fun onEvent(event: AirportsEvent) {
        when (event) {
            is AirportsEvent.Delete -> {
                // Delete the airport from the database
                viewModelScope.launch {
                    dao.delete(event.airport)
                }
            }

            is AirportsEvent.Error -> {
                _state.update {
                    it.copy(loading = false, msg = event.message)
                }
            }

            /*is AirportsEvent.SortAirports -> {
                Log.d(TAG, "Sort airports by ${event.sortType}")
                _sortType.value = event.sortType
            }*/

            is AirportsEvent.Success -> {
                _state.update {
                    it.copy(loading = false, msg = event.message)
                }
            }

            AirportsEvent.HideDialog -> {
                _state.update {
                    it.copy(loading = false)
                }
            }

            AirportsEvent.Loading -> {
                _state.update {
                    it.copy(loading = true)
                }
            }

            AirportsEvent.Refresh -> {
                // Refresh the list of airports
                updateAirports()
            }

            AirportsEvent.ShowDialog -> {
                _state.update {
                    it.copy(loading = true)
                }
            }

            AirportsEvent.Cancel -> {
                _state.update {
                    it.copy(loading = false)
                }
            }
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}
