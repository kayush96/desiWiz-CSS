package com.itisakdesigns.foodbit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.model.CartItems

class CartAdapter(val context: Context, private val cartItems: ArrayList<CartItems>):
    RecyclerView.Adapter<CartAdapter.ViewHolderCart>() {

    class ViewHolderCart(view:View): RecyclerView.ViewHolder(view){
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCart {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_cart_single_row, parent, false)

        return ViewHolderCart(view)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: ViewHolderCart, position: Int) {
        val cartItemObject = cartItems[position]

        holder.txtItemName.text = cartItemObject.itemName
        holder.txtItemPrice.text = "Rs. " +cartItemObject.itemPrice
    }
}