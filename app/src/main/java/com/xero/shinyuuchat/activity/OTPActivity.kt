package com.xero.shinyuuchat.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.xero.shinyuuchat.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit
import com.xero.shinyuuchat.activity.ProfileActivity


class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Initialize loading dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please Wait...")
        builder.setTitle("Loading")
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()

        // Get phone number from intent
        val phoneNumber = "+91" + intent.getStringExtra("number")

        // Configure PhoneAuthOptions
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    // Auto-retrieval or instant verification completed
                    //signInWithCredential(credential)
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    // Verification failed
                    dialog.dismiss()
                    Toast.makeText(this@OTPActivity, "Verification failed: Try Again", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    // Verification code successfully sent
                    super.onCodeSent(p0, p1)
                    dialog.dismiss()
                    verificationId = p0
                }
            }).build()

        // Initiate phone number verification
        PhoneAuthProvider.verifyPhoneNumber(options)

        // Button click listener for OTP verification
        binding.verifyBtn.setOnClickListener {
            if (binding.otpInput.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter OTP!!", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                val credential = PhoneAuthProvider.getCredential(verificationId, binding.otpInput.text!!.toString())
                auth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if ( it.isSuccessful ){
                            startActivity(Intent(this@OTPActivity, ProfileActivity::class.java))
                            finish()
                        }else{
                            dialog.dismiss()
                            Toast.makeText(this, "Error ${it.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
