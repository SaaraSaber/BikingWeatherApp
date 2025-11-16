package ir.developer.bihingweather.domain.repository

import ir.developer.bihingweather.domain.model.WeatherResponse

interface WeatherRepository {
    suspend fun getWeatherForecast(lat: Double, lon: Double): Result<WeatherResponse>
}