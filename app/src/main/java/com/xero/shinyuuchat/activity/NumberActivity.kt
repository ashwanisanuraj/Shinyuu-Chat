package com.xero.shinyuuchat.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.xero.shinyuuchat.MainActivity
import com.xero.shinyuuchat.databinding.ActivityNumberBinding

class NumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNumberBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityNumberBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.continueBtn.setOnClickListener {
            if (binding.phoneInput.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Number", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this@NumberActivity, OTPActivity::class.java)
                intent.putExtra("number", binding.phoneInput.text!!.toString())
                startActivity(intent)
            }
        }


    }
}