package app.beachist.weather.view

import androidx.lifecycle.viewModelScope
import app.beachist.shared.base.FlowBaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.shared.base.set
import app.beachist.weather.dtos.WeatherInfo
import app.beachist.weather.service.WeatherService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
internal class WeatherViewModel(private val service: WeatherService) :
    FlowBaseViewModel<WeatherAction, WeatherState, WeatherEffect>(emptyState) {

    companion object {
        private val emptyState = WeatherState()
    }

    override fun handleActions() {
        actions.filterIsInstance<WeatherAction.FetchWeather>()
            .onEach { onFetch() }
            .launchIn(viewModelScope)
    }

    private suspend fun onFetch() {
        service.weatherInfo.collect {
            _state.set { copy(weatherData = it) }
        }
    }
}

internal sealed class WeatherAction : ViewModelAction() {
    object FetchWeather: WeatherAction()
}

internal sealed class WeatherEffect : ViewModelEffect()

internal data class WeatherState(val weatherData: WeatherInfo? = null) : ViewModelState
