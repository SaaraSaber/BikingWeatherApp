package ir.developer.bihingweather.domain.usecase

import ir.developer.bihingweather.domain.model.WeatherResponse
import ir.developer.bihingweather.domain.repository.WeatherRepository

class GetWeatherForecastUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<WeatherResponse> {
        return repository.getWeatherForecast(lat = lat, lon = lon)
    }
}