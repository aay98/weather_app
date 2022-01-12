package ru.andreyuniquename.theweatherapp.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse

interface WeatherService {
    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(@Query("lat") lat: String,
                              @Query("lon") lon: String,
                              @Query("APPID") app_id: String): Call<WeatherResponse>
    @GET("data/2.5/weather?")
    fun getTownWeatherData(@Query("q") cityName: String,
                           @Query("APPID") app_id: String): Call<WeatherResponse>
    @GET("data/2.5/onecall?")
    fun getOneCallData(@Query("lat") lat: String,
                       @Query("lon") lon: String,
                       @Query("exclude") exclude: String,
                       @Query("units") units: String,
                       @Query("APPID") app_id: String): Call<OneCallResponse>

}
/*
api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}
api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
https://api.openweathermap.org/data/2.5/onecall?lat={}}lon={}&exclude={current,minutely,alerts}&units={metric}&appid={API key}


 */