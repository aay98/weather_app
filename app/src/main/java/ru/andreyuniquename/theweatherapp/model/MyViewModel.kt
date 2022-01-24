package ru.andreyuniquename.theweatherapp.model

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import ru.andreyuniquename.theweatherapp.retrofit.RetrofitGetResponse

class MyViewModel(application: Application) : AndroidViewModel(application) {
    var mainInfoLiveData = MutableLiveData<String>()
    var weekLiveData = MutableLiveData<List<String>>()
    var dayLiveData = MutableLiveData<List<String>>()
    var errorLiveData = MutableLiveData<String>()

    fun getOldData() {
        mainInfoLiveData.value = oldData
        weekLiveData.value = oldDataWeek
        dayLiveData.value = oldDataDay
    }
    fun getLastKnownLocation(fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) { errorLiveData.value = errorInPermission }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude.toString()
                    lon = location.longitude.toString()
                    Log.d("MyTag", "lon is $lon, lat is $lat")
                    getDataByLat()
                } else {
                    Log.d("MyTag", "No location")
                    errorLiveData.value = errorLocation
                    getOldData()
                }
            }
    }
    private fun getDataByLat() {
        RetrofitGetResponse().getResponseInfo(errorLiveData, false, lat, lon, cityName) { weather ->
            if (weather == null)
                errorLiveData.value = errorCity
            else {
                Log.d("MyTag", "getDataByLat is working city is ${weather.name!!}")
                cityName = weather.name!!
                val infoStr = StringBuilder()
                infoStr.append(cityName)
                    .append("\n")
                    .append("t: ${(weather.main!!.temp.toDouble() - celsiusConst).toInt()}")
                    .append("\n")
                    .append(weather.weather[0].description)

                // UPD размер массива не нужен потому что по апи там только нулевой элемент хз почему так https://openweathermap.org/api/one-call-api

                mainInfoLiveData.value = infoStr.toString()
                oldData = infoStr.toString()
                getToDayWeather(true)
            }
        }
    }
    fun getDataByTown(city: String) {
        RetrofitGetResponse().getResponseInfo(errorLiveData, true, lat, lon, city) { weather ->
            if (weather == null)
                errorLiveData.value = errorCity
            else {
                cityName = city
                lat = weather.coord!!.lat.toString()
                lon = weather.coord!!.lon.toString()
                val infoStr = StringBuilder()
                infoStr.append(cityName)
                    .append("\n")
                    .append("t: ${(weather.main!!.temp.toDouble() - celsiusConst).toInt()}")
                    .append("\n")
                    .append(weather.weather[0].description)

                // UPD размер массива не нужен потому что по апи там только нулевой элемент хз почему так https://openweathermap.org/api/one-call-api

                mainInfoLiveData.value = infoStr.toString()
                getToDayWeather(false)
            }
        }
    }
    private fun getToDayWeather(isItFromLat: Boolean) {
        RetrofitGetResponse().getResponseForRecycler(errorLiveData, lat, lon) { weather ->
            if (weather == null)
                errorLiveData.value = errorCity
            else {
                val dateNow = Calendar.getInstance()
                val dateDay = dateNow.get(Calendar.DAY_OF_MONTH)
                val dateHour = dateNow.get(Calendar.HOUR_OF_DAY)
                val dateMonth = dateNow.get(Calendar.MONTH)
                Log.d("MyTag", "dateHour = $dateHour")
                Log.d("MyTag", "dateDay = $dateDay")
                Log.d("MyTag", "dateNow = $dateNow")
                val dataDay = mutableListOf<String>() // size 25 будет
                val dataWeek = mutableListOf<String>() // size 8 будет

                (0..24).forEach { i ->
                    dataDay.add(
                        "${if (dateHour + i > 12) if (dateHour + i > 24) dateHour - 24 + i else dateHour - 12 + i else dateHour + i }:00 \n" +
                            "${weather.hourly[i].temp.toInt()} \n" +
                            weather.hourly[i].weather[0].description
                    )
                }
                (0..7).forEach { i ->
                    dataWeek.add(
                        "${if (dateDay + i > 31) "${dateDay - 31 + i} ${months[dateMonth + 1]}" else "${dateDay + i} ${months[dateMonth]}"} \n" +
                            "${weather.daily[i].temp.day.toInt()}\n" +
                            weather.daily[i].weather[0].description
                    )
                }

                if (isItFromLat) {
                    oldDataDay = dataDay
                    oldDataWeek = dataWeek
                }
                dayLiveData.value = dataDay
                weekLiveData.value = dataWeek
            }
        }
    }

    companion object {
        var cityName = ""
        private var lat: String = "35" // TODO аналогично cityName
        private var lon: String = "139" // TODO аналогично cityName
        private const val errorCity = "Error in name of city, try again"
        private const val errorLocation = "Error in getting location"
        private const val errorInPermission = "No permission granted"
        private var oldData = ""
        private var oldDataDay = mutableListOf<String>()
        private var oldDataWeek = mutableListOf<String>()
        const val celsiusConst: Double = 273.15
        val months = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Okt",
            "Nov",
            "Dec"
        )
    }
}
