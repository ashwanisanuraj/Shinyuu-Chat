package com.xero.shinyuuchat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xero.shinyuuchat.MainActivity
import com.xero.shinyuuchat.databinding.ActivityProfileBinding
import com.xero.shinyuuchat.model.UserModel
import java.util.Date

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: AlertDialog

    private val getContent =
        this.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImg = uri
                binding.profilePic.setImageURI(selectedImg)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user already has a profile
        checkUserProfile()
    }

    private fun checkUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User profile exists, display skipEdit button
                        binding.skipEdit.visibility = View.VISIBLE

                        binding.skipEdit.setOnClickListener {
                            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                        }

                        // Retrieve old name and profile pic URL
                        val user = snapshot.getValue(UserModel::class.java)
                        user?.let {
                            // Display old name in the EditText
                            binding.usernameInput.setText(user.name)

                            // Load old profile pic
                            Glide.with(this@ProfileActivity).load(user.imageUrl).into(binding.profilePic)
                        }
                    } else {
                        // User profile does not exist, proceed with profile update
                        initializeUI()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(this@ProfileActivity, "Error checking user profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun initializeUI() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize loading dialog
        dialog = AlertDialog.Builder(this)
            .setMessage("Please Wait...")
            .setTitle("Updating Profile")
            .setCancelable(false)
            .create()

        binding.profilePic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.continueFromProfleBtn.setOnClickListener {
            dialog.show()
            if (binding.usernameInput.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else if (!::selectedImg.isInitialized) {
                Toast.makeText(this, "Please Select Your Image", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                uploadData()
            }
        }
    }

    private fun uploadData() {
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImg)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        uploadInfo(uri.toString())
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
    }

    private fun uploadInfo(imgUrl: String) {
        val user = UserModel(
            auth.uid.toString(),
            binding.usernameInput.text.toString(),
            auth.currentUser?.phoneNumber.toString(),
            imgUrl
        )
        database.reference.child("users")
            .child(auth.uid.toString())
            .setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User Created Successfully", Toast.LENGTH_SHORT).show()
                // Navigate to MainActivity only after user creation is successful
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                dialog.dismiss()
            }
    }
}
