package com.example.unit1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unit1.databinding.ItemCalendarEventBinding

class CalendarEventAdapter :
    ListAdapter<CalendarEvent, CalendarEventAdapter.CalendarEventViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CalendarEvent>() {
            override fun areItemsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent) =
                oldItem == newItem
        }
    }

    class CalendarEventViewHolder(val binding: ItemCalendarEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CalendarEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.eventIdTextView.text = "ID: ${event.id}"
        holder.binding.eventNameTextView.text = event.name
        holder.binding.eventDateTextView.text = event.date
    }
}