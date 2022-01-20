package ru.andreyuniquename.theweatherapp.model

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import ru.andreyuniquename.theweatherapp.retrofit.RetrofitGetResponse

/* TODO Не использовать статические переменные для этой цели.
                                     Гипотетически тебе может понадобиться несколько инстансов твоих экранов,
                                     и тебе не нужно хранить эту информацию постоянно, она относится только к этому конкретному инстансу.
                                     Нужно перенести твое состояние экрана во ViewModel. Она переживает смену конфигурации (переворот экрана).*/

class MyViewModel (application: Application) : AndroidViewModel(application) {
    var mainInfoLiveData = MutableLiveData<String>()
    var weekLiveData = MutableLiveData<List<String>>()
    var dayLiveData = MutableLiveData<List<String>>()


    fun getOldData(){
        mainInfoLiveData.value = oldData
        weekLiveData.value = oldDataWeek
        dayLiveData.value = oldDataDay
    }
    fun getLastKnownLocation(context: Context,  fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {return}
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude.toString()
                    lon = location.longitude.toString()
                    getDataByLat(context)
                }
                else
                    Toast.makeText(context,error_city, Toast.LENGTH_SHORT).show()
            }
    }
    private fun getDataByLat(context: Context) {
        RetrofitGetResponse().getResponseInfo(context,false, lat,lon, cityName){weather ->
            if (weather == null)
                mainInfoLiveData.value = error_city
            else
            {
                cityName = weather.name!!
                val infoStr = StringBuilder()
                infoStr.append(cityName)
                    .append("\n")
                    .append("t: ${(weather.main!!.temp.toDouble()- celciumConst).toInt()}")
                    .append("\n")
                    .append(weather.weather[0].description)

                // UPD размер массива не нужен потому что по апи там только нулевой элемент хз почему так https://openweathermap.org/api/one-call-api

                mainInfoLiveData.value = infoStr.toString()
                oldData = infoStr.toString()
                getToDayWeather(context,true)
            }
        }
    }
    fun getDataByTown(context: Context, city: String) {
        RetrofitGetResponse().getResponseInfo(context,true, lat,lon, city){weather ->
            if (weather == null)
                //тут мы тоже умираем причем незаконно
                mainInfoLiveData.value = error_city

            else
            {
                cityName = city
                lat = weather.coord!!.lat.toString()
                lon = weather.coord!!.lon.toString()
                val infoStr = StringBuilder()
                    infoStr.append(cityName)
                        .append("\n")
                        .append("t: ${(weather.main!!.temp.toDouble()- celciumConst).toInt()}")
                        .append("\n")
                        .append(weather.weather[0].description)

                    // UPD размер массива не нужен потому что по апи там только нулевой элемент хз почему так https://openweathermap.org/api/one-call-api

                mainInfoLiveData.value = infoStr.toString()
                getToDayWeather(context,false)

            }
        }
    }
    private fun getToDayWeather(context: Context, isItFromLat : Boolean) {
        RetrofitGetResponse().getResponseForRecycler(context, lat, lon){weather ->
            if (weather == null)
                mainInfoLiveData.value = error_city
            else
            {
                val dateNow = Calendar.getInstance()
                val dateDay = dateNow.get(Calendar.DAY_OF_MONTH)
                var dateHour = dateNow.get(Calendar.HOUR_OF_DAY)
                val dateMonth = dateNow.get(Calendar.MONTH)
                Log.d("MyTag", "dateHour = $dateHour")
                Log.d("MyTag", "dateDay = $dateDay")
                Log.d("MyTag", "dateNow = $dateNow")
                val dataDay = mutableListOf<String>() //size 25 будет
                val dataWeek = mutableListOf<String>() //size 8 будет
                // 1) TODO нужна проверка на длину массива
                // 2) TODO непонятно откуда взялись (0..24) и (0..7)
                // 3) TODO не углублялся в логику (т.к. она нечитаема и в полвторого ночи у меня состояние овоща),
                //     вроде как создание MutableList + forEach можно заменить на map
                // 4) TODO вместо ручной конкатенации строк используй шаблоны (string templates)

                (0..24).forEach { i -> dataDay.add(
                    "${if (dateHour + i > 12) if (dateHour +i >24) dateHour-24+i else dateHour-12+i else dateHour +i }:00 \n" +
                        "${weather.hourly[i].temp.toInt()} \n" +
                        weather.hourly[i].weather[0].description
                ) }
                (0..7).forEach { i -> dataWeek.add(
                    "${if (dateDay + i > 31) "${dateDay-31+i} ${months[dateMonth + 1]}" else "${dateDay+i} ${months[dateMonth]}"} \n" +
                        "${weather.daily[i].temp.day.toInt()}\n" +
                        weather.daily[i].weather[0].description
                ) }

                if (isItFromLat){
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
        private var error_city ="Error in name of city, try again"
        private var oldData = ""
        private var oldDataDay = mutableListOf<String>()
        private var oldDataWeek = mutableListOf<String>()
        const val celciumConst : Double = 273.15
        val months = arrayOf("Jan",
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