package com.example.kotlinprojektiossih

import com.google.gson.annotations.SerializedName

data class MyResponse (
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("organisaatio")
    val organisaatio: String? = null,
    @SerializedName("ammattiala")
    val ammattiala : String? = null,
    @SerializedName("tyotehtava")
    val tyotehtava: String? = null,
    @SerializedName("tyoavain")
    val tyoavain: String? = null,
    @SerializedName("osoite")
    val osoite: String? = null,
    @SerializedName("haku_paattyy_pvm")
    val haku_paattyy_pvm: String? = null ,
    @SerializedName("x")
    val x: Float = 0f,
    @SerializedName("y")
    val y: Float = 0f,
    @SerializedName("linkki")
    val linkki: String? = null
)