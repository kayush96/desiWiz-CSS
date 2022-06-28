package com.itisakdesigns.foodbit.adapter

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.model.CartItems
import com.itisakdesigns.foodbit.model.OrderHistoryRestaurant
import com.itisakdesigns.foodbit.utils.ConnectionManager
import org.json.JSONException
import java.lang.Exception

class OrderHistoryAdapter(val context: Context, private val orderedRestaurantList: ArrayList<OrderHistoryRestaurant>):
RecyclerView.Adapter<OrderHistoryAdapter.ViewHolderOrderHistory>() {

    class ViewHolderOrderHistory(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtDate: TextView = view.findViewById(R.id.txtDateOfOrder)
        val recyclerItemOrder: RecyclerView = view.findViewById(R.id.recyclerOrderHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderOrderHistory {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_order_history_single_row, parent, false)

        return ViewHolderOrderHistory(view)
    }

    override fun getItemCount(): Int {
        return orderedRestaurantList.size
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHistory, position: Int) {

        val restaurantObject = orderedRestaurantList[position]

        holder.txtRestaurantName.text = restaurantObject.restaurantName
        var formatDate = restaurantObject.orderPlacedAt
        formatDate = formatDate.replace("-","/")
        formatDate = formatDate.substring(0,6)+"20"+ formatDate.substring(6,8)
        holder.txtDate.text = formatDate

        val layoutManager = LinearLayoutManager(context)

        var orderedItemAdapter: CartAdapter

        if(ConnectionManager().checkConnectivity(context)) {
            //Try Catch block
            try {

                val orderItemsPerRestaurant = ArrayList<CartItems>()
                val sharedPreferences =
                    context.getSharedPreferences(context.getString(R.string.preference_file_name), Context.MODE_PRIVATE)

                val userId = sharedPreferences.getString("user_id", "0")

                //Volley Queue Request
                val queue = Volley.newRequestQueue(context)

                val url = "http://13.235.250.119/v2/orders/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url + userId,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if(success){
                            val resArray = responseJsonObjectData.getJSONArray("data")

                            //Restaurant at index of position
                            val fetchedRestaurantJsonObject = resArray.getJSONObject(position)
                            orderItemsPerRestaurant.clear()

                            val foodOrderedJsonArray = fetchedRestaurantJsonObject.getJSONArray("food_items")

                            for(i in 0 until foodOrderedJsonArray.length()) {
                                val eachFoodItem = foodOrderedJsonArray.getJSONObject(i)
                                val itemObject = CartItems(
                                    eachFoodItem.getString("food_item_id"),
                                    eachFoodItem.getString("name"),
                                    eachFoodItem.getString("cost"),
                                    "000"
                                )

                                orderItemsPerRestaurant.add(itemObject)
                            }

                            orderedItemAdapter = CartAdapter(
                                context,
                                orderItemsPerRestaurant
                            )
                            //Bind The RecyclerView To Adapter
                            holder.recyclerItemOrder.adapter = orderedItemAdapter
                            //Bind RecyclerView to Layout manager
                            holder.recyclerItemOrder.layoutManager = layoutManager
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            context,
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

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    context,
                    "Some Unexpected Error Occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }













    }
}