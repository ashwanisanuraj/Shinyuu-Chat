package com.xero.shinyuuchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.xero.shinyuuchat.R
import com.xero.shinyuuchat.databinding.ReceiveChatLayoutBinding
import com.xero.shinyuuchat.databinding.SendChatLayoutBinding
import com.xero.shinyuuchat.model.MsgModel

class MsgAdapter(var context: Context, private var list: ArrayList<MsgModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var ITEM_SENT = 1
    var ITEM_RECEIVE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_SENT)
            SentViewHolder(LayoutInflater.from(context).inflate(R.layout.send_chat_layout, parent, false))
        else ReceiveViewHolder(
            LayoutInflater.from(context).inflate(R.layout.receive_chat_layout, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if ( FirebaseAuth.getInstance().uid == list[position].senderId ) ITEM_SENT else ITEM_RECEIVE
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]
        if(holder.itemViewType == ITEM_SENT){
            val viewHolder = holder as SentViewHolder
            viewHolder.binding.senChat.text = message.message
        }else{
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.binding.receiveChat.text = message.message
        }
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = SendChatLayoutBinding.bind(view)
    }

    inner class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ReceiveChatLayoutBinding.bind(view)
    }

}