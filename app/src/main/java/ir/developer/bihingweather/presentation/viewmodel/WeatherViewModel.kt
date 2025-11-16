package ir.developer.bihingweather.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ir.developer.bihingweather.domain.usecase.CalculateBikingRidingScoreUseCase
import ir.developer.bihingweather.domain.usecase.GetWeatherForecastUseCase

class WeatherViewModel(
    application: Application,
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase,
    private val calculateBikingRidingScoreUseCase: CalculateBikingRidingScoreUseCase
) : AndroidViewModel(application) {

    //location
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _locationPermissionGranted = mutableStateOf(false)
    val locationPermissionGranted: State<Boolean> = _locationPermissionGranted

    fun checkLocationPermission() {
        val context = getApplication<Application>()
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        _locationPermissionGranted.value = hasPermission
        if (hasPermission) {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        fetchWeatherData(it.latitude, it.longitude)
                    }
                }
                .addOnFailureListener {

                }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        TODO("Not yet implemented")
    }
}