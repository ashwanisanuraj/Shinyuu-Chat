package com.xero.shinyuuchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xero.shinyuuchat.adapter.ChatAdapter
import com.xero.shinyuuchat.databinding.FragmentChatBinding
import com.xero.shinyuuchat.model.UserModel

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private var database: FirebaseDatabase? = null
    private lateinit var userList: ArrayList<UserModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment

        database = FirebaseDatabase.getInstance()
        userList = ArrayList()

        database!!.reference.child("users")
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (snapshot1 in snapshot.children){
                    val user = snapshot1.getValue(UserModel::class.java)
                    if (user!!.uid != FirebaseAuth.getInstance().uid){
                        userList.add(user)
                    }
                }

                binding.userChat.adapter = ChatAdapter(requireContext(), userList)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        return binding.root
    }
}