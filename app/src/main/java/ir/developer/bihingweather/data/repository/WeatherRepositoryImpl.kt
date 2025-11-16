package ir.developer.bihingweather.data.repository

import ir.developer.bihingweather.data.remote.Config
import ir.developer.bihingweather.data.remote.WeatherApiService
import ir.developer.bihingweather.domain.model.WeatherResponse
import ir.developer.bihingweather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(private val apiService: WeatherApiService) : WeatherRepository {
    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double
    ): Result<WeatherResponse> {
        return try {
            val response = apiService.getWeatherForecast(
                lat = lat,
                lon = lon,
                apiKey = Config.OPEN_WEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}