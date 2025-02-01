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

        titleEditText = findViewById(R.id.editTextTitle)
        descriptionEditText = findViewById(R.id.editTextDescription)
        dateButton = findViewById(R.id.buttonSelectDate)
        timeButton = findViewById(R.id.buttonSelectTime)
        saveButton = findViewById(R.id.buttonSaveTask)

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
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (title.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val taskDateTime = "$selectedDate $selectedTime"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = dateFormat.parse(taskDateTime)
        val startDateTime = DateTime(date)
        val endDateTime = DateTime(date.time + 3600000) // Assume task duration = 1 hour

        // Ask user if they want to add the task to Google Calendar
        Toast.makeText(this, "Task saved. Adding to Google Calendar...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                GoogleCalendarService.addEvent(
                    summary = title,
                    location = "No location",
                    description = description,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime
                )
                runOnUiThread {
                    Toast.makeText(this, "Task added to Google Calendar!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error adding task to Google Calendar", Toast.LENGTH_LONG).show()
                }
            }
        }.start()

        finish() // Close the activity after saving
    }
}

