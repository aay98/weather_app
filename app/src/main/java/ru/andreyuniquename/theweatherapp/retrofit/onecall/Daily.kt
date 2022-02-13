package ru.andreyuniquename.theweatherapp.retrofit.onecall

data class Daily (

    var dt : Int,
    var sunrise : Int,
    var sunset : Int,
    var moonrise : Int,
    var moonset : Int,
    var moon_phase : Double,
    var temp : Temp,
    var feels_like : FeelsLike,
    var pressure : Int,
    var humidity : Int,
    var dew_point : Double,
    var wind_speed : Double,
    var wind_deg : Int,
    var wind_gust : Double,
    var weather : List<Weather>,
    var clouds : Int,
    var pop : Double,
    var uvi : Double
)