package se.kth.hi1033.weatherapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kth.hi1033.weatherapp.model.FavouritePlace
import se.kth.hi1033.weatherapp.model.UserSettings
import se.kth.hi1033.weatherapp.model.WeatherForecast

private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

interface WeatherLocalStorage {
    suspend fun saveLastForecast(forecast: WeatherForecast)
    suspend fun getLastForecast(): WeatherForecast?
    fun observeFavourites(): Flow<List<FavouritePlace>>
    suspend fun saveFavourites(favourites: List<FavouritePlace>)
    fun observeSettings(): Flow<UserSettings>
    suspend fun saveSettings(settings: UserSettings)
}

class DataStoreWeatherStorage(private val context: Context) : WeatherLocalStorage {

    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        val KEY_LAST_FORECAST = stringPreferencesKey("last_forecast_json")
        val KEY_FAVOURITES = stringPreferencesKey("favourites_json")
        val KEY_SETTINGS = stringPreferencesKey("settings_json")
    }

    override suspend fun saveLastForecast(forecast: WeatherForecast) {
        val encoded = json.encodeToString(forecast)
        context.weatherDataStore.edit { prefs ->
            prefs[KEY_LAST_FORECAST] = encoded
        }
    }

    override suspend fun getLastForecast(): WeatherForecast? {
        val raw = context.weatherDataStore.data.first()[KEY_LAST_FORECAST] ?: return null
        return runCatching { json.decodeFromString<WeatherForecast>(raw) }.getOrNull()
    }

    override fun observeFavourites(): Flow<List<FavouritePlace>> {
        return context.weatherDataStore.data.map { prefs ->
            val raw = prefs[KEY_FAVOURITES] ?: return@map emptyList()
            runCatching { json.decodeFromString<List<FavouritePlace>>(raw) }.getOrDefault(emptyList())
        }
    }

    override suspend fun saveFavourites(favourites: List<FavouritePlace>) {
        val encoded = json.encodeToString(favourites)
        context.weatherDataStore.edit { prefs ->
            prefs[KEY_FAVOURITES] = encoded
        }
    }

    override fun observeSettings(): Flow<UserSettings> {
        return context.weatherDataStore.data.map { prefs ->
            val raw = prefs[KEY_SETTINGS] ?: return@map UserSettings()
            runCatching { json.decodeFromString<UserSettings>(raw) }.getOrDefault(UserSettings())
        }
    }

    override suspend fun saveSettings(settings: UserSettings) {
        val encoded = json.encodeToString(settings)
        context.weatherDataStore.edit { prefs ->
            prefs[KEY_SETTINGS] = encoded
        }
    }
}
