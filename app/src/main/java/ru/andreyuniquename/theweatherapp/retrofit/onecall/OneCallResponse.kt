package ru.andreyuniquename.theweatherapp.retrofit.onecall


class OneCallResponse (
    var lat : Double,
    var lon : Double,
    var timezone : String,
    var timezone_offset : Int,
    var hourly : List<Hourly>, // TODO не закоммитил файл
    var daily : List<Daily> // TODO не закоммитил файл
)