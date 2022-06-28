package com.itisakdesigns.foodbit.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.adapter.HomeRecyclerAdapter
import com.itisakdesigns.foodbit.model.Restaurant
import com.itisakdesigns.foodbit.utils.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment(private val contextParams: Context) : Fragment() {

    //Declaring Variables
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var homeAdapter: HomeRecyclerAdapter
    lateinit var etSearch: EditText
    lateinit var radioButtonView: View
    lateinit var progressLayout: RelativeLayout
    lateinit var cant_find_restaurant: RelativeLayout


    var restaurantInfoList = arrayListOf<Restaurant>()

    private var ratingComparator = Comparator<Restaurant> { restaurant1, restaurant2 ->

        if(restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true)==0){
            restaurant1.restaurantName.compareTo(restaurant2.restaurantName, true)
        } else {
            restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true)
        }

    }

    private var costComparator = Comparator<Restaurant> { restaurant1, restaurant2 ->
        restaurant1.cost_for_one.compareTo(restaurant2.cost_for_one, true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        
        layoutManager = LinearLayoutManager(activity)

        recyclerView = view.findViewById(R.id.recyclerHome)
        etSearch = view.findViewById(R.id.etSearch)
        progressLayout = view.findViewById(R.id.progressLayout)

        cant_find_restaurant = view.findViewById(R.id.rlCant_Find_Restaurant)

        fun filterFun(strTyped: String) {
            val filteredList = arrayListOf<Restaurant>()

            for(item in restaurantInfoList) {
                if(item.restaurantName.toLowerCase().contains(strTyped.toLowerCase()))
                    filteredList.add(item)
            }

            if(filteredList.size == 0){
                cant_find_restaurant.visibility = View.VISIBLE
            } else {

                cant_find_restaurant.visibility = View.INVISIBLE
            }
            homeAdapter.filterList(filteredList)
        }

        //As user types,the search filter is applied
        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        return view
    }

    private fun fetchData() {
        if(ConnectionManager().checkConnectivity(activity as Context)) {
            progressLayout.visibility = View.VISIBLE
            //Try Catch Block
            try {

                val queue = Volley.newRequestQueue(activity as Context)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {
                        println("Response:$it")

                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if(success) {
                            val data = responseJsonObjectData.getJSONArray("data")

                            for(i in 0 until data.length()){
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )

                                restaurantInfoList.add(restaurantObject)
                                //Set the adapter with Data
                                homeAdapter = HomeRecyclerAdapter(
                                    activity as Context,
                                    restaurantInfoList)

                                recyclerView.adapter = homeAdapter // Bind RecyclerView to Adapter

                                recyclerView.layoutManager = layoutManager //Bind RecyclerView to LayoutManager

                            }

                        }

                        progressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {

                        progressLayout.visibility = View.INVISIBLE

                        println("error$it")
                        Toast.makeText(
                            activity as Context,
                            "Some Error Occurred!!!",
                            Toast.LENGTH_SHORT).show()
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
                Toast.makeText(
                    activity as Context,
                    "Some Unexpected Error Occurred!!!",
                    Toast.LENGTH_SHORT).show()
            }
        } else {

            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text,listener->
                ActivityCompat.finishAffinity(activity as Activity)//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_home_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.sort -> {
                //RadioButton view for Sorting Display
                radioButtonView = View.inflate(contextParams, R.layout.sort_radio_button, null)
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") {text, listener ->
                        if(radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(restaurantInfoList, costComparator)
                            restaurantInfoList
                            //Updates the Adapter
                            homeAdapter.notifyDataSetChanged()
                        }
                        if(radioButtonView.radio_high_to_low.isChecked){
                            Collections.sort(restaurantInfoList, costComparator)
                            restaurantInfoList.reverse()
                            homeAdapter.notifyDataSetChanged()
                        }
                        if(radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantInfoList,ratingComparator)
                            restaurantInfoList.reverse()
                            homeAdapter.notifyDataSetChanged()
                        }

                    }
                    .setNegativeButton("Cancel") {text, listener ->

                    }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if(ConnectionManager().checkConnectivity(activity as Context)) {
            if(restaurantInfoList.isEmpty())
                fetchData() //If Internet is Available then Fetch Data
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
        super.onResume()
    }
}