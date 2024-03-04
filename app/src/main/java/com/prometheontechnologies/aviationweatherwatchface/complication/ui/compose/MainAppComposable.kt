package com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.Destinations
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.MainViewModel
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.theme.AviationWeatherWatchFaceTheme

@OptIn(
    ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun MainAppEntry(
    context: Context,
    viewModel: MainViewModel
) {
    AviationWeatherWatchFaceTheme {

        val navController = rememberSwipeDismissableNavController()

        AppScaffold {
            SwipeDismissableNavHost(
                startDestination = Destinations.LocationPerms,
                navController = navController
            ) {
                composable(
                    Destinations.NotifactionPerms,

                    ) {
                    val columnState =
                        rememberColumnState(
                            ScalingLazyColumnDefaults.responsive(
                                firstItemIsFullWidth = false,
                                verticalArrangement = Arrangement.Bottom,
                            )
                        )
                    ScreenScaffold(scrollState = columnState) {
                        ScalingLazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            columnState = columnState
                        ) {
                            item {
                                val notificationsPermissionState = rememberPermissionState(
                                    permission = Manifest.permission.POST_NOTIFICATIONS
                                )
                                PermissionsScreen(
                                    permissionState = notificationsPermissionState,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                composable(
                    Destinations.LocationPerms,
                ) {
                    val columnState =
                        rememberColumnState(
                            ScalingLazyColumnDefaults.responsive(
                                firstItemIsFullWidth = false
                            )
                        )
                    ScreenScaffold(scrollState = columnState) {
                        ScalingLazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            columnState = columnState
                        ) {
                            item {
                                val locationPermissionsState = rememberMultiplePermissionsState(
                                    permissions = listOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                PermissionsScreen(
                                    permissionState = locationPermissionsState,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                composable(
                    Destinations.SettingsScreen
                ) {
                    // TODO: Setup snack-bar to show the user that the location service is being started

                    // Start the location service
                    viewModel.handleLocationService(true)

                    // Start the work manager
                    viewModel.scheduleWeatherUpdates(context)

                    val columnState =
                        rememberColumnState(
                            ScalingLazyColumnDefaults.responsive(
                                firstItemIsFullWidth = false
                            )
                        )
                    ScreenScaffold(scrollState = columnState) {
                        SettingsScreen(
                            context = context,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }

                composable(
                    Destinations.TimePicker
                ) {
                    val columnState =
                        rememberColumnState(
                            ScalingLazyColumnDefaults.responsive(
                                firstItemIsFullWidth = false
                            )
                        )
                    ScreenScaffold(scrollState = columnState) {
                        TimePickerScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}