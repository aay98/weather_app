package ru.andreyuniquename.theweatherapp.retrofit

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherResponse
import ru.andreyuniquename.theweatherapp.retrofit.weather.WeatherService

class RetrofitGetResponse() {
    private val baseUrl =
        "http://api.openweathermap.org/"
    private val apiId =
        "0656d14d3641e754d706c16afcf3b9f3"
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service: WeatherService = retrofit.create(WeatherService::class.java)

    private val errorCity = "Error in name of city, try again"
    private lateinit var call: Call<WeatherResponse>
    private val successfulResponseCod = 200
    private val units =
        "metric"
    private val exclude =
        "current,minutely,alerts"
    private val responseError = "Error in response body"
    private val codError = "Error in response cod"

    fun getResponseForRecycler(errorLiveData: MutableLiveData<String>, lat: String, lon: String, callback: (oneCall: OneCallResponse?) -> Unit) {
        val call = service.getOneCallData(
            lat,
            lon,
            exclude,
            units,
            apiId
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
                if (response.code() == successfulResponseCod && response.body() != null) {
                    val returnResponse = response.body()
                    callback(returnResponse)
                } else {
                    if (response.code() == successfulResponseCod)
                        errorLiveData.value = responseError
                    else
                        errorLiveData.value = codError
                    callback(null)
                }
            }

            override fun onFailure(call: Call<OneCallResponse>?, t: Throwable?) {
                if (t != null) {
                    errorLiveData.value = t.message.toString()
                }
                callback(null)
            }
        })
    }

    fun getResponseInfo(errorLiveData: MutableLiveData<String>, isItTown: Boolean, lat: String, lon: String, city: String, callback: (weather: WeatherResponse?) -> Unit) {
        call = if (isItTown) {
            service.getTownWeatherData(city, apiId)
        } else {
            service.getCurrentWeatherData(
                lat, lon, apiId
            )
        }
        Log.d("MyTag", "getResponse $call")

        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.code() == successfulResponseCod && response.body() != null) {
                    val returnResponse = response.body()
                    Log.d("MyTag", "resp cod is ${response.code()}")
                    callback(returnResponse)
                } else {
                    if (response.code() == successfulResponseCod)
                        errorLiveData.value = responseError
                    else
                        errorLiveData.value = codError
                    callback(null)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                errorLiveData.value = t.message.toString()
                callback(null)
            }
        })
    }
}
