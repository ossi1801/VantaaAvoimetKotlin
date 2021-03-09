package com.example.kotlinprojektiossih


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
Created by ossi1801 (Ossi H) on 7/3/2021.
App that lets you search for available jobs in the Vantaa city area.
Uses retrofit library

IMPORTANT -------------> The JSON is not maintained regularly by Vantaa, so a lot of jobs are in "HAKU ON PÄÄTTYNYT" state
-----------> In this case use different keyword for a job that has jobs available example:  http://prntscr.com/10hf0yu
 */

class MainActivity : AppCompatActivity() {
    private val  formattedlist = initializeListView()
    private val adapter = RecycleAdapter(formattedlist)
    var textView: AutoCompleteTextView? = null  //auto complete function
  //  var isSearchFirstTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
        //----------------------------
         textView = findViewById(R.id.SearchField) as AutoCompleteTextView  //auto complete function
        val tyopaikat: Array<out String> = resources.getStringArray(R.array.tyopaikat_array)
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tyopaikat).also { adapter -> textView!!.setAdapter(adapter) }
        //------------------------------
    }
    /*TODO Fix Search bar  limits  (or do radiobuttonlist)
    * Clikable list
    * Format list better or stmh  xml
    */


    fun haeData(view: View) {
        val haku = textView?.text.toString()
        getCurrentData(haku)
    }

    fun removeItem(view: View) {}

    private fun initializeListView(): ArrayList<RecycleItem> {
        val list = ArrayList<RecycleItem>()
            val item = RecycleItem( "", "", "")
            list += item
        return list
    }

    fun getCurrentData(hakusana: String) {
        var hakusana = hakusana
        Log.d("TAG", hakusana)
        if(hakusana.isNullOrEmpty()) hakusana=extraURL

        val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(Service::class.java)
        val call: Call<List<MyResponse>> = service.getCurrentCityData(hakusana)
        call.enqueue(object : Callback<List<MyResponse>> {
            override fun onResponse(call: Call<List<MyResponse>>, response: Response<List<MyResponse>>)  {

                if (response.code() == 200) {
                    clearData() //poistaa vanhan haun datan
                   var tallennettuobj = response.body()!!
                    var maxNro =  tallennettuobj.size-1
                  //  if (isSearchFirstTime == false) { } //poistaa vanhan haun datan
                    val sdf = SimpleDateFormat("yyyy-M-dd")
                    for (i in 0 until maxNro) {
                        val item: RecycleItem
                        if (Date().after(sdf.parse(tallennettuobj[i].haku_paattyy_pvm))) { // If current date after job ending date
                             item = RecycleItem(tallennettuobj[i].tyotehtava.toString(), "id $i", "HAKU ON PÄÄTTYNYT")
                        }
                        else {
                            item = RecycleItem(tallennettuobj[i].tyotehtava.toString(), "id $i", tallennettuobj[i].haku_paattyy_pvm.toString())
                        }
                        formattedlist.add(i, item)
                        adapter.notifyItemInserted(i)
                    }
                    //isSearchFirstTime = false
                }
            }
            override fun onFailure(call: Call<List<MyResponse>>, t: Throwable) { Log.d("ERROR", t.message.toString()) }

        })
    }
    fun clearData() {
        formattedlist.clear() // clear list
        adapter.notifyDataSetChanged() // let your adapter know about the changes and reload view.
    }


    companion object {
        var BaseUrl = "http://gis.vantaa.fi/rest/tyopaikat/v1/"
        var extraURL = "kaikki"
        var x = "25.0527573018971"
        var y = "60.3018360201365"

    }



}