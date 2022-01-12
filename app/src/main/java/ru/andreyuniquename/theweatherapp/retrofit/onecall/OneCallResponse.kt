package ru.andreyuniquename.theweatherapp.retrofit.onecall


class OneCallResponse (
    var lat : Double,
    var lon : Double,
    var timezone : String,
    var timezone_offset : Int,
    var hourly : List<Hourly>,
    var daily : List<Daily>
)