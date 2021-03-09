package com.example.kotlinprojektiossih

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface Service {
    @GET("{extraURL}")
    fun getCurrentCityData(@Path("extraURL") extraURL: String): Call<List<MyResponse>>
}