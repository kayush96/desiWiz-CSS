package com.itisakdesigns.foodbit.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.itisakdesigns.foodbit.R


class ProfileFragment : Fragment() {
    //Declaring Variables
    private lateinit var txtUserName: TextView
    private lateinit var txtUserMobile: TextView
    private lateinit var txtUserEmail: TextView
    private lateinit var txtUserAddress: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        //Initializing Variables with their IDs
        txtUserName = view.findViewById(R.id.txtUserName)
        txtUserMobile = view.findViewById(R.id.txtUserMobile)
        txtUserEmail = view.findViewById(R.id.txtUserEmail)
        txtUserAddress = view.findViewById(R.id.txtUserAddress)

        sharedPreferences = (activity as FragmentActivity).getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        //Using Stored Value in profile fragment
        txtUserName.text = sharedPreferences.getString("user_name", null)
        val phone = "+91-${sharedPreferences.getString("user_mobile_number", null)}"
        txtUserMobile.text = phone
        txtUserEmail.text = sharedPreferences.getString("user_email", null)
        txtUserAddress.text = sharedPreferences.getString("user_address", null)

        return view
    }
}