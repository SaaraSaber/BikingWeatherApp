package ir.developer.bihingweather.di

import ir.developer.bihingweather.data.remote.Config
import ir.developer.bihingweather.data.remote.WeatherApiService
import ir.developer.bihingweather.data.repository.WeatherRepositoryImpl
import ir.developer.bihingweather.domain.repository.WeatherRepository
import ir.developer.bihingweather.domain.usecase.CalculateBikingRidingScoreUseCase
import ir.developer.bihingweather.domain.usecase.GetWeatherForecastUseCase
import ir.developer.bihingweather.presentation.viewmodel.WeatherViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder().baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    single {
        get<Retrofit>().create(WeatherApiService::class.java)
    }

    //Repository
    single<WeatherRepository> {
        WeatherRepositoryImpl(get())
    }

    //usecase
    single {
        GetWeatherForecastUseCase(get())
    }
    single {
        CalculateBikingRidingScoreUseCase()
    }

    //viewmodel
    viewModel { WeatherViewModel(get(), get(), get()) }

}