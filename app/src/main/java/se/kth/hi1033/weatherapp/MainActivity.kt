package se.kth.hi1033.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import se.kth.hi1033.weatherapp.ui.navigation.WeatherNavHost
import se.kth.hi1033.weatherapp.ui.theme.WeatherAppTheme
import se.kth.hi1033.weatherapp.ui.viewmodel.WeatherViewModel
import se.kth.hi1033.weatherapp.ui.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel(
                factory = WeatherViewModelFactory(applicationContext)
            )
            val uiState by viewModel.uiState.collectAsState()

            WeatherAppTheme(darkTheme = uiState.settings.useDarkTheme) {
                WeatherNavHost(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
