package ru.andreyuniquename.theweatherapp.domain

import ru.andreyuniquename.theweatherapp.domain.entity.*

class WeatherMessage (val coord : CoordInfo?, val weather: WeatherInfo?,
                      val base :String?, val mainInfo : MainInfo?, val visibilityInfo: Int?,
                      val wind: WindInfo?, val rainInfo: RainInfo?, val cloudsInfo: CloudsInfo?,
                      val dt:Int?, val sysInfo: SysInfo?, val timeZone: Int?, val sysId :Int?,
                      private val name:String?, val cod:Int?) {
    public fun getInfo(): String {
        return "Weather in " + name + " is " + mainInfo?.getMainInfo()
    }
}










