package com.xero.shinyuuchat.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.xero.shinyuuchat.R
import com.xero.shinyuuchat.databinding.ActivityDeveloperBinding
import com.xero.shinyuuchat.databinding.ActivityUserProfileBinding

class DeveloperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeveloperBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityDeveloperBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}