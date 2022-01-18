package ru.andreyuniquename.theweatherapp

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import ru.andreyuniquename.theweatherapp.model.MainFactory
import ru.andreyuniquename.theweatherapp.model.MyViewModel

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding?  = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var viewModel : MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        viewModel = ViewModelProvider(this, MainFactory(application))
            .get(MyViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.mainInfoLiveData.observe(this, Observer {
            binding!!.mainInfo.text = viewModel.mainInfoLiveData.value
        })
        viewModel.weekLiveData.observe(this, Observer {
            if (viewModel.weekLiveData.value == null)
                Toast.makeText(applicationContext, ERROR_IN_WEEK_DATA, Toast.LENGTH_SHORT).show()
            else binding!!.recyclerViewWeek.adapter = CustomRecyclerAdapter(viewModel.weekLiveData.value!!)
        })
        viewModel.dayLiveData.observe(this, Observer {
            if (viewModel.dayLiveData.value == null)
                Toast.makeText(applicationContext, ERROR_IN_DAY_DATA, Toast.LENGTH_SHORT).show()
            else binding!!.recyclerViewDay.adapter = CustomRecyclerAdapter(viewModel.dayLiveData.value!!)
        })


        binding!!.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.getLastKnownLocation(fusedLocationClient)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (binding!!.citySpinner.selectedItem) {
                    "Another" -> binding!!.inputLayout.visibility = View.VISIBLE //Через константы не работает
                    else -> {
                        cityName = binding!!.citySpinner.selectedItem.toString()
                        viewModel.getDataByTown(cityName)
                    }
                }
            }
        }
        binding!!.inputButton.setOnClickListener(){
            if (binding!!.inputText.text != null) {
                MyViewModel.cityName = binding!!.inputText.text.toString()
                binding!!.inputLayout.visibility = View.GONE
                cityName = binding!!.inputText.text.toString()
                viewModel.getDataByTown(cityName)
            } else binding!!.mainInfo.text = R.string.error_city.toString()
        }

        var rightNow = Calendar.getInstance()
        var time : Int = rightNow.timeInMillis.toInt() - TIME_OUT

        binding!!.myLocationImage.setOnClickListener() {
            rightNow = Calendar.getInstance()
            var newTime = rightNow.timeInMillis.toInt()
            Log.d("MyTag","time is $time, new time is $newTime")
            if (time.toInt() + TIME_OUT < newTime) {
                viewModel.getLastKnownLocation(fusedLocationClient)
                time = newTime
            }
            else{
                Toast.makeText(applicationContext,TO_MUCH_CLICKS,Toast.LENGTH_SHORT).show()
                Log.d("MyTag","last was in $time, but now only $newTime it means that was only ${(newTime-time)/1000} sec")
                viewModel.getOldData()


            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private var cityName = R.string.defCityName.toString()
        //private const val ANOTHER = R.string.another.toString()
        private const val TIME_OUT : Int = 60000
        private const val TO_MUCH_CLICKS : String = R.string.toMuchClicks.toString()
        private const val ERROR_IN_WEEK_DATA : String = R.string.noWeekData.toString()
        private const val ERROR_IN_DAY_DATA : String = R.string.noDayData.toString()
    }

}






