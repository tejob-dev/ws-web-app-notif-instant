package com.notificationapp.kotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.notificationapp.kotlin.databinding.ItemMessageBinding
import com.notificationapp.kotlin.model.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptateur pour la RecyclerView des messages
 */
class MessageAdapter(private val messages: List<Message>) : 
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    
    companion object {
        private const val TAG = "MessageAdapter"
    }
    
    inner class MessageViewHolder(private val binding: ItemMessageBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            binding.textMessageContent.text = message.content
            binding.textMessageTime.text = formatTimestamp(message.timestamp)
            binding.textMessageType.text = message.type
        }
        
        private fun formatTimestamp(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(timestamp)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                timestamp
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount(): Int = messages.size
}
