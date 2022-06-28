package com.itisakdesigns.foodbit.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.adapter.RestaurantMenuRecyclerAdapter
import com.itisakdesigns.foodbit.model.RestaurantMenu
import com.itisakdesigns.foodbit.utils.ConnectionManager
import org.json.JSONException

class RestaurantMenuActivity() : AppCompatActivity() {
    //Declaring Variables
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: RestaurantMenuRecyclerAdapter
    lateinit var restaurantId: String
    lateinit var restaurantName: String
    lateinit var proceedToCartLayout: RelativeLayout
    lateinit var buttonProceedToCart: Button
    lateinit var progressLayout: RelativeLayout
    private lateinit var toolbar: Toolbar

    var restaurantMenuList = arrayListOf<RestaurantMenu>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        proceedToCartLayout = findViewById(R.id.proceedToCartLayout)
        buttonProceedToCart = findViewById(R.id.btnGoToCart)
        progressLayout = findViewById(R.id.progressLayout)

        toolbar = findViewById(R.id.toolbar)

        restaurantId = intent.getStringExtra("restaurantId")

        restaurantName = intent.getStringExtra("restaurantName")

        setToolbar()

        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerMenuItems)

    }

    private fun fetchData() {

        if(ConnectionManager().checkConnectivity(this)) {

            progressLayout.visibility = View.VISIBLE

            try {
                val queue = Volley.newRequestQueue(this)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url + restaurantId,
                    null,
                    Response.Listener {
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if(success) {
                            restaurantMenuList.clear()

                            val resArray = responseJsonObjectData.getJSONArray("data")

                            for(i in 0 until resArray.length()) {
                                val itemJsonObject = resArray.getJSONObject(i)
                                val menuObject = RestaurantMenu(
                                    itemJsonObject.getString("id"),
                                    itemJsonObject.getString("name"),
                                    itemJsonObject.getString("cost_for_one")
                                )

                                restaurantMenuList.add(menuObject)

                                //Set Adapter with Data
                                menuAdapter = RestaurantMenuRecyclerAdapter(
                                    this,
                                    restaurantId,
                                    restaurantName,
                                    proceedToCartLayout,
                                    buttonProceedToCart,
                                    restaurantMenuList
                                )
                                //Bind RecyclerView to Adapter
                                recyclerView.adapter = menuAdapter
                                //Bind RecyclerView to LayoutManager
                                recyclerView.layoutManager = layoutManager
                            }

                        }
                        progressLayout.visibility = View.INVISIBLE

                    },
                    Response.ErrorListener {
                        Toast.makeText(this, "Volley Error Occurred!!!", Toast.LENGTH_SHORT).show()

                        progressLayout.visibility = View.INVISIBLE

                    }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "254d7c5e450ec4"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(this, "Some Unexpected Error Occurred!!!", Toast.LENGTH_SHORT).show()

            }
        } else {
            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection Not Available!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = restaurantName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    override fun onBackPressed() {
        if(menuAdapter.getSelectedItemCount()>0) {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will Empty the Cart!")
            alterDialog.setPositiveButton("Okay") { text, listener ->
                super.onBackPressed()
            }
            alterDialog.setNegativeButton("No") { text, listener ->

            }
            alterDialog.show()
        }
        else {
            super.onBackPressed()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home->{
                if(menuAdapter.getSelectedItemCount()>0) {

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will Empty The Cart!")
                    alterDialog.setPositiveButton("Okay") { text, listener ->
                        super.onBackPressed()
                    }
                    alterDialog.setNegativeButton("No") { text, listener ->

                    }
                    alterDialog.show()
                }else{
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(this)) {
            if(restaurantMenuList.isEmpty())
            //If internet is available then fetch data
                fetchData()
        }
        else
        {
            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
        super.onResume()
    }
}