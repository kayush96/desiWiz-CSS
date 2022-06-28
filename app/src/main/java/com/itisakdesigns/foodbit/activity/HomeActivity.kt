package com.itisakdesigns.foodbit.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.fragment.FaqFragment
import com.itisakdesigns.foodbit.fragment.FavouritesFragment
import com.itisakdesigns.foodbit.fragment.HomeFragment
import com.itisakdesigns.foodbit.fragment.ProfileFragment

class HomeActivity : AppCompatActivity() {
    //Creating Variables
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Inflating Drawer Header in Navigation
        val convertView = LayoutInflater.from(this@HomeActivity).inflate(R.layout.drawer_header, null)
        //Initializing Variables with ID's
        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)

        val userName: TextView = convertView.findViewById(R.id.txtUserName)
        val userMobile: TextView = convertView.findViewById(R.id.txtUserMobileNumber)
        val userEmail: TextView = convertView.findViewById(R.id.txtUserEmail)
        val appIcon: ImageView = convertView.findViewById(R.id.imgUserImage)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        userName.text = sharedPreferences.getString("user_name", null)

        val phoneNo = "+91-${sharedPreferences.getString("user_mobile_number", null)}"
        userMobile.text = phoneNo

        userEmail.text = sharedPreferences.getString("user_email", null)

        navigationView.addHeaderView(convertView)



        //calling setup toolbar function
        setUpToolbar()

        openHome() //For Opening Home Fragment on Running the app

        //creating object for hamburger menu
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@HomeActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle) //enables click listener to hamburger icon
        actionBarDrawerToggle.syncState()

        //Setting Click Listeners to Navigation Items
        navigationView.setNavigationItemSelectedListener {

            //Check-Uncheck Menu Items
            if(previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isChecked = true
            it.isCheckable = true
            previousMenuItem = it

            when(it.itemId){

                R.id.home -> {

                    openHome()
                    drawerLayout.closeDrawers()
                }

                R.id.myProfile -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame,
                            ProfileFragment()
                        )
                        .commit()

                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }

                R.id.favourite -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame,
                            FavouritesFragment(this)
                        )
                        .commit()

                    supportActionBar?.title = "Favourite Restaurants"
                    drawerLayout.closeDrawers()

                }

                R.id.order -> {

                    val intent = Intent(this, OrderHistoryActivity::class.java)
                    drawerLayout.closeDrawers()
                    startActivity(intent)
                }

                R.id.faq -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame,
                            FaqFragment()
                        )
                        .commit()

                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()

                }

                //Logout from your account
                R.id.logout -> {
                    //Alert Box for logout
                    val builder = AlertDialog.Builder(this@HomeActivity)
                    builder.setTitle("Confirm")
                        .setMessage("Are you sure you want to Logout?")
                        .setPositiveButton("Yes") { _,_->
                            savePreferences()
                            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Success",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .setNegativeButton("No") {_,_ ->
                            openHome()
                        }
                        .create()
                        .show()

                    drawerLayout.closeDrawers()

                }
            }
            return@setNavigationItemSelectedListener true
        }

    }
    //Setup Toolbar
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Restaurants"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    //Enabling Home Button to function as Hamburger menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    //Opening HomePage Fragment Method
    private fun openHome() {
        val fragment = HomeFragment(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    //Back Button Functionality
    override fun onBackPressed() {
        when(supportFragmentManager.findFragmentById(R.id.frame)) {
            !is HomeFragment -> openHome()
            else -> super.onBackPressed()
        }
    }

    private fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn",false).apply()
    }
}