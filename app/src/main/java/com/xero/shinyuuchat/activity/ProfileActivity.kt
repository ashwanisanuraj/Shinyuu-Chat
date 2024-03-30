package com.xero.shinyuuchat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var dialog: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityProfileBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        dialog = AlertDialog.Builder(this)
            .setMessage("Updating Profile...")
            .setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.profilePic.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.continueFromProfleBtn.setOnClickListener {
            if (binding.usernameInput.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
            } else if (selectedImg == null) {
                Toast.makeText(this, "Please Select Your Image", Toast.LENGTH_SHORT).show()
            } else uploadData()

        }

    }

    private fun uploadData() {
        //here new folder will be created
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImg)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { task ->
                        uploadInfo(task.toString())
                    }
                }
            }
    }

    private fun uploadInfo(imgUrl: String) {
        //we need to create a model data class of UserModel before proceeding
        val user = UserModel(
            auth.uid.toString(),
            binding.usernameInput.text.toString(),
            auth.currentUser!!.phoneNumber.toString(),
            imgUrl
        )
        database.reference.child("users")
            .child(auth.uid.toString())
            .setValue(user)
            .addOnSuccessListener {//creating a new directory to store users
                Toast.makeText(this, "User Created Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (data.data != null) {
                selectedImg = data.data!!
                binding.profilePic.setImageURI(selectedImg)
            }
        }
    }
}