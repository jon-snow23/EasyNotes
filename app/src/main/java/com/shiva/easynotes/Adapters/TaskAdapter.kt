package com.shiva.easynotes.Adapters

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shiva.easynotes.Models.TaskItem
import com.shiva.easynotes.R

class TaskAdapter(): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var list: List<TaskItem> = listOf()

    fun setList(newList : List<TaskItem>){
        list = newList
        notifyDataSetChanged()
    }
    class TaskViewHolder(view: View): RecyclerView.ViewHolder(view){
        val taskMessage = view.findViewById<TextView>(R.id.task_message)
        val taskTime = view.findViewById<TextView>(R.id.task_reminder)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout, null)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskAdapter.TaskViewHolder, position: Int) {
        val item = list[position]
        holder.taskMessage.text = item.message
        if(item.alarmTime==null){
            holder.taskTime.visibility = View.GONE
        }
        else holder.taskTime.text = item.alarmTime.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}