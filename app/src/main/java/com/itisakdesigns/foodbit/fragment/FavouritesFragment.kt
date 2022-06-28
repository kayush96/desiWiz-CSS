package com.itisakdesigns.foodbit.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.adapter.HomeRecyclerAdapter
import com.itisakdesigns.foodbit.database.RestaurantDatabase
import com.itisakdesigns.foodbit.database.RestaurantEntity
import com.itisakdesigns.foodbit.model.Restaurant
import com.itisakdesigns.foodbit.utils.ConnectionManager
import org.json.JSONException

class FavouritesFragment(val contextParams: Context) : Fragment() {
    //Declaring Views
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favouriteAdapter: HomeRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var noFavourites: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_favourites, container, false)
        //Set the Layout manager
        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerRestaurant)
        progressLayout = view.findViewById(R.id.progressLayout)
        noFavourites = view.findViewById(R.id.rlNoFavourites)
        return view
    }

   private fun fetchData() {

       if(ConnectionManager().checkConnectivity(activity as Context)) {
           progressLayout.visibility = View.VISIBLE
           //try catch block
           try {
               //Volley Queue Request
               val queue = Volley.newRequestQueue(activity as Context)
               val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

               val jsonObjectRequest = object : JsonObjectRequest(
                   Method.GET,
                   url,
                   null,
                   Response.Listener {

                       val responseJsonObjectData = it.getJSONObject("data")
                       val success = responseJsonObjectData.getBoolean("success")

                       if(success) {
                           restaurantInfoList.clear()

                           val resArray = responseJsonObjectData.getJSONArray("data")

                           for(i in 0 until resArray.length()) {
                               val restaurantJsonObject = resArray.getJSONObject(i)

                               val restaurantEntity = RestaurantEntity(
                                   restaurantJsonObject.getString("id"),
                                   restaurantJsonObject.getString("name")
                               )
                               if(DBAsyncTask(contextParams, restaurantEntity, 1).execute().get()) {
                                   val restaurantObject = Restaurant(
                                       restaurantJsonObject.getString("id"),
                                       restaurantJsonObject.getString("name"),
                                       restaurantJsonObject.getString("rating"),
                                       restaurantJsonObject.getString("cost_for_one"),
                                       restaurantJsonObject.getString("image_url")
                                   )
                                   restaurantInfoList.add(restaurantObject)

                                   favouriteAdapter = HomeRecyclerAdapter(activity as Context, restaurantInfoList)
                                   //Bind Recycler View to Adapter
                                   recyclerView.adapter = favouriteAdapter
                                   //Bind the Recycler View to Layout manager
                                   recyclerView.layoutManager = layoutManager
                               }

                           }

                           if(restaurantInfoList.size == 0) {
                               noFavourites.visibility = View.VISIBLE
                           }
                       }
                       progressLayout.visibility = View.INVISIBLE

                   },Response.ErrorListener {
                       Toast.makeText(
                           activity as Context,
                           "Volley Error Occurred!!!",
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
               Toast.makeText(
                   activity as Context,
                   "Some Error Occurred!!!",
                   Toast.LENGTH_SHORT
               ).show()
           }
       } else {

           val alterDialog=androidx.appcompat.app.AlertDialog.Builder(activity as Context)
           alterDialog.setTitle("No Internet")
           alterDialog.setMessage("Internet Connection can't be establish!")
           alterDialog.setPositiveButton("Open Settings"){text,listener->
               val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
               startActivity(settingsIntent)
           }

           alterDialog.setNegativeButton("Exit"){ text,listener->
               ActivityCompat.finishAffinity(activity as Activity)//closes all the instances of the app and the app closes completely
           }
           alterDialog.setCancelable(false)

           alterDialog.create()
           alterDialog.show()
       }
   }

    class  DBAsyncTask(val context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int)
        : AsyncTask<Void,Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {

            when(mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao().getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null

                } else -> return false
            }
        }
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            //If Internet is available, fetch data
            fetchData()
        }else
        {

            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                ActivityCompat.finishAffinity(activity as Activity)//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

        super.onResume()
    }

}