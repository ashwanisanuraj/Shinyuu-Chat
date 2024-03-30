package com.xero.shinyuuchat.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xero.shinyuuchat.adapter.MsgAdapter
import com.xero.shinyuuchat.databinding.ActivityChatBinding
import com.xero.shinyuuchat.model.MsgModel
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var list: ArrayList<MsgModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Get sender and receiver UIDs from Intent
        senderUid = FirebaseAuth.getInstance().uid.toString()
        receiverUid = intent.getStringExtra("uid") ?: ""

        // Create sender and receiver rooms
        senderRoom = "$senderUid$receiverUid"
        receiverRoom = "$receiverUid$senderUid"

        list = ArrayList()

        // Set click listener for send button
        binding.sendTextBtn.setOnClickListener {
            if (binding.messageBox.text.isEmpty()) {
                Toast.makeText(this, "Empty Message", Toast.LENGTH_SHORT).show()
            } else {
                val message = MsgModel(binding.messageBox.text.toString(), senderUid, Date().time)
                val randomKey = database.reference.push().key
                if (randomKey != null) {
                    // Send message to sender room
                    database.reference.child("chats").child(senderRoom).child("message").child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {
                            // Send message to receiver room
                            database.reference.child("chats").child(receiverRoom).child("message").child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener {
                                    // Clear message box after sending message
                                    binding.messageBox.text = null
                                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure to send message to receiver room
                                    Toast.makeText(this, "Failed to send message to receiver: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to send message to sender room
                            Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Handle case where randomKey is null
                    Toast.makeText(this, "Failed to generate random key", Toast.LENGTH_SHORT).show()
                }
            }
        }

        database.reference.child("chats").child(senderRoom).child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()

                    for (snapshot1 in snapshot.children){
                        val data = snapshot1.getValue(MsgModel::class.java)
                        list.add(data!!)
                    }

                    binding.RecyclerView.adapter = MsgAdapter(this@ChatActivity, list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error : $error", Toast.LENGTH_SHORT).show()
                }
            })

    }
}
