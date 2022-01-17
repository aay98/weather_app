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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
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
// TODO если быстро кликать по этой кнопке, будет уходить куча одинаковых запросов. Подумай, что с этим можно сделать


// TODO ты уже задал LayoutManager в вёрстке, соответственно в коде его не нужно уже задавать
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding?  = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var viewModel : MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        viewModel = ViewModelProvider(this ,MainFactory(application,fusedLocationClient))
            .get(MyViewModel::class.java)

        binding!!.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                MyViewModel.getLastKnownLocation(fusedLocationClient)
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



    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }



}






