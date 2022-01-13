package ru.andreyuniquename.theweatherapp.retrofit.weather

import com.google.gson.annotations.SerializedName

// TODO вынести все классы в отдельные файлы
class WeatherResponse {

    @SerializedName("coord")
    var coord: Coord? = null
    @SerializedName("sys")
    var sys: Sys? = null
    @SerializedName("weather")
    var weather = ArrayList<Weather>()
    @SerializedName("main")
    var main: Main? = null
    @SerializedName("wind")
    var wind: Wind? = null
    @SerializedName("rain")
    var rain: Rain? = null
    @SerializedName("snow")
    var snow: Snow? = null
    @SerializedName("clouds")
    var clouds: Clouds? = null
    @SerializedName("dt")
    var dt: Float = 0.toFloat()
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("name")
    var name: String? = null
    @SerializedName("cod")
    var cod: Float = 0.toFloat()
}


