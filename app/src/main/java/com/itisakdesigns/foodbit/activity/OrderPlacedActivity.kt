package com.itisakdesigns.foodbit.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.itisakdesigns.foodbit.R

class OrderPlacedActivity : AppCompatActivity() {

    private lateinit var buttonOk: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)

        buttonOk = findViewById(R.id.btnOk)

        buttonOk.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            finishAffinity()
        })
    }

    override fun onBackPressed() {

    }
}