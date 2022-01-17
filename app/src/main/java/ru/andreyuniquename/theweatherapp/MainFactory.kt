package ru.andreyuniquename.theweatherapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class MainFactory (val application: Application, var fusedLocationClient: FusedLocationProviderClient) :
ViewModelProvider.AndroidViewModelFactory(application){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyViewModel(application, fusedLocationClient) as T
    }
}