package com.example.studentmanagement.notification

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.database.NotificationEntity
import com.example.studentmanagement.databinding.ItemNotificationBinding
import com.example.studentmanagement.model.NotificationType
import com.example.studentmanagement.utils.DateUtils

class NotificationAdapter(
    private val onItemClick: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onItemClick: (NotificationEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationEntity) {
            binding.root.setOnClickListener { onItemClick(notification) }

            binding.tvNotifTitle.text = notification.title
            binding.tvNotifMessage.text = notification.message
            binding.tvNotifTime.text = DateUtils.formatRelativeTime(notification.createdAt)
            
            val type = NotificationType.fromString(notification.type)
            binding.tvTypeEmoji.text = type.emoji()

            // Styling based on read/unread status
            if (notification.isRead) {
                binding.tvNotifTitle.setTypeface(null, Typeface.NORMAL)
                binding.tvNotifMessage.alpha = 0.7f
                binding.viewUnreadDot.visibility = View.INVISIBLE
                binding.cardNotification.setCardBackgroundColor(android.graphics.Color.WHITE)
            } else {
                binding.tvNotifTitle.setTypeface(null, Typeface.BOLD)
                binding.tvNotifMessage.alpha = 1.0f
                binding.viewUnreadDot.visibility = View.VISIBLE
                binding.cardNotification.setCardBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"))
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
