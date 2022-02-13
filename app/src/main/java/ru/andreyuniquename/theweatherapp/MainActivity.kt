package ru.andreyuniquename.theweatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ru.andreyuniquename.theweatherapp.databinding.ActivityMainBinding
import ru.andreyuniquename.theweatherapp.model.MainFactory
import ru.andreyuniquename.theweatherapp.model.MyViewModel

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        viewModel = ViewModelProvider(this, MainFactory(application))
            .get(MyViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        var cityName: String
        val TIME_OUT = 60000
        val TO_MUCH_CLICKS: String = resources.getText(R.string.toMuchClicks).toString()
        val ERROR_IN_WEEK_DATA: String = resources.getText(R.string.noWeekData).toString()
        val ERROR_IN_DAY_DATA: String = resources.getText(R.string.noDayData).toString()
        val error_city = resources.getText(R.string.error_city).toString()
        val another = resources.getText(R.string.another).toString()
        val noPermission = resources.getText(R.string.noPermission).toString()

        // TODO проверка на null
        viewModel.mainInfoLiveData.observe(
            this
        ) {
            binding!!.mainInfo.text = viewModel.mainInfoLiveData.value
        }
        viewModel.weekLiveData.observe(
            this
        ) {
            if (viewModel.weekLiveData.value == null)
                Toast.makeText(applicationContext, ERROR_IN_WEEK_DATA, Toast.LENGTH_SHORT).show()
            else binding!!.recyclerViewWeek.adapter =
                CustomRecyclerAdapter(viewModel.weekLiveData.value!!)
        }
        viewModel.dayLiveData.observe(
            this
        ) {
            if (viewModel.dayLiveData.value == null)
                Toast.makeText(applicationContext, ERROR_IN_DAY_DATA, Toast.LENGTH_SHORT).show()
            else binding!!.recyclerViewDay.adapter =
                CustomRecyclerAdapter(viewModel.dayLiveData.value!!)
        }
        // TODO сделать ошибку
        viewModel.errorLiveData.observe(
            this
        ) {
            if (viewModel.errorLiveData.value == null)
                Toast.makeText(applicationContext, ERROR_IN_DAY_DATA, Toast.LENGTH_SHORT).show()
            else Toast.makeText(
                applicationContext,
                viewModel.errorLiveData.value,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding!!.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.getOldData()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (binding!!.citySpinner.selectedItem) {
                    another ->
                        binding!!.inputLayout.visibility =
                            View.VISIBLE
                    else -> {
                        binding!!.inputLayout.visibility = View.GONE
                        cityName = binding!!.citySpinner.selectedItem.toString()
                        viewModel.getDataByTown(cityName)
                    }
                }
            }
        }
        binding!!.inputButton.setOnClickListener {
            if (binding!!.inputText.text != null) {
                MyViewModel.cityName = binding!!.inputText.text.toString()
                binding!!.inputLayout.visibility = View.GONE
                cityName = binding!!.inputText.text.toString()
                viewModel.getDataByTown(cityName)
            } else {
                Toast.makeText(applicationContext, error_city, Toast.LENGTH_SHORT).show()
            }
        }

        var rightNow = Calendar.getInstance()
        var time: Int = rightNow.timeInMillis.toInt() - TIME_OUT
        binding!!.myLocationImage.setOnClickListener {
            rightNow = Calendar.getInstance()
            val newTime = rightNow.timeInMillis.toInt()
            Log.d("MyTag", "time is $time, new time is $newTime")
            if (time + TIME_OUT < newTime) {
                if (!checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !checkSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    val permList = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    Log.d("MyTag", "permission deny")
                    Toast.makeText(applicationContext, noPermission, Toast.LENGTH_SHORT).show()
                    requestPermissions(permList, REQUEST_LOCATION_PERMISSION)
                }
                else{
                    Log.d("MyTag", "permission ok")
                    viewModel.getLastKnownLocation(fusedLocationClient)
                    time = newTime
                }

            } else {
                Toast.makeText(applicationContext, TO_MUCH_CLICKS, Toast.LENGTH_SHORT).show()
                Log.d(
                    "MyTag",
                    "last was in $time, but now only $newTime it means that was only ${(newTime - time) / 1000} sec"
                )
                viewModel.getOldData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    private fun Context.checkSinglePermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

}
