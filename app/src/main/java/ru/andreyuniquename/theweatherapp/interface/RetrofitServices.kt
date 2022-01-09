package ru.andreyuniquename.theweatherapp.`interface`

import ru.andreyuniquename.theweatherapp.domain.WeatherMessage
import retrofit2.Call
import retrofit2.http.*

interface RetrofitServices {
    @GET("Weather")
    fun getWeather(function: () -> Unit):Call<WeatherMessage>
}