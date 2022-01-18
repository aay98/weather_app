package ru.andreyuniquename.theweatherapp.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreyuniquename.theweatherapp.MainActivity
import ru.andreyuniquename.theweatherapp.R
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherService
import ru.andreyuniquename.theweatherapp.retrofit.RetrofitGetResponse as RetrofitGetResponse

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

    fun getLastKnownLocation(fusedLocationClient: FusedLocationProviderClient) {
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
                    getDataByLat()
                }
                else
                    Toast.makeText(getApplication(),R.string.errorTryAgain, Toast.LENGTH_SHORT).show()
            }

    }

    private fun getDataByLat() {
        val retro  = RetrofitGetResponse()
        Log.d("MyTag","lon = $lon, lat = $lat")
        val response = retro.getResponseInfo(false, lat,lon, cityName)
        Log.d("MyTag",response.toString())
        if (response != null)
        {
            cityName = response.name!!
            val infoStr = nowStringBuilder(
                response.main!!.temp.toDouble(),
                response.weather[0].description
                // UPD размер массива не нужен потому что по апи там только нулевой элемент хз почему так https://openweathermap.org/api/one-call-api
            )
            mainInfoLiveData.value = infoStr
            oldData = infoStr
            getToDayWeather(true)

        } else mainInfoLiveData.value = R.string.error_city.toString()

    }

    fun getDataByTown(city: String) {
        // TODO вынести создание клиента в отдельный класс
        cityName = city
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getTownWeatherData(cityName, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.code() == SUCCESSFUL_RESPONSE_COD) { // TODO вынести 200 в константу
                    // TODO нужна безопасная проверка на null, поскольку даже ответ 200 не гарантирует, что сервер вернет корректные данные
                    val weatherResponse = response.body()!!
                    lat = weatherResponse.coord!!.lat.toString()
                    lon = weatherResponse.coord!!.lon.toString()
                    // TODO нужна проверка что список не пустой
                    // TODO нужна проверка на не null
                    mainInfoLiveData.value = nowStringBuilder(
                        weatherResponse.main!!.temp.toDouble(),
                        weatherResponse.weather[0].description
                    )
                    getToDayWeather(false)

                } else {
                    mainInfoLiveData.value = R.string.error_city.toString()
                }

            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                mainInfoLiveData.value = t.message
            }
        })
    }

    // TODO перенести в ViewModel
    // TODO вместо ручной конкатенации строк используй шаблоны (string templates):
    //  в этом случае Kotlin под капотом может использовать StringBuilder для улучшения производительности;
    //  также это улучшит читаемость
    fun nowStringBuilder(temp: Double, desc: String?): String {
        return cityName +
                "\n" +
                "t: " +
                (temp - 273.15).toInt()
                    .toString() + // TODO захардкодить константу, вынеси в Companion object
                "\n" +
                desc
    }

    // TODO перенести в ViewModel
    // TODO вместо ручной конкатенации строк используй шаблоны (string templates):
    fun hourStringBuilder(hour: String, temp: Double, desc: String?): String {
        return hour + ":00" +
                "\n" +
                "t: " +
                temp.toInt().toString() +
                "\n" +
                desc
    }



    // TODO вынести логику обработки из Activity в ViewModel (гугли MVVM), ViewModel саму создавать через ViewModelProvider.Factory
    private fun getToDayWeather(isItFromLat : Boolean) {
        // TODO вынести создание клиента в отдельный класс [2]. Не нужно пересоздавать клиент каждый раз,
        //  когда делаешь запрос, тк создание клиента может быть ресурсоемкой операцией и ухудшит скорость запросов.
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getOneCallData(
            lat, lon, EXCLUDE, UNITS, AppId
        )

        val dateNow = Calendar.getInstance()
        val dateDay = Calendar.DAY_OF_MONTH
        val dateHour = Calendar.HOUR_OF_DAY
        Log.d("MyTag", "dateHour = $dateHour")
        Log.d("MyTag", "dateDay = $dateDay")
        Log.d("MyTag", "dateNow = $dateNow")
        call.enqueue(object : Callback<OneCallResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<OneCallResponse>,
                response: Response<OneCallResponse>
            ) {
                if (response.code() == 200) { // TODO вынести 200 в константу
                    // TODO response.body() может быть null, сделай безопасную проверку
                    val weatherResponse = response.body()!!
                    val dataDay = mutableListOf<String>()
                    val dataWeek = mutableListOf<String>()
                    // 1) TODO нужна проверка на длину массива
                    // 2) TODO непонятно откуда взялись (0..24) и (0..8)
                    // 3) TODO не углублялся в логику (т.к. она нечитаема и в полвторого ночи у меня состояние овоща),
                    //     вроде как создание MutableList + forEach можно заменить на map
                    // 4) TODO вместо ручной конкатенации строк используй шаблоны (string templates)
                    (0..24).forEach { i -> dataDay.add((dateHour + i).toString() + ":00" + "\n" + weatherResponse.hourly[i].temp.toString() + "\n" + weatherResponse.hourly[i].weather[0].description) }
                    (0..8).forEach { i -> dataWeek.add("$i element") }
                    if (isItFromLat){
                        oldDataDay = dataDay
                        oldDataWeek = dataWeek
                    }
                    dayLiveData.value = dataDay
                    weekLiveData.value = dataWeek

                } else {
                    mainInfoLiveData.value = R.string.error_city.toString()
                }

            }

            override fun onFailure(call: Call<OneCallResponse>?, t: Throwable?) {
                if (t != null) {
                    mainInfoLiveData.value = t.message
                }
            }
        })
    }

    companion object {
        var cityName = ""
        private val BaseUrl =
            "http://api.openweathermap.org/" // TODO перенести в класс создающий клиент ретрофита, сделать приватной константой,
        private val AppId =
            "0656d14d3641e754d706c16afcf3b9f3" // TODO насколько я понимаю, это поле можно добавлять через Interceptor, но в этом я не уверен, пока можешь оставить так
        private var lat: String = "35" // TODO аналогично cityName
        private var lon: String = "139" // TODO аналогично cityName
        private const val SUCCESSFUL_RESPONSE_COD = 200
        private const val UNITS =
            "metric"
        private const val EXCLUDE =
            "current,minutely,alerts" // means !include (see API)
        private var oldData = ""
        private var oldDataDay = mutableListOf<String>()
        private var oldDataWeek = mutableListOf<String>()
    }
}