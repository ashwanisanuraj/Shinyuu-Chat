package com.xero.shinyuuchat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xero.shinyuuchat.MainActivity
import com.xero.shinyuuchat.databinding.ActivityUserProfileBinding
import com.xero.shinyuuchat.model.UserModel
import java.util.Date

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private lateinit var selectedImg: Uri

    private val getContent =
        this.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImg = uri
                binding.profilePic2.setImageURI(selectedImg)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dialog = AlertDialog.Builder(this)
            .setMessage("Please Wait...")
            .setTitle("Updating Profile")
            .setCancelable(false)
            .create()

        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        checkUserProfile()
        initializeUI()
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setMessage("Are you sure you want to sign out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                auth.signOut()
                startActivity(Intent(this, NumberActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
            .create()

        alertDialogBuilder.show()
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
                            binding.usernameInput2.setText(user.name)
                            Glide.with(this@UserProfileActivity).load(user.imageUrl).into(binding.profilePic2)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun initializeUI() {
        binding.profilePic2.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.updateBtn.setOnClickListener {
            dialog.show()
            val username = binding.usernameInput2.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                uploadData(username)
            }
        }
    }

    private fun uploadData(username: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("Profile").child(Date().time.toString())
        storageReference.putFile(selectedImg)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val user = UserModel(auth.uid.toString(), username, auth.currentUser?.phoneNumber.toString(), uri.toString())
                        FirebaseDatabase.getInstance().reference.child("users").child(auth.uid.toString()).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnCompleteListener {
                                dialog.dismiss()
                            }
                    }
                } else {
                    Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
    }
}
