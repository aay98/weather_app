package ru.andreyuniquename.theweatherapp.retrofit.common

import ru.andreyuniquename.theweatherapp.`interface`.RetrofitServices
import ru.andreyuniquename.theweatherapp.retrofit.RetrofitClient

object Common {
    /* SHOULD BE
    BASE_URL = "https://api.openweathermap.org/data/2.5/"
    */
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=Orenburg&appid=0656d14d3641e754d706c16afcf3b9f3"
    val retrofitServices: RetrofitServices
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}