package ru.andreyuniquename.theweatherapp.retrofit

import android.graphics.ColorSpace
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log
import android.util.Log

object RetrofitClient {
    private var retrofit: Retrofit?=null
    fun getClient(baseUrl:String):Retrofit{
        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        Log.d("Debugster", retrofit.toString())
        return retrofit!!
    }
}