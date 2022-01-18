package ru.andreyuniquename.theweatherapp.retrofit

import android.annotation.SuppressLint
import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreyuniquename.theweatherapp.R
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherService

class RetrofitGetResponse (){
    private val BaseUrl =
        "http://api.openweathermap.org/"
    private val AppId =
        "0656d14d3641e754d706c16afcf3b9f3"
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service: WeatherService = retrofit.create(WeatherService::class.java)
    private lateinit var call : Call<WeatherResponse>
    private val successfulResponseCod = 200
    private val units =
        "metric"
    private val EXCLUDE =
        "current,minutely,alerts"
    private lateinit var  returnResponse : WeatherResponse
    private var init : Boolean = false //флаг инициализации

    fun getResponseForRecycler(lat : String, lon : String) : OneCallResponse?{
        val call = service.getOneCallData(
            lat,
            lon,
            EXCLUDE,
            units,
            AppId
        )

        val dateNow = Calendar.getInstance()
        val dateDay = Calendar.DAY_OF_MONTH
        val dateHour = Calendar.HOUR_OF_DAY
        Log.d("MyTag", "dateHour = $dateHour")
        Log.d("MyTag", "dateDay = $dateDay")
        Log.d("MyTag", "dateNow = $dateNow")
        var returnResponse : OneCallResponse? =  null

        call.enqueue(object : Callback<OneCallResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<OneCallResponse>,
                response: Response<OneCallResponse>
            ) {
                if (response.code() == successfulResponseCod && response.body() != null) {
                    returnResponse = response.body()
                } else {
                    Toast.makeText(Application(),R.string.error_city, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<OneCallResponse>?, t: Throwable?) {
                if (t != null) {
                    Toast.makeText(Application(),t.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        return returnResponse
    }
    fun getResponseInfo(isItTown : Boolean, lat : String, lon : String, city:String) : WeatherResponse?
    {
        if (isItTown){
            call = service.getTownWeatherData(city, AppId)
        }
        else{
            call = service.getCurrentWeatherData(
                lat, lon, AppId
            )
        }
        Log.d("MyTag", "call is ${call.toString()}")

        call.enqueue(object : Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.code() == successfulResponseCod && response.body() != null) {
                        returnResponse = response.body()
                        init = true

                    } else {
                        init = false
                        Toast.makeText(Application(),R.string.error_city.toString(), Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    if (t != null) {
                        Toast.makeText(Application(),t.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        Log.d("MyTag", "resp is ${returnResponse.toString()}")
        if (init) return returnResponse
        else return null
    }
}
