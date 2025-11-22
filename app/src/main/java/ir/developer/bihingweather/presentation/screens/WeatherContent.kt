package ir.developer.bihingweather.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.developer.bihingweather.domain.model.WeatherResponse
import ir.developer.bihingweather.presentation.components.BikeRidingCard
import ir.developer.bihingweather.presentation.viewmodel.WeatherViewModel

@Composable
fun WeatherContent(
    weatherData: WeatherResponse,
    viewModel: WeatherViewModel
) {
    val dailyScores by viewModel.dailyScores
    val bestDay = dailyScores.maxByOrNull { it.second.score }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(weatherData, bestDay?.first, bestDay?.second, viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(dailyScores) { (forecast, score) ->
                BikeRidingCard(
                    forecast = forecast,
                    score = score,
                    viewModel = viewModel,
                    isBest = bestDay?.first?.date == forecast.date
                )
            }
        }
    }
}
fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF22C55E) // Green - Excellent
        score >= 60 -> Color(0xFF4ADE80) // Light Green - Good
        score >= 40 -> Color(0xFFFACC15) // Yellow - Moderate
        score >= 20 -> Color(0xFFF87171) // Light Red - Poor
        else -> Color(0xFFDC2626) // Red - Dangerous
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}