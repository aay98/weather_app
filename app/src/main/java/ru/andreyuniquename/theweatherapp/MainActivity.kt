package ru.andreyuniquename.theweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherService
import ru.andreyuniquename.theweatherapp.CustomRecyclerAdapter


// TODO RecyclerView.Adapter не должен находиться в слое Domain. Domain - это бизнес-логика. RecyclerView - это UI.
// TODO необходимо сделать форматирование кода Ctrl + Alt + L
// TODO проверка на null не нужна
// TODO нужно занулять binding в конце ЖЦ твоего активити, чтобы избежать утечку памяти
// TODO вынеси константу Another в Companion object

// TODO не используется, убрать

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding?  = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)


        binding!!.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                getLastKnownLocation()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (binding!!.citySpinner.selectedItem) {
                    another -> binding!!.inputLayout.visibility =
                        View.VISIBLE
                    else -> {
                        cityName = binding!!.citySpinner.selectedItem.toString()
                        getDataByTown()
                    }
                }
            }
        }
        binding!!.inputButton.setOnClickListener() {
            if (binding!!.inputText.text != null) {
                cityName = binding!!.inputText.text.toString()
                binding!!.inputLayout.visibility = View.GONE
                getDataByTown()
            } else binding!!.mainInfo.text = getString(R.string.error_city)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var rightNow = Calendar.getInstance()
        var time : Int = rightNow.timeInMillis.toInt() - timeOut

        binding!!.myLocationImage.setOnClickListener() {
            rightNow = Calendar.getInstance()
            var newTime = rightNow.timeInMillis.toInt()
            Log.d("MyTag","time is $time, new time is $newTime")
            if (time.toInt() + timeOut < newTime) {
                getLastKnownLocation()
                time = newTime
            }
            else{
                Toast.makeText(applicationContext,toMuchClicks,Toast.LENGTH_SHORT).show()
                Log.d("MyTag","last was in $time, but now only $newTime it means that was only ${(newTime-time)/1000} sec")
                binding!!.mainInfo.text = oldData
                binding!!.recyclerViewDay.adapter = CustomRecyclerAdapter(oldDataDay)
                binding!!.recyclerViewWeek.adapter = CustomRecyclerAdapter(oldDataWeek)

            }

        }
        // TODO если быстро кликать по этой кнопке, будет уходить куча одинаковых запросов. Подумай, что с этим можно сделать

        // TODO ты уже задал LayoutManager в вёрстке, соответственно в коде его не нужно уже задавать
        binding!!.recyclerViewWeek.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding!!.recyclerViewDay.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // TODO вынести логику обработки из Activity в ViewModel (гугли MVVM), ViewModel саму создавать через ViewModelProvider.Factory
                if (location != null) {
                    lat = location.latitude.toString()
                    lon = location.longitude.toString()
                    getDataByLat()

                }

            } // TODO нужна обработка ошибок

    }

    private fun getDataByLat() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.code() == 200) { // TODO вынести 200 в константу с осмысленным названием
                    // TODO нужна безопасная проверка на null, поскольку даже ответ 200 не гарантирует, что сервер вернет корректные данные
                    val weatherResponse = response.body()!!
                    // TODO нужна проверка на null
                    cityName = weatherResponse.name!!
                    // TODO нужна проверка на null и размер массива
                    val infoStr = nowStringBuilder(
                        weatherResponse.main!!.temp.toDouble(),
                        weatherResponse.weather[0].description
                    )
                    binding!!.mainInfo.text = infoStr
                    oldData = infoStr
                    getToDayWeather(true)
                } else binding!!.mainInfo.text = getString(R.string.error_city)
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding!!.mainInfo.text = t.message

            }
        })
    }

    private fun getDataByTown() {
        // TODO вынести создание клиента в отдельный класс
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
                if (response.code() == 200) { // TODO вынести 200 в константу
                    // TODO нужна безопасная проверка на null, поскольку даже ответ 200 не гарантирует, что сервер вернет корректные данные
                    val weatherResponse = response.body()!!

                    lat = weatherResponse.coord!!.lat.toString()
                    lon = weatherResponse.coord!!.lon.toString()
                    // TODO нужна проверка что список не пустой
                    // TODO нужна проверка на не null
                    // TODO нужно осмысленное название для TextView, чтобы без превью было понятно, в чем его суть
                    binding!!.mainInfo.text = nowStringBuilder(
                        weatherResponse.main!!.temp.toDouble(),
                        weatherResponse.weather[0].description
                    )
                    getToDayWeather(false)

                } else {
                    binding!!.mainInfo.text = getString(R.string.error_city)
                }

            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding!!.mainInfo.text = t.message
            }
        })
    }

    // TODO перенести в ViewModel
    // TODO вместо ручной конкатенации строк используй шаблоны (string templates):
    //  в этом случае Kotlin под капотом может использовать StringBuilder для улучшения производительности;
    //  также это улучшит читаемость
    private fun nowStringBuilder(temp: Double, desc: String?): String {
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
    private fun hourStringBuilder(hour: String, temp: Double, desc: String?): String {
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
        val call = service.getOneCallData(lat, lon, exclude, units, AppId)

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
                    // TODO("Не закоммитил файл")
                    binding!!.recyclerViewDay.adapter = CustomRecyclerAdapter(dataDay)
                    binding!!.recyclerViewWeek.adapter = CustomRecyclerAdapter(dataWeek)

                } else {
                    binding!!.mainInfo.text = getString(R.string.error_city)
                }

            }

            override fun onFailure(call: Call<OneCallResponse>?, t: Throwable?) {
                if (t != null) {
                    binding!!.mainInfo.text = t.message
                }
            }
        })
    }


    companion object {
        // TODO все поля здесь должны быть по возможности приватными
        var cityName = "Moscow" /* TODO Не использовать статические переменные для этой цели.
                                     Гипотетически тебе может понадобиться несколько инстансов твоих экранов,
                                     и тебе не нужно хранить эту информацию постоянно, она относится только к этому конкретному инстансу.
                                     Нужно перенести твое состояние экрана во ViewModel. Она переживает смену конфигурации (переворот экрана).
        */
        var BaseUrl =
            "http://api.openweathermap.org/" // TODO перенести в класс создающий клиент ретрофита, сделать приватной константой,
        var AppId =
            "0656d14d3641e754d706c16afcf3b9f3" // TODO насколько я понимаю, это поле можно добавлять через Interceptor, но в этом я не уверен, пока можешь оставить так
        var lat: String = "35" // TODO аналогично cityName
        var lon: String = "139" // TODO аналогично cityName
        const val units =
            "metric" // TODO константы пишутся капсом. Инфа к изучению: https://kotlinlang.org/docs/coding-conventions.html
        const val exclude =
            "current,minutely,alerts" // TODO поправить название поля, по полю непонятно в чем его суть
        const val another = "Another"
        const val timeOut : Int = 60000
        const val toMuchClicks : String = "Too much clicks"
        var oldData = ""
        var oldDataDay = mutableListOf<String>()
        var oldDataWeek = mutableListOf<String>()
    }
}






