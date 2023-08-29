package app.beachist.weather.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.beachist.weather.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun WeatherScreen(
    viewModel: WeatherViewModel,
) {
    val uiState: WeatherState by viewModel.state.collectAsStateWithLifecycle()

    MaterialTheme {
        WeatherView(
            airTemp = uiState.weatherData?.airTemp,
            waterTemp = uiState.weatherData?.waterTemp,
            windSpeed = uiState.weatherData?.windSpeed,
            windDirection = uiState.weatherData?.windDirection,
            uvIndex = uiState.weatherData?.uvIndex,
            uvIndexMax = uiState.weatherData?.maxUvIndex,
            date = uiState.weatherData?.date,
        )
    }
}

@Composable
fun WeatherView(
    airTemp: Int? = null,
    waterTemp: Int? = null,
    windSpeed: Int? = null,
    windDirection: String? = null,
    uvIndex: Int? = null,
    uvIndexMax: Int? = null,
    date: LocalDateTime? = null,
) {
    val airTempString = airTemp.toPrintable("ºC")
    val waterTempString = waterTemp.toPrintable("ºC")
    val windSpeedString = windSpeed.toPrintable(" bft")
    val uvIndexString = uvIndex.toPrintable()
    val uvIndexMaxString = uvIndexMax.toPrintable()
    val windDirectionString = windDirection.toPrintable()
    val dateString = if (date == null) "" else {
        stringResource(
            id = R.string.weather_date_caption, date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        )
    }

    Column(
        Modifier
            .fillMaxHeight()
            .padding(12.dp), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WeatherRow(Icons.Outlined.WbSunny, R.string.weather_air_temperature_title, airTempString)
            WeatherRow(Icons.Outlined.Waves, R.string.weather_water_temperature_title, waterTempString)
            WeatherRow(Icons.Outlined.Air, R.string.weather_wind_speed_title, windSpeedString)
            WeatherRow(Icons.Outlined.Explore, R.string.weather_wind_direction_title, windDirectionString)
            WeatherRow(Icons.Outlined.BeachAccess, R.string.weather_uv_title, uvIndexString)
            WeatherRow(Icons.Outlined.BeachAccess, R.string.weather_max_uv_title, uvIndexMaxString)
        }

        Text(dateString, fontSize = 12.sp)
    }
}

@Composable
private fun <T> T.toPrintable(unit: String = ""): String {
    return this?.toString()?.plus(unit) ?: stringResource(R.string.weather_no_data)
}

@Composable
fun WeatherRow(icon: ImageVector, title: Int, value: String) {
    @Suppress("NAME_SHADOWING") val title = stringResource(title)

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = title, Modifier.size(36.dp))
            Text(title, fontSize = 24.sp)
        }
        Text(value, fontSize = 24.sp)
    }
}

@Preview(device = "spec:width=1340dp,height=800dp,dpi=179,orientation=portrait")
@Composable
fun WeatherScreenPreview() {
    val date = LocalDateTime.of(2023, 8, 22, 23, 42, 0)

    WeatherView(
        windDirection = "Nord-West",
        date = date,
    )
}
