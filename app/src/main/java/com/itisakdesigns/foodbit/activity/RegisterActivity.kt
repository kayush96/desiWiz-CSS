package com.itisakdesigns.foodbit.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.utils.ConnectionManager
import com.itisakdesigns.foodbit.utils.Validations
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    //Declaring Variables
    private lateinit var etUserName: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etUserMobile: EditText
    private lateinit var etUserDeliveryAddress: EditText
    private lateinit var etUserPassword: EditText
    private lateinit var etUserConfirmPassword: EditText
    private lateinit var btnRegister: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var rlRegister: RelativeLayout
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Initializing Variables with their ID's
        etUserName = findViewById(R.id.etUserName)
        etUserEmail = findViewById(R.id.etEmailAddress)
        etUserMobile = findViewById(R.id.etMobileNumber)
        etUserDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        etUserPassword = findViewById(R.id.etPassword)
        etUserConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        rlRegister = findViewById(R.id.rlRegister)
        progressBar = findViewById(R.id.progressBar)

        //sharedPreferences = this@RegisterActivity.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        rlRegister.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE


        //Click Listener for Register Button
        btnRegister.setOnClickListener {
            rlRegister.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE

            if (Validations.validateNameLength(etUserName.text.toString())) {
                etUserName.error = null
                if (Validations.validateEmail(etUserEmail.text.toString())) {
                    etUserEmail.error = null
                    if (Validations.validateMobile(etUserMobile.text.toString())) {
                        etUserMobile.error = null
                        if (Validations.validatePasswordLength(etUserPassword.text.toString())) {
                            etUserPassword.error = null
                            if (Validations.matchPassword(
                                    etUserPassword.text.toString(),
                                    etUserConfirmPassword.text.toString()
                                )
                            ) {
                                etUserPassword.error = null
                                etUserConfirmPassword.error = null
                                //Check Internet Connection
                                if(ConnectionManager().checkConnectivity(this@RegisterActivity)){
                                    sendRegisterRequest(etUserName.text.toString(),
                                        etUserMobile.text.toString(),
                                        etUserDeliveryAddress.text.toString(),
                                        etUserPassword.text.toString(),
                                        etUserEmail.text.toString())
                                }
                                else {
                                    rlRegister.visibility = View.VISIBLE
                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(this@RegisterActivity,
                                        "Internet Connection Not Found",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                rlRegister.visibility = View.VISIBLE
                                progressBar.visibility= View.INVISIBLE
                                etUserPassword.error = "Password Don't Match"
                                etUserConfirmPassword.error = "Password Don't Match"
                                Toast.makeText(this@RegisterActivity,
                                    "Passwords Don't Match, Try Again!",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            rlRegister.visibility = View.VISIBLE
                            progressBar.visibility= View.INVISIBLE
                            etUserPassword.error = "Password must be more than or equal to 4"
                            Toast.makeText(this@RegisterActivity,
                                "Password must be more than or equal to 4",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility= View.INVISIBLE
                        etUserMobile.error = "Invalid Mobile Number"
                        Toast.makeText(this@RegisterActivity,
                            "Invalid Mobile Number",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility= View.INVISIBLE
                    etUserEmail.error = "Invalid Email Address"
                    Toast.makeText(this@RegisterActivity,
                        "Invalid Email Address",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                rlRegister.visibility = View.VISIBLE
                progressBar.visibility= View.INVISIBLE
                etUserName.error = "Invalid User Name"
                Toast.makeText(this@RegisterActivity,
                    "Invalid User Name",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }

    private fun sendRegisterRequest(name: String, phone: String, address: String, password: String, email: String) {

        //Volley Queue Request
        val queue = Volley.newRequestQueue(this)
        //url
        val url = "http://13.235.250.119/v2/register/fetch_result"
        //jsonParams
        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password",password)
        jsonParams.put("address",address)
        jsonParams.put("email",email)
        //Json Object Request

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                //try-catch block
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    //check if Success
                    if(success) {
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit().putString("user_id", response.getString("user_id")).apply()
                        sharedPreferences.edit().putString("user_name", response.getString("name")).apply()
                        sharedPreferences.edit().putString("user_mobile_number", response.getString("mobile_number")).apply()
                        sharedPreferences.edit().putString("user_address", response.getString("address")).apply()
                        sharedPreferences.edit().putString("user_email", response.getString("email")).apply()

                        savePreferences()
                        // sessionManager.setLogin(true)
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                        finish()

                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        val errorMessage = data.getString("errorMessage")
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()

                    }
                }catch (e: JSONException) {
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                rlRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                Log.e("Error::::", "/post request fail! Error: ${it.message}")

            }) {
            //Headers
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "254d7c5e450ec4"
                return headers
            }

        }
        queue.add(jsonObjectRequest)
    }
}