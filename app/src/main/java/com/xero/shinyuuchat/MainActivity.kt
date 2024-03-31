package com.xero.shinyuuchat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xero.shinyuuchat.activity.DeveloperActivity
import com.xero.shinyuuchat.activity.NumberActivity
import com.xero.shinyuuchat.activity.UserProfileActivity
import com.xero.shinyuuchat.activity.WelcomeActivity
import com.xero.shinyuuchat.fragments.CallFragment
import com.xero.shinyuuchat.fragments.ChatFragment
import com.xero.shinyuuchat.fragments.StatusFragment
import com.xero.shinyuuchat.adapter.ViewPagerAdapter
import com.xero.shinyuuchat.databinding.ActivityMainBinding
import com.xero.shinyuuchat.model.UserModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)
        if (isFirstTime) {
            openWelcomePage()
            with(sharedPreferences.edit()) {
                putBoolean("isFirstTime", false)
                apply()
            }
        } else {
            // Check if the user is already logged in
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                startActivity(Intent(this, NumberActivity::class.java))
                finish()
            } else {
                initializeMainActivity()
            }
        }
    }

    private fun initializeMainActivity() {
        val fragmentArrayList = ArrayList<Fragment>()
        fragmentArrayList.add(ChatFragment())
        fragmentArrayList.add(StatusFragment())
        fragmentArrayList.add(CallFragment())

        val adapter = ViewPagerAdapter(this, supportFragmentManager, fragmentArrayList)

        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        checkUserProfile()

        binding.userProfilePic.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        binding.developer.setOnClickListener {
            startActivity(Intent(this@MainActivity, DeveloperActivity::class.java))
        }
    }

    private fun openWelcomePage() {
        startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
        finish() // Finish the MainActivity after starting WelcomeActivity
    }

    private fun checkUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        user?.let {
                            Glide.with(this@MainActivity).load(user.imageUrl).into(binding.userProfilePic)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
