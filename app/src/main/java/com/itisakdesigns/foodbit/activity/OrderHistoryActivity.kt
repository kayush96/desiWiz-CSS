package com.itisakdesigns.foodbit.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.adapter.OrderHistoryAdapter
import com.itisakdesigns.foodbit.model.OrderHistoryRestaurant
import com.itisakdesigns.foodbit.utils.ConnectionManager
import kotlinx.android.synthetic.main.activity_order_history.*
import org.json.JSONException
import java.util.ArrayList

class OrderHistoryActivity : AppCompatActivity() {

    //Declaring Variables
    lateinit var layoutManager1: RecyclerView.LayoutManager
    lateinit var menuAdapter1: OrderHistoryAdapter
    lateinit var recyclerAllOrders: RecyclerView
    private lateinit var toolbar: Toolbar
    lateinit var progressLayout: RelativeLayout
    lateinit var noOrders: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerAllOrders = findViewById(R.id.recyclerOrderHistory)

        toolbar = findViewById(R.id.toolbar)

        progressLayout = findViewById(R.id.progressLayout)

        noOrders = findViewById(R.id.rlNoOrders)

        setToolBar()
    }


    private fun setItemsForEachRestaurant() {
        layoutManager1 = LinearLayoutManager(this)

        val orderedRestaurantList = ArrayList<OrderHistoryRestaurant>()

        val sharedPreferences =
            this.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val userId = sharedPreferences.getString("user_id", "000")

        if(ConnectionManager().checkConnectivity(this)) {

            progressLayout.visibility = View.VISIBLE

            try {
                //Volley queue request
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/orders/fetch_result/"

                val jsonObjectRequest = object: JsonObjectRequest(
                    Method.GET,
                    url + userId,
                    null,
                    Response.Listener {
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if(success) {

                            val resArray = responseJsonObjectData.getJSONArray("data")
                            if(resArray.length() == 0) {
                                Toast.makeText(this, "No Orders Placed Yet!", Toast.LENGTH_SHORT).show()

                                noOrders.visibility = View.VISIBLE

                            } else {
                                noOrders.visibility = View.INVISIBLE

                                for(i in 0 until resArray.length()) {
                                    val restaurantItemJsonObject = resArray.getJSONObject(i)

                                    val eachRestaurantObject = OrderHistoryRestaurant(
                                        restaurantItemJsonObject.getString("order_id"),
                                        restaurantItemJsonObject.getString("restaurant_name"),
                                        restaurantItemJsonObject.getString("total_cost"),
                                        restaurantItemJsonObject.getString("order_placed_at").substring(0,10)
                                    )

                                    orderedRestaurantList.add(eachRestaurantObject)
                                    //Setting Adapter with Data
                                    menuAdapter1 = OrderHistoryAdapter(this, orderedRestaurantList)
                                    recyclerAllOrders.adapter = menuAdapter1
                                    //Bind RecyclerView To Layout manager
                                    recyclerAllOrders.layoutManager = layoutManager1
                                }
                            }

                        }

                        progressLayout.visibility = View.INVISIBLE

                    },
                    Response.ErrorListener {

                        progressLayout.visibility = View.INVISIBLE

                        Toast.makeText(
                            this,
                            "Some Error Occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "254d7c5e450ec4"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException){
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Some Unexpected Error Occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection Not Found!")
            alterDialog.setPositiveButton("Open Settings") {text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                //closes all the instances of the app and the app closes completely
                finishAffinity()
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
    }

    private fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Order History"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(this)) {
            //If internet is available then fetch data
            setItemsForEachRestaurant()
        }else
        {

            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection Not Found!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                //closes all the instances of the app and the app closes completely
                finishAffinity()
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
        super.onResume()
    }

}