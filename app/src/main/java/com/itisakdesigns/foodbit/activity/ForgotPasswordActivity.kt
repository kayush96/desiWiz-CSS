package com.itisakdesigns.foodbit.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.utils.ConnectionManager
import com.itisakdesigns.foodbit.utils.Validations
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {
    //Declaring Variables
    private lateinit var etForgotMobile: EditText
    private lateinit var etForgotEmail: EditText
    private lateinit var btnForgotNext: Button
    private lateinit var progressLayout: RelativeLayout
    private lateinit var rlContentMain: RelativeLayout
    private lateinit var progressBar: ProgressBar

    //On Create Method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //Initializing Variables with respective IDs
        etForgotMobile = findViewById(R.id.etMobileNumber)
        etForgotEmail = findViewById(R.id.etEmail)
        btnForgotNext = findViewById(R.id.btnNext)
        progressLayout = findViewById(R.id.progressLayout)
        rlContentMain = findViewById(R.id.rlContentMain)
        progressBar = findViewById(R.id.progressBar)

        //Click Listener of Button Next
        btnForgotNext.setOnClickListener {

            if(Validations.validateMobile(etForgotMobile.text.toString())) {
                etForgotMobile.error = null
                if(Validations.validateEmail(etForgotEmail.text.toString())) {
                    etForgotEmail.error = null
                    //Checking Internet Connection
                    if(ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {
                        rlContentMain.visibility = View.GONE
                        progressLayout.visibility = View.VISIBLE
                        //Calling Function to send Data
                        sendOTP(etForgotMobile.text.toString(), etForgotEmail.text.toString())
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        progressLayout.visibility = View.GONE
                        Toast.makeText(this@ForgotPasswordActivity, "No Internet Connection Available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    rlContentMain.visibility = View.VISIBLE
                    progressLayout.visibility = View.GONE
                    etForgotEmail.error = "Invalid Email Address"
                }
            } else {
                rlContentMain.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                etForgotMobile.error = "Invalid Mobile Number"
            }
        }
    }

    private fun sendOTP(mobileNumber: String, email: String) {
        //Volley Request Queue
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)

        val url = "http://13.235.250.119/v2/forgot_password/fetch_result"
        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                //Try Catch Block
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if(success) {
                        val firstTry = data.getBoolean("first_try")
                        if(firstTry) {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Check Information")
                            builder.setMessage("Please Check Email For getting the OTP")
                            builder.setCancelable(false)
                            builder.setPositiveButton("OK") {_,_ ->
                                val intent = Intent(this@ForgotPasswordActivity, ChangePasswordActivity::class.java)
                                intent.putExtra("mobile_number", mobileNumber)
                                startActivity(intent)
                                finish()

                            }
                            builder.create().show()
                        } else {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Check Information")
                            builder.setMessage("Please Refer Previous Email For getting the OTP")
                            builder.setCancelable(false)
                            builder.setPositiveButton("OK") {_,_ ->
                                val intent = Intent(this@ForgotPasswordActivity, ChangePasswordActivity::class.java)
                                intent.putExtra("mobile_number", mobileNumber)
                                startActivity(intent)
                                finish()

                            }
                            builder.create().show()
                        }
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        progressLayout.visibility = View.GONE
                        Toast.makeText(this@ForgotPasswordActivity, "Mobile Number Not Registered", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    rlContentMain.visibility = View.VISIBLE
                    progressLayout.visibility = View.GONE
                    Toast.makeText(this@ForgotPasswordActivity, "Incorrect Response Error", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                rlContentMain.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                VolleyLog.e("Error::::","/post request fail! Error: ${it.message}")
                Toast.makeText(this@ForgotPasswordActivity, it.message, Toast.LENGTH_SHORT).show()

            }) {

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