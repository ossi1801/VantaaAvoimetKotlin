package com.example.kotlinprojekti

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*



/**
 Created by ossi1801 (Ossi H) on 28/2/2021.
 App that lets you search for available jobs in the Vantaa city area.
 Uses retrofit library

 IMPORTANT -------------> The JSON is not maintained regularly by Vantaa, so a lot of jobs are in "HAKU ON PÄÄTTYNYT" state
 -----------> In this case use diffrent keyword for a job that has jobs available example:  http://prntscr.com/109p7fr
*/
class MainActivity : AppCompatActivity() {

    private var text: TextView? = null
    private var tyotehtavaHolder: TextView? = null
    private var ammattialaHolder: TextView? = null

    private var hakupaattyyHolder: TextView? = null
    private var osoiteHolder: TextView? = null

    private var maxpagesize: TextView? = null
    var tallennettuobj: List<MyResponse>? = null
    var counter: Int = 0
    var tyotietoURL: String? = null
    var ptypvm: String? = null
    var jobHasEnded: Boolean = false
    val sdf = SimpleDateFormat("yyyy-M-dd")
   // val currentDate = sdf.format(Date()) //2021-2-2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*
*TODO
* Unfocus and hide keyboard
* layout http://prntscr.com/108rozc
* http://gis.vantaa.fi/rajapinnat/rest_tyopaikat.html more inputs
* Parse datetime
* Documentation doc
*/
      //  text = findViewById(R.id.textView)
        //holderit texteille
        tyotehtavaHolder = findViewById(R.id.tyotehtavaHolder)
        ammattialaHolder = findViewById(R.id.ammattialaHolder)
        hakupaattyyHolder = findViewById(R.id.hakupaattyyHolder)
        osoiteHolder = findViewById(R.id.osoiteHolder)

        //----
        maxpagesize = findViewById(R.id.maxpagesize)
        var pagenro = findViewById<TextView>(R.id.pagenumber)

        //----------------------------
        val textView = findViewById(R.id.SearchField) as AutoCompleteTextView  //auto complete function
        val tyopaikat: Array<out String> = resources.getStringArray(R.array.tyopaikat_array)
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tyopaikat).also { adapter -> textView.setAdapter(adapter) }
        //------------------------------
        findViewById<View>(R.id.button).setOnClickListener {//Button click get
            var haku = textView.text.toString()
            getCurrentData(haku)

        }

        //---------- Page movement buttons
        findViewById<View>(R.id.buttonForward).setOnClickListener{
            if (tallennettuobj.isNullOrEmpty()) tallennettuobj = null else pageMovement(tallennettuobj!!,true)
            pagenro.text = counter.toString()
        }
        findViewById<View>(R.id.buttonBackwards).setOnClickListener{
            if (tallennettuobj.isNullOrEmpty()) tallennettuobj = null else pageMovement(tallennettuobj!!,false)
            pagenro.text = counter.toString()
        }
        //--------- Page movement buttons

        findViewById<View>(R.id.lisatietojabutton).setOnClickListener{
            if (jobHasEnded || tyotietoURL.isNullOrEmpty() ){
                Toast.makeText(getApplicationContext(),"Ei lisätietoja",Toast.LENGTH_LONG).show()
            }
            else openNewTabWindow(tyotietoURL!!,this@MainActivity)
        }


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

      // val call: Call<MyResponse> = service.getCurrentCityData(extraURL)
      //  val call = service.getCurrentCityData(extraURL)

        //val call = service.getCurrentCityData(x, y, AmmattiAla)
     //   call.enqueue()
        call.enqueue(object : Callback<List<MyResponse>> {

            override fun onResponse(call: Call<List<MyResponse>>, response: Response<List<MyResponse>>)  {
                if (response.code() == 200) {
                    tallennettuobj = response.body()!!
                    val myResponse  = response.body()!!
                    var maxNro =  myResponse.size-1
                    maxpagesize!!.text =  maxNro.toString()  //SIVU NRO 1/133

                    ammattialaHolder!!.text = "Ammattiala "+myResponse[0]!!.ammattiala
                    tyotehtavaHolder!!.text = "Työkuvaus: \n \n"+myResponse[0]!!.tyotehtava
                    hakupaattyyHolder!!.text = "Haku päättyy: "+myResponse[0]!!.haku_paattyy_pvm
                    osoiteHolder!!.text = myResponse[0]!!.osoite
                    tyotietoURL = myResponse[0]!!.linkki
                    ptypvm = myResponse[0]!!.haku_paattyy_pvm
                    checkJobDate(ptypvm,Date())
                        if(jobHasEnded){
                            tyotietoURL = null
                            hakupaattyyHolder!!.text = "HAKU ON PÄÄTTYNYT"
                        }
                        else {
                            tyotietoURL = myResponse[0]!!.linkki
                            hakupaattyyHolder!!.text = ptypvm
                        }
                }
            }
            override fun onFailure(call: Call<List<MyResponse>>, t: Throwable) {
                text!!.text = t.message
            }
        })
    }



    fun pageMovement(tallennettuobj: List<MyResponse>,isPlus: Boolean){
        if (isPlus) counter ++ else counter--
        if(counter>tallennettuobj.size-1) counter = 1   // 133/133 >   = 1/133
        if (counter<1) counter = tallennettuobj.size-1  // 1/133 <   = 133/133

        ammattialaHolder!!.text ="Ammattiala "+tallennettuobj[counter]!!.ammattiala
        tyotehtavaHolder!!.text = "Työkuvaus: \n \n"+tallennettuobj[counter]!!.tyotehtava
        osoiteHolder!!.text = tallennettuobj[counter]!!.osoite
        ptypvm = tallennettuobj[counter]!!.haku_paattyy_pvm

        checkJobDate(ptypvm,Date())
           if(jobHasEnded){
               tyotietoURL = null
               hakupaattyyHolder!!.text = "HAKU ON PÄÄTTYNYT"
           }
           else {
               tyotietoURL = tallennettuobj[counter]!!.linkki
               hakupaattyyHolder!!.text = "Haku päättyy: "+ptypvm
           }
    }
    fun openNewTabWindow(urls: String, context : Context) {

        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        context.startActivity(intents)
    }
    fun checkJobDate(pvm: String?, currentDate: Date){
       if (currentDate.after(sdf.parse(pvm))) {
           jobHasEnded = true
           Log.d("date",pvm.toString()+currentDate)
       } else jobHasEnded = false


    }

    companion object {
        var BaseUrl = "http://gis.vantaa.fi/rest/tyopaikat/v1/"

        var extraURL = "kaikki"
        var x = "25.0527573018971"
        var y = "60.3018360201365"

    }

}
