package com.example.taskmanagerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanagerapp.services.GoogleCalendarService
import com.google.api.client.util.DateTime
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var saveButton: Button
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Match IDs with activity_add_task.xml
        titleEditText = findViewById(R.id.inputTitle)  // ✅ Fixed ID
        descriptionEditText = findViewById(R.id.inputDescription)  // ✅ Fixed ID
        dateButton = findViewById(R.id.buttonSelectDate)  // ✅ Fixed ID
        timeButton = findViewById(R.id.buttonSelectTime)  // ✅ Fixed ID
        saveButton = findViewById(R.id.btnSaveTask)  // ✅ Fixed ID

        dateButton.setOnClickListener { showDatePicker() }
        timeButton.setOnClickListener { showTimePicker() }
        saveButton.setOnClickListener { saveTask() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            dateButton.text = selectedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            selectedTime = "$hourOfDay:$minute"
            timeButton.text = selectedTime
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        timePicker.show()
    }

    private fun saveTask() {
        val title = titleEditText

    }}


