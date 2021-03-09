package com.example.kotlinprojekti


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import android.util.Log
import java.util.*

interface Service {
    // @GET("data/2.5/weather?")

    @GET("{extraURL}")

    fun getCurrentCityData(@Path("extraURL") extraURL: String): Call<List<MyResponse>>

}