package ru.andreyuniquename.theweatherapp.domain.entity

class MainInfo(
    private val temp:Double, private val feels_like:Double,
    val temp_min:Double, val temp_max:Double,
    val pressure:Int, val humidity:Int, val seaLevel:Int?, val grndLevel:Int?) {
    /*temp in K (-273,15)*/
    public fun getMainInfo(): String {
        var temp = temp - 273.15
        val currentTemp: String = temp.toInt().toString()
        temp = feels_like - 273.15
        val feelsTemp: String = temp.toInt().toString()
        return "$currentTemp feels like $feelsTemp"

    }
}
