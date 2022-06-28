package com.itisakdesigns.foodbit.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.utils.ConnectionManager
import com.itisakdesigns.foodbit.utils.Validations
import org.json.JSONObject
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    //Declaring Variables
    private lateinit var etMobileNumber : EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtForgotPassword: TextView
    private lateinit var txtRegister: TextView

    //Variables for LogIn Session
    lateinit var sharedPreferences: SharedPreferences

    //OnCreate Method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Initializing Variables with respective ID's
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)


        //Click Listeners

        /*Forgot Password Text Click Listener*/
        txtForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        /*Register Text Click Listener*/

        txtRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if(isLoggedIn) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }

        /*Login Button Click Listener*/
        btnLogin.setOnClickListener {
            btnLogin.visibility = View.INVISIBLE

            if(Validations.validateMobile(etMobileNumber.text.toString())
                && Validations.validatePasswordLength(etPassword.text.toString())) {
                if(ConnectionManager().checkConnectivity(this@LoginActivity)) {
                    //Volley Queue Request
                    val queue = Volley.newRequestQueue(this@LoginActivity)
                    //JSON Params
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number",etMobileNumber.text.toString())
                    jsonParams.put("password", etPassword.text.toString())
                    //URL
                    val url = "http://13.235.250.119/v2/login/fetch_result/"
                    //JSON object Request
                    val jsonObjectRequest = object : JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonParams,
                        Response.Listener {
                            //Try-Catch Block
                            try {
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val response = data.getJSONObject("data")
                                    sharedPreferences.edit().putString("user_id", response.getString("user_id")).apply()
                                    sharedPreferences.edit().putString("user_name", response.getString("name")).apply()
                                    sharedPreferences.edit().putString("user_mobile_number", response.getString("mobile_number")).apply()
                                    sharedPreferences.edit().putString("user_address", response.getString("address")).apply()
                                    sharedPreferences.edit().putString("user_email", response.getString("email")).apply()

                                    savePreferences()
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finish()
                                } else {
                                    btnLogin.visibility = View.VISIBLE
                                    txtForgotPassword.visibility = View.VISIBLE
                                    val errorMessage = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@LoginActivity,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception){
                                btnLogin.visibility = View.VISIBLE
                                txtForgotPassword.visibility = View.VISIBLE
                                txtRegister.visibility = View.VISIBLE
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener {
                            btnLogin.visibility = View.VISIBLE
                            txtForgotPassword.visibility = View.VISIBLE
                            txtRegister.visibility = View.VISIBLE
                            Log.e("Error::::", "/post request fail! Error: ${it.message}")
                        }) {
                        //Sending Headers to API
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "254d7c5e450ec4"
                            return headers
                        }
                    }
                    queue.add(jsonObjectRequest)
                }
                else {
                    btnLogin.visibility = View.VISIBLE
                    txtForgotPassword.visibility = View.VISIBLE
                    txtRegister.visibility = View.VISIBLE
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Not Found")
                    dialog.setPositiveButton("Open Settings") {_,_ ->
                        val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit") {_,_ ->
                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }

                    dialog.create()
                    dialog.show()
                }
            } else {
                btnLogin.visibility = View.VISIBLE
                txtForgotPassword.visibility = View.VISIBLE
                txtRegister.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Invalid Phone or Password", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    } //OnCreate-End
    fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }
}