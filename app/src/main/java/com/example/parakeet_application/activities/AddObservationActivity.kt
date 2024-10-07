package com.example.parakeet_application.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.parakeet_application.R

class AddObservationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_observations)

        class AddObservationActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                //This call the parent constructor
                super.onCreate(savedInstanceState)

                // Initialize the toolbar
                val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.btnHome)

                // Set the toolbar as the action bar
                setSupportActionBar(toolbar)

                // Enable the back button in the toolbar
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // Set the click listener for the back button
                toolbar.setNavigationOnClickListener {
                    onBackPressed()
                }

            }
        }
    }
}