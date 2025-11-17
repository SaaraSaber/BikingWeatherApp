package ir.developer.bihingweather.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ir.developer.bihingweather.data.remote.Config
import ir.developer.bihingweather.domain.model.BikeRidingScore
import ir.developer.bihingweather.domain.model.DailyForecast
import ir.developer.bihingweather.domain.model.Temperature
import ir.developer.bihingweather.domain.model.WeatherResponse
import ir.developer.bihingweather.domain.usecase.CalculateBikingRidingScoreUseCase
import ir.developer.bihingweather.domain.usecase.GetWeatherForecastUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    //weather
    private val _weatherState = mutableStateOf(WeatherState())
    val weatherState: State<WeatherState> = _weatherState

    //scores
    private val _dailyScores =
        mutableStateOf<List<Pair<DailyForecast, BikeRidingScore>>>(emptyList())
    val dailyScores: State<List<Pair<DailyForecast, BikeRidingScore>>> = _dailyScores

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
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch location: ${it.message}"
                    )
                }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        _weatherState.value = _weatherState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            getWeatherForecastUseCase(latitude, longitude)
                .onSuccess { response ->
                    val dailyForecasts = processForecastIntoDaily(response)
                    val score = dailyForecasts.map { forecast ->
                        forecast to calculateBikingRidingScoreUseCase(forecast)
                    }
                    _dailyScores.value = score
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        weatherData = response.copy(daily = dailyForecasts),
                        error = null
                    )
                }
                .onFailure {
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch weather data: ${it.message}"
                    )
                    _dailyScores.value = emptyList()
                }
        }
    }

    private fun processForecastIntoDaily(response: WeatherResponse): List<DailyForecast> {
        val allDailyForecasts = mutableListOf<DailyForecast>()
        val dataFormat = SimpleDateFormat("yyy-MM-dd", Locale.getDefault())

        //Group weather items by data
        val dailyGroups = response.list.groupBy { item ->
            dataFormat.format(Date(item.date * 1000))
        }

        dailyGroups.values.forEach { dailyForecast ->
            if (dailyForecast.isNotEmpty()) {
                val firstForecast = dailyForecast.first()
                val maxTemp = dailyForecast.maxOf { it.main.tempMax }
                val minTemp = dailyForecast.minOf { it.main.tempMin }
                val avgHumidity = dailyForecast.map { it.main.humidity }.average().toInt()
                val avgWindSpeed = dailyForecast.map { it.wind.speed }.average()
                val avgPrecipitation = dailyForecast.map { it.precipitationProbability }.average()

                //get the most common weather for the day
                val mostCommonWeather = dailyForecast
                    .flatMap { it.weather }
                    .groupBy { it.id }
                    .maxByOrNull { it.value.size }
                    ?.value?.first() ?: firstForecast.weather.first()

                val dailyForecast = DailyForecast(
                    date = firstForecast.date,
                    temperature = Temperature(
                        day = firstForecast.main.temp,
                        min = minTemp,
                        max = maxTemp,
                        night = firstForecast.main.temp
                    ),
                    weather = listOf(mostCommonWeather),
                    humidity = avgHumidity,
                    windSpeed = avgWindSpeed,
                    precipitationProbability = avgPrecipitation
                )
                allDailyForecasts.add(dailyForecast)
            }
        }
        return allDailyForecasts.take(6) // Return up to 6 days
    }

    fun formatData(timesTamp: Long): String {
        val date = Date(timesTamp * 1000) //Convert Unix timestamp to milliseconds
        val dataFormat = SimpleDateFormat("EEE,MM,d", Locale.getDefault())
        return dataFormat.format(date)
    }

    fun getWeatherIcon(iconCode: String): String {
        return "${Config.WEATHER_ICON_BASE_URL}${iconCode}@2x.png"
    }
}

data class WeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null
)