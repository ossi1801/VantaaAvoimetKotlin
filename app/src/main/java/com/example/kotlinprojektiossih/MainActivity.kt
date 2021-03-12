package com.example.kotlinprojektiossih


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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

/** IMPORTANT! README!
Created by ossi1801 (Ossi H)
App that lets you search for available jobs in the Vantaa city area.
Uses retrofit library

 IMPORTANT! ------------->  12/3/2021. The JSON is not maintained regularly by Vantaa, so a lot of jobs are in "HAKU ON PÄÄTTYNYT" state
-----------> In this case use different keyword for a job title that has jobs available, or use the checkbox filter
Example links (images):
 Expired date:
    http://prntscr.com/10j5w05
    http://prntscr.com/10j5tdg
 Working dates:
    http://prntscr.com/10j5x4n
    http://prntscr.com/10j5y30
 */

class MainActivity : AppCompatActivity(), RecycleAdapter.OnItemClickListener {
    private val  formattedlist = initializeListView()
    private val adapter = RecycleAdapter(formattedlist, this)
    var textView: AutoCompleteTextView? = null  //auto complete function
    var tallennettuobj: List<MyResponse>? = null
    val sdf = SimpleDateFormat("yyyy-M-dd")
    var isFilterToggled: Boolean = false


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


    fun haeData(view: View) {
        val haku = textView?.text.toString()
        getCurrentData(haku)
    }


    override fun onItemClick(position: Int) {
        if (tallennettuobj.isNullOrEmpty()) return

        var position = position
        if (isFilterToggled) position = formattedlist[position].text2.toInt()
        if (Date().after(sdf.parse(tallennettuobj!![position].haku_paattyy_pvm))) { // If current date after job ending date
            Toast.makeText(this, "HAKU ON PÄÄTTYNYT, \n EI LISÄTIETOJA", Toast.LENGTH_SHORT).show()
        }
        else{ var url =  tallennettuobj!![position].linkki.toString()
            openNewTabWindow(url)
        }
    }
    fun openNewTabWindow(urls: String) {
        if (urls.isNullOrEmpty()) return
        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        this@MainActivity.startActivity(intents)
    }

    fun setChecked(view: View) {
        if (isFilterToggled == false) isFilterToggled = true
        else if (isFilterToggled == true) isFilterToggled = false
        Log.d("isfiltered: ", isFilterToggled.toString())
        haeData(view)
    }

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
                    clearData() //clears data object
                    tallennettuobj = response.body()!!
                    var maxNro = tallennettuobj?.size?.minus(1)
                    for (i in 0 until maxNro!!) {
                        val item: RecycleItem
                        if(isFilterToggled ) {
                            if (sdf.parse(tallennettuobj!![i].haku_paattyy_pvm).after(Date())) {
                                var bI = 0
                                item = RecycleItem(tallennettuobj!![i].tyotehtava.toString(), "$i", tallennettuobj!![i].haku_paattyy_pvm.toString())
                                formattedlist.add(bI, item)
                                bI++
                            }
                        }
                        else {
                            if (Date().after(sdf.parse(tallennettuobj!![i].haku_paattyy_pvm))) { // If current date after job ending date
                                item = RecycleItem(tallennettuobj!![i].tyotehtava.toString(), "id $i", "HAKU ON PÄÄTTYNYT")
                            }
                            else {
                                item = RecycleItem(tallennettuobj!![i].tyotehtava.toString(), "id $i", tallennettuobj!![i].haku_paattyy_pvm.toString())
                            }
                            formattedlist.add(i, item)
                        }
                        adapter.notifyItemInserted(i)
                    }
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