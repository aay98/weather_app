package ru.andreyuniquename.theweatherapp.retrofit.weather

import com.google.gson.annotations.SerializedName

class Rain {
    @SerializedName("3h")
    var h3: Float = 0.toFloat()
}