package com.xero.shinyuuchat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.xero.shinyuuchat.activity.NumberActivity
import com.xero.shinyuuchat.activity.ProfileActivity
import com.xero.shinyuuchat.fragments.CallFragment
import com.xero.shinyuuchat.fragments.ChatFragment
import com.xero.shinyuuchat.fragments.StatusFragment
import com.xero.shinyuuchat.adapter.ViewPagerAdapter
import com.xero.shinyuuchat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val fragmentArrayList = ArrayList<Fragment>()
        fragmentArrayList.add(ChatFragment())
        fragmentArrayList.add(StatusFragment())
        fragmentArrayList.add(CallFragment())

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, NumberActivity::class.java))
            finish()
        }


        val adapter = ViewPagerAdapter(this, supportFragmentManager, fragmentArrayList)

        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setMessage("Are you sure you want to sign out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Sign out user
                auth.signOut()
                // Navigate to NumberActivity
                startActivity(Intent(this, NumberActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
            .create()

        alertDialogBuilder.show()
    }
}