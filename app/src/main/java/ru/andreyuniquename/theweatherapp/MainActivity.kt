package ru.andreyuniquename.theweatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import android.app.AlertDialog

import ru.andreyuniquename.theweatherapp.retrofit.common.Common
import ru.andreyuniquename.theweatherapp.`interface`.RetrofitServices
import ru.andreyuniquename.theweatherapp.domain.WeatherMessage

import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var mService: RetrofitServices
    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.tv1.text = "binding is working"

        mService = Common.retrofitServices



        getIt()
    }

    private fun getIt() {
        dialog.show()
        mService.getWeather(){
            fun onFailure(call: Call<WeatherMessage>, t: Throwable) {

            }

            fun onResponse(call: Call<WeatherMessage>, response: Response<WeatherMessage>) {
                binding.tv1.text = response.

                dialog.dismiss()
            }
        }
    }

}