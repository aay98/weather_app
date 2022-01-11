package ru.andreyuniquename.theweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreyuniquename.theweatherapp.retrofit.WeatherResponse
import ru.andreyuniquename.theweatherapp.retrofit.WeatherService
import android.util.Log



/*
  .Отображение прогноза погоды за неделю за текущий день
    - https://api.openweathermap.org/data/2.5/onecall?lat=45&lon=-9.1987&exclude=current,hourly,minutely,alerts&units=metric&appid=0656d14d3641e754d706c16afcf3b9f3
    -широту и долготу узнавать по getDataByTown
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.ChangeButton.setOnClickListener(){ binding.inputLayout.visibility = View.VISIBLE}
        binding.inputButton.setOnClickListener(){
            if (binding.inputText.text != null)
            {
                cityName = binding.inputText.text.toString()
                binding.inputLayout.visibility = View.GONE
                getDataByTown()
            }
            else binding.tv1.text = getString(R.string.error_city)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.locationButton.setOnClickListener(){getLastKnownLocation()}

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
            .addOnSuccessListener { location->
                if (location != null) {
                    lat =  location.latitude.toString()
                    lon = location.longitude.toString()
                    getDataByLat()

                }

            }

    }
    private fun getDataByLat() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!
                    cityName = weatherResponse.name!!
                    val stringBuilder = "Country: " +
                            weatherResponse.sys!!.country +
                            "\n" +
                            "Town: " + cityName +
                            "\n" +
                            "Temperature: " +
                            (weatherResponse.main!!.temp - 273.15).toInt().toString() +
                            "\n" +
                            "Temperature(Min): " +
                            (weatherResponse.main!!.temp_min - 273.15).toInt().toString() +
                            "\n" +
                            "Temperature(Max): " +
                            (weatherResponse.main!!.temp_max - 273.15).toInt().toString() +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main!!.humidity +
                            "\n" +
                            "Pressure: " +
                            weatherResponse.main!!.pressure

                    binding.tv1.text = stringBuilder

                }
                else {
                    binding.tv1.text = getString(R.string.error_city)
                }

            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding.tv1.text = t.message
            }
        })
    }
    private fun getDataByTown() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getTownWeatherData(cityName, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!
                    lat = weatherResponse.coord!!.lat.toString()
                    lon = weatherResponse.coord!!.lon.toString()

                    val stringBuilder = "Country: " +
                            weatherResponse.sys!!.country +
                            "\n" +
                            "Town: " + cityName +
                            "\n" +
                            "Temperature: " +
                            (weatherResponse.main!!.temp - 273.15).toInt().toString() +
                            "\n" +
                            "Temperature(Min): " +
                            (weatherResponse.main!!.temp_min - 273.15).toInt().toString() +
                            "\n" +
                            "Temperature(Max): " +
                            (weatherResponse.main!!.temp_max - 273.15).toInt().toString() +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main!!.humidity +
                            "\n" +
                            "Pressure: " +
                            weatherResponse.main!!.pressure


                    binding.tv1.text = stringBuilder
                }
                else {
                    binding.tv1.text = getString(R.string.error_city)
                }

            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding.tv1.text = t.message
            }
        })
    }

    companion object {
        var cityName = "Moscow"
        var BaseUrl = "http://api.openweathermap.org/"
        var AppId = "0656d14d3641e754d706c16afcf3b9f3"
        var lat = "35"
        var lon = "139"
    }
}


