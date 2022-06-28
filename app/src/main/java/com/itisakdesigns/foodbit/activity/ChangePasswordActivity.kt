package com.itisakdesigns.foodbit.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.itisakdesigns.foodbit.R
import com.itisakdesigns.foodbit.utils.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {
    //Declaring Variables
    private lateinit var etOTP: EditText
    private lateinit var etEnterPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        //Initializing Variables
        etOTP = findViewById(R.id.etOtpNumber)
        etEnterPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val otp = etOTP.text.toString()
            val password = etEnterPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val mobileNumber = intent.getStringExtra("mobile_number")

            if(otp != "" && password != "" && confirmPassword != ""){
                if(password==confirmPassword) {

                    //Volley Queue Request
                    val queue = Volley.newRequestQueue(this@ChangePasswordActivity)

                    //Json Parameters
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", mobileNumber)
                    jsonParams.put("password", password)
                    jsonParams.put("otp", otp)

                    val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                    if(ConnectionManager().checkConnectivity(this@ChangePasswordActivity)) {
                        val jsonObjectRequest = object: JsonObjectRequest(
                            Method.POST,
                            url,
                            jsonParams,
                            Response.Listener {
                                //Try Catch Block
                                try{
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success")

                                    if(success){
                                        val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        Toast.makeText(
                                            this@ChangePasswordActivity,
                                            data.getString("successMessage"),
                                            Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(
                                            this@ChangePasswordActivity,
                                            data.getString("errorMessage"),
                                            Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        this@ChangePasswordActivity,
                                        "Some Error Occurred",
                                        Toast.LENGTH_SHORT).show()
                                }
                            },
                            Response.ErrorListener {
                                Toast.makeText(
                                    this@ChangePasswordActivity,
                                    "Volley Error Occurred",
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

                    } else {
                        val dialog = AlertDialog.Builder(this@ChangePasswordActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet Connection Not Found")
                        dialog.setPositiveButton("Open Settings") {_,_ ->
                            val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") {_,_ ->
                            ActivityCompat.finishAffinity(this@ChangePasswordActivity)
                        }

                        dialog.create()
                        dialog.show()
                    }

                } else {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Passwords Don't Match",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@ChangePasswordActivity,
                    "Enter Details And Continue",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}