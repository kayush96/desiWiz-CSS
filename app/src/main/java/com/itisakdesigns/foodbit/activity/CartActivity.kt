package com.itisakdesigns.foodbit.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.adapter.CartAdapter
import com.itisakdesigns.foodbit.model.CartItems
import com.itisakdesigns.foodbit.utils.ConnectionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity() {
    //Declare Variables
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var txtOrderingFrom: TextView
    private lateinit var buttonPlaceOrder: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var menuAdapter: CartAdapter
    private lateinit var restaurantId: String
    private lateinit var restaurantName: String
    private lateinit var progressLayout: RelativeLayout
    private lateinit var selectedItemsId: ArrayList<String>

    var totalAmount = 0

    var cartListItems = arrayListOf<CartItems>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        buttonPlaceOrder = findViewById(R.id.btnConfirmOrder)
        txtOrderingFrom = findViewById(R.id.txtResName)
        toolbar = findViewById(R.id.toolbar)
        progressLayout = findViewById(R.id.progressLayout)

        restaurantId = intent.getStringExtra("restaurantId")
        restaurantName = intent.getStringExtra("restaurantName")
        selectedItemsId = intent.getStringArrayListExtra("selectedItemsId")

        txtOrderingFrom.text = restaurantName

        buttonPlaceOrder.setOnClickListener(View.OnClickListener {
            val sharedPreferences =
                this.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

            if(ConnectionManager().checkConnectivity(this)) {

                progressLayout.visibility = View.VISIBLE
                try {

                    val itemJsonArray = JSONArray()

                    for(foodItem in selectedItemsId) {
                        val singleItemObject = JSONObject()
                        singleItemObject.put("food_item_id", foodItem)
                        itemJsonArray.put(singleItemObject)
                    }

                    val sendOrder = JSONObject()

                    sendOrder.put("user_id", sharedPreferences.getString("user_id", "0"))
                    sendOrder.put("restaurant_id", restaurantId.toString())
                    sendOrder.put("total_cost", totalAmount)
                    sendOrder.put("food", itemJsonArray)

                    //Volley QUEUE Request
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://13.235.250.119/v2/place_order/fetch_result/"

                    val jsonObjectRequest = object : JsonObjectRequest(
                        Method.POST,
                        url,
                        sendOrder,
                        Response.Listener {
                            val responseJsonObjectData = it.getJSONObject("data")
                            val success = responseJsonObjectData.getBoolean("success")

                            if(success) {
                                Toast.makeText(
                                    this,
                                    "Order Placed",
                                    Toast.LENGTH_SHORT
                                ).show()

                                createNotification()

                                val intent = Intent(this, OrderPlacedActivity::class.java)
                                startActivity(intent)
                                //Destroy All Previous Activities
                                finishAffinity()
                            } else {

                                val responseMessageServer = responseJsonObjectData.getString("errorMessage")

                                Toast.makeText(this, responseMessageServer.toString(), Toast.LENGTH_SHORT).show()
                            }

                            progressLayout.visibility = View.INVISIBLE

                        },
                        Response.ErrorListener {
                            Toast.makeText(
                                this,
                                "Some Error occurred!!!",
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
                    jsonObjectRequest.retryPolicy = DefaultRetryPolicy(15000,
                        1 , 1f)

                    queue.add(jsonObjectRequest)

                } catch (e: JSONException){
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Some Unexpected error Occurred!!!",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else {
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
        })

        setToolBar()
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerCartItem)
    }

    private fun fetchData() {

        if(ConnectionManager().checkConnectivity(this)){
            progressLayout.visibility = View.VISIBLE
            //try catch block
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
                            val data = responseJsonObjectData.getJSONArray("data")

                            cartListItems.clear()
                            totalAmount = 0

                            for(i in 0 until data.length()){
                                val cartItemJsonObject = data.getJSONObject(i)

                                if(selectedItemsId.contains(cartItemJsonObject.getString("id"))) {

                                    val menuObject = CartItems(
                                        cartItemJsonObject.getString("id"),
                                        cartItemJsonObject.getString("name"),
                                        cartItemJsonObject.getString("cost_for_one"),
                                        cartItemJsonObject.getString("restaurant_id"))

                                    totalAmount += cartItemJsonObject.getString("cost_for_one")
                                        .toString().toInt()

                                    cartListItems.add(menuObject)

                                }

                                menuAdapter = CartAdapter(this, cartListItems)
                                //Bind RecyclerView to The Adapter
                                recyclerView.adapter = menuAdapter
                                //Bind the RecyclerView to the Layout manager
                                recyclerView.layoutManager = layoutManager
                            }

                            buttonPlaceOrder.text = "Pay(Total: Rs. "+ totalAmount + ")"
                        }

                        progressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

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
                Toast.makeText(this, "Some Unexpected error Occurred!!!",Toast.LENGTH_SHORT).show()
            }

        } else {
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
    }

    private fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(this)) {
            fetchData()//if internet is available fetch data
        }else
        {

            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection Not Found!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)
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

    fun createNotification() {
        val notificationId = 1
        val channelId = "personal_notification"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder.setSmallIcon(R.drawable.ic_app)
        notificationBuilder.setContentTitle("Order Placed")
        notificationBuilder.setContentText("Your Order has been successfully Placed!")
        notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(
            "Ordered From $restaurantName and Total Amount is Rs.$totalAmount"
        ))
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT

        val notificationManagerCompat= NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId,notificationBuilder.build())

        //For Oreo or Less Android OS
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            val name ="Order Placed"
            val description="Your Order has been successfully Placed!"
            val importance= NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel= NotificationChannel(channelId,name,importance)
            notificationChannel.description=description
            val notificationManager=  (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


}