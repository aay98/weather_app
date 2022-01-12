package ru.andreyuniquename.theweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import ru.andreyuniquename.theweatherapp.domain.CustomRecyclerAdapter
import ru.andreyuniquename.theweatherapp.retrofit.onecall.OneCallResponse
import java.util.Calendar.getInstance


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

        binding.citySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                getLastKnownLocation()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (binding.citySpinner.selectedItem) {
                    "Another" -> binding.inputLayout.visibility = View.VISIBLE
                    else -> {
                        cityName = binding.citySpinner.selectedItem.toString()
                        getDataByTown()
                    }
                }
            }
        }
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
        binding.myLocationImage.setOnClickListener(){getLastKnownLocation()}
        binding.recyclerViewWeek.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDay.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
                    binding.tv1.text = nowStringBuilder(weatherResponse.main!!.temp.toDouble(),weatherResponse.weather[0].description)
                    getToDayWeather()
                }
                else binding.tv1.text = getString(R.string.error_city)
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) { binding.tv1.text = t.message }
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
                    binding.tv1.text = nowStringBuilder(weatherResponse.main!!.temp.toDouble(),weatherResponse.weather[0].description)
                    getToDayWeather()

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
    private fun nowStringBuilder(temp : Double, desc :String?):String{
        return  cityName +
                    "\n" +
                    "t: " +
                    (temp-273.15).toInt().toString() +
                    "\n" +
                    desc
    }
    private fun hourStringBuilder(hour : String,temp: Double, desc :String?):String{
        return  hour + ":00" +
                "\n" +
                "t: " +
                temp.toInt().toString() +
                "\n" +
                desc
    }
    private fun fillList(): List<String> {
        val data = mutableListOf<String>()
        (0..30).forEach { i -> data.add("$i element") }
        return data
    }
    private fun getToDayWeather() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getOneCallData(lat, lon,exclude,units, AppId)

        val dateNow = Calendar.getInstance()
        val dateDay = Calendar.DAY_OF_MONTH
        val dateHour = Calendar.HOUR_OF_DAY
        Log.d("MyTag","dateHour = $dateHour")
        Log.d("MyTag","dateDay = $dateDay")
        Log.d("MyTag","dateNow = $dateNow")
        call.enqueue(object : Callback<OneCallResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<OneCallResponse>, response: Response<OneCallResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!
                    val dataDay = mutableListOf<String>()
                    val dataWeek = mutableListOf<String>()
                    (0..24).forEach { i -> dataDay.add((dateHour + i).toString() +":00" + "\n" + weatherResponse.hourly[i].temp.toString() + "\n" + weatherResponse.hourly[i].weather[0].description) }
                    (0..8).forEach { i -> dataWeek.add("$i element") }

                    binding.recyclerViewDay.adapter = CustomRecyclerAdapter(dataDay)
                    binding.recyclerViewWeek.adapter = CustomRecyclerAdapter(dataWeek)
                }
                else {
                    binding.tv1.text = getString(R.string.error_city)
                }

            }

            override fun onFailure(call: Call<OneCallResponse>?, t: Throwable?) {
                if (t != null) {
                    binding.tv1.text = t.message
                }
            }
        })
    }



    companion object {
        var cityName = "Moscow"
        var BaseUrl = "http://api.openweathermap.org/"
        var AppId = "0656d14d3641e754d706c16afcf3b9f3"
        var lat : String = "35"
        var lon : String = "139"
        const val units= "metric"
        const val exclude = "current,minutely,alerts"

    }
}






