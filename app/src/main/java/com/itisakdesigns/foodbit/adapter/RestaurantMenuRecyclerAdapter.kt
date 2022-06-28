package com.itisakdesigns.foodbit.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.activity.CartActivity
import com.itisakdesigns.foodbit.model.RestaurantMenu
import kotlinx.android.synthetic.main.recycler_menu_items_single_row.view.*

class RestaurantMenuRecyclerAdapter(val context: Context,
                                    private val restaurantId: String,
                                    private val restaurantName: String,
                                    private val proceedToCartPassed: RelativeLayout,
                                    private val buttonProceedToCart: Button,
                                    private val restaurantMenu: ArrayList<RestaurantMenu>):
    RecyclerView.Adapter<RestaurantMenuRecyclerAdapter.ViewHolderRestaurantMenu>() {

    var itemSelectedCount: Int = 0
    lateinit var proceedToCart: RelativeLayout

    var itemsSelectedId = arrayListOf<String>()

    class ViewHolderRestaurantMenu(view:View): RecyclerView.ViewHolder(view) {
        val txtSerialNumber: TextView = view.findViewById(R.id.txtSno)
        val txtItemName: TextView = view.findViewById(R.id.txtDishName)
        val txtItemPrice: TextView = view.findViewById(R.id.txtDishPrice)
        val buttonAddToCart: Button = view.findViewById(R.id.btnAddToCart)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRestaurantMenu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_menu_items_single_row, parent, false)

        return ViewHolderRestaurantMenu(view)
    }

    override fun getItemCount(): Int {
        restaurantMenu.size
        return restaurantMenu.size
    }

    override fun onBindViewHolder(holder: ViewHolderRestaurantMenu, position: Int) {
        val restaurantMenuItem = restaurantMenu[position]

        proceedToCart = proceedToCartPassed
        buttonProceedToCart.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, CartActivity::class.java)
            //Passing Restaurant ID to Next Activity
            intent.putExtra("restaurantId", restaurantId.toString())
            intent.putExtra("restaurantName", restaurantName)
            //Passing All the items selected by User
            intent.putExtra("selectedItemsId", itemsSelectedId)

            context.startActivity(intent)
        })

        holder.buttonAddToCart.setOnClickListener(View.OnClickListener {

            if(holder.buttonAddToCart.text.toString() == "Remove") {
                //Not Selected
                itemSelectedCount--
                itemsSelectedId.remove(holder.buttonAddToCart.tag.toString())

                holder.buttonAddToCart.text = "Add"

                holder.buttonAddToCart.setBackgroundColor(Color.rgb(219, 243, 26))
            } else {
                itemSelectedCount++

                itemsSelectedId.add(holder.buttonAddToCart.tag.toString())

                holder.buttonAddToCart.text = "Remove"
                holder.buttonAddToCart.setBackgroundColor(Color.rgb(172,217,38))
            }

            if(itemSelectedCount > 0) {
                proceedToCart.visibility = View.VISIBLE

            } else {
                proceedToCart.visibility = View.INVISIBLE
            }
        })

        holder.buttonAddToCart.setTag(restaurantMenuItem.id+"")//save the item id in textViewName Tag ,will be used to add to cart
        holder.txtSerialNumber.text=(position+1).toString()//position starts from 0
        holder.txtItemName.text=restaurantMenuItem.name
        holder.txtItemPrice.text="Rs."+restaurantMenuItem.cost_for_one

    }
    fun getSelectedItemCount():Int{
        return itemSelectedCount
    }
}