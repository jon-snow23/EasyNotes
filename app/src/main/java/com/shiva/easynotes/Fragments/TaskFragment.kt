package com.shiva.easynotes.Fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.findFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.shiva.easynotes.Activities.MainActivity
import com.shiva.easynotes.Activities.NoteActivity
import com.shiva.easynotes.Adapters.TaskAdapter
import com.shiva.easynotes.AlarmScheduler
import com.shiva.easynotes.AlarmSchedulerImpl
import com.shiva.easynotes.Models.TaskItem
import com.shiva.easynotes.R
import com.shiva.easynotes.TaskRepository
import com.shiva.easynotes.TaskViewModel
import com.shiva.easynotes.databinding.FragmentTaskBinding
import java.time.LocalDateTime

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel
    private var popupWindow: PopupWindow? = null
    private var timePopupWindow : PopupWindow? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: TaskAdapter
    private lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = (activity as NoteActivity).viewModel
        sharedPreferences = (activity as NoteActivity).sharedPreferences
        alarmScheduler = AlarmSchedulerImpl(requireContext())
        setupRecyclerView()
        viewModel.savedTasks.observe(viewLifecycleOwner, Observer {
            Log.w("task-", it.toString())
            it?.let{
                adapter.setList(it)
            }
        })

        binding.addTaskFab.setOnClickListener{
            showAddTaskPopUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter()
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTask.adapter = adapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAddTaskPopUp(){
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.add_task_layout, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(popupView)

        val taskText = popupView.findViewById<TextView>(R.id.taskEditText)
        val saveBtn = popupView.findViewById<Button>(R.id.add_task_btn)
        val setReminderButton = popupView.findViewById<CardView>(R.id.set_reminder_btn)
        val cancelBtn = popupView.findViewById<Button>(R.id.cancel_task_btn)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var dateAndTime = MutableLiveData<LocalDateTime?>(null)
        saveBtn.setOnClickListener {
            if(taskText.text.isNotBlank()){
                saveTask(taskText.text.toString(), dateAndTime.value)
                alertDialog.dismiss()
            }
            else{
                showToast("Text cannot be empty")
            }
        }
        dateAndTime.observe(viewLifecycleOwner, Observer {
            it?.let{ data ->
                setReminderButton.findViewById<TextView>(R.id.set_reminder_text).text = data.toString()
            }
        })

        setReminderButton.setOnClickListener {
            showDateAndTimePicker(dateAndTime)
        }
        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateAndTimePicker(dateAndTime: MutableLiveData<LocalDateTime?>) {
        val timePopupView = LayoutInflater.from(requireContext()).inflate(R.layout.set_time_popup, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(timePopupView)

        val timePicker = timePopupView.findViewById<TimePicker>(R.id.timePicker)
        val datePicker = timePopupView.findViewById<DatePicker>(R.id.datePicker)
        val cancelBtn = timePopupView.findViewById<Button>(R.id.cancel_time_btn)
        val addTimeBtn = timePopupView.findViewById<Button>(R.id.set_time_btn)

        val alertDailog = builder.create()

        var time: LocalDateTime? = null
        alertDailog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        cancelBtn.setOnClickListener {
            alertDailog.dismiss()
        }
        addTimeBtn.setOnClickListener {
            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth

            val hour = timePicker.hour
            val minute = timePicker.minute

            time = LocalDateTime.of(year, month+1, day, hour, minute)
            dateAndTime.postValue(time)
            alertDailog.dismiss()
        }
        alertDailog.show()
    }

    private fun saveTask(message: String, dateAndTime: LocalDateTime?) {





        var id = sharedPreferences.getInt("task_id", -1)
        showToast(id.toString())
        val editor = sharedPreferences.edit()
        id+=1
        editor.putInt("task_id", id)
        editor.apply()
        val task = TaskItem(dateAndTime, message, id)
        viewModel.addNewTask(task)
        try{
            dateAndTime?.let{
                task.let { alarmScheduler::schedule}
            }
        }
        catch(e: Exception){
            Log.w("alarm-issue", "$e")
        }

    }

    private fun cancelTask(task: TaskItem){
        //add function to delete task for db

        task.alarmTime?.let{
            task.let { alarmScheduler::cancel}
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}