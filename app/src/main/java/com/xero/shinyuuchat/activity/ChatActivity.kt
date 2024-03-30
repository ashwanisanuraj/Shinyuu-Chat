package com.xero.shinyuuchat.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private lateinit var msgAdapter: MsgAdapter
    private val messageList = ArrayList<MsgModel>()

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

        // Set up RecyclerView
        msgAdapter = MsgAdapter(this, messageList)
        binding.RecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = msgAdapter
        }

        // Set click listener for send button
        binding.sendTextBtn.setOnClickListener {
            val messageText = binding.messageBox.text.toString().trim()
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Empty Message", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(messageText)
            }
        }

        // Listen for changes in chat messages
        database.reference.child("chats").child(senderRoom).child("message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MsgModel::class.java)
                        message?.let { messageList.add(it) }
                    }
                    msgAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(messageText: String) {
        val message = MsgModel(messageText, senderUid, Date().time)
        val randomKey = database.reference.push().key
        randomKey?.let { key ->
            val messageRef = database.reference.child("chats")
            val messageMap = HashMap<String, Any>()
            messageMap["$senderRoom/message/$key"] = message
            messageMap["$receiverRoom/message/$key"] = message
            messageRef.updateChildren(messageMap)
                .addOnSuccessListener {
                    binding.messageBox.text.clear()
                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Failed to generate random key", Toast.LENGTH_SHORT).show()
        }
    }
}

