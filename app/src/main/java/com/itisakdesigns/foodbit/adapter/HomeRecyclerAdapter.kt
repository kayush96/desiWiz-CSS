package com.itisakdesigns.foodbit.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.activity.RestaurantMenuActivity
import com.itisakdesigns.foodbit.database.RestaurantDatabase
import com.itisakdesigns.foodbit.database.RestaurantEntity
import com.itisakdesigns.foodbit.model.Restaurant
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_home_single_row.view.*

class HomeRecyclerAdapter(val context: Context, var itemList:ArrayList<Restaurant>):
    RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgRestaurantImage: ImageView = view.findViewById(R.id.imgRestaurantImage)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtPricePerPerson: TextView = view.findViewById(R.id.txtRestaurantPrice)
        val txtRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val txtFavourite: TextView = view.findViewById(R.id.imgFavourite)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_home_single_row, parent, false)

        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant = itemList[position]

        val restaurantEntity = RestaurantEntity(
            restaurant.restaurantId,
            restaurant.restaurantName
        )

        holder.txtFavourite.setOnClickListener(View.OnClickListener{
            if(!DBAsyncTask(context,restaurantEntity, 1).execute().get()) {

                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()

                if(result) {

                    Toast.makeText(context, "Added To Favourites", Toast.LENGTH_SHORT).show()

                    holder.txtFavourite.tag = "liked" //new Value
                    holder.txtFavourite.background = context.resources.getDrawable(R.drawable.ic_favourite_filled)

                } else {
                    Toast.makeText(context, "Some Error Occurred!", Toast.LENGTH_SHORT).show()

                }
            } else {

                val result = DBAsyncTask(context, restaurantEntity,3).execute().get()

                if(result) {
                    Toast.makeText(context, "Removed From Favourites", Toast.LENGTH_SHORT).show()
                    holder.txtFavourite.tag = "unliked"
                    holder.txtFavourite.background = context.resources.getDrawable(R.drawable.ic_favourite)
                } else {
                    Toast.makeText(context, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        holder.llContent.setOnClickListener(View.OnClickListener {
            println(holder.txtRestaurantName.tag.toString())
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantId", holder.txtRestaurantName.tag.toString())
            intent.putExtra("restaurantName", holder.txtRestaurantName.text.toString())
            context.startActivity(intent)

        })

        holder.txtRestaurantName.tag = restaurant.restaurantId + ""
        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtPricePerPerson.text = restaurant.cost_for_one + "/Person"
        holder.txtRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.default_foods).into(holder.imgRestaurantImage)

        val checkFav = DBAsyncTask(context, restaurantEntity,1).execute()
        val isFav = checkFav.get()

        if(isFav) {
            holder.txtFavourite.tag = "liked"
            holder.txtFavourite.background = context.resources.getDrawable(R.drawable.ic_favourite_filled)

        } else {
            holder.txtFavourite.tag = "unliked"
            holder.txtFavourite.background = context.resources.getDrawable(R.drawable.ic_favourite)
        }
    }

    fun filterList(filteredList: ArrayList<Restaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    class DBAsyncTask(val context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int)
        :AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            /*
           * Mode 1->check if restaurant is in favourites
           * Mode 2->Save the restaurant into DB as favourites
           * Mode 3-> Remove the favourite restaurant*/
            when(mode){
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant!= null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                } else -> return false
            }

            }
        }

    }