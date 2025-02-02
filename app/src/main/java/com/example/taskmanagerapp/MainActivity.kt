package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var dbHelper: TaskDatabaseHelper
    private val taskList: MutableList<Task> = mutableListOf()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val RC_SIGN_IN = 100

    companion object {
        const val NEW_TASK_EXTRA = "NEW_TASK"
        const val EDIT_TASK_EXTRA = "EDIT_TASK"
        private const val TAG = "MainActivity"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize database and views
        dbHelper = TaskDatabaseHelper(this)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Setup adapter with click listener
        taskAdapter = TaskAdapter { task, position -> showTaskOptions(task, position) }
        recyclerViewTasks.adapter = taskAdapter

        // Floating action button to add a task
        val fabAddTask = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fabAddTask.setOnClickListener { navigateToAddTask() }

        // Load tasks and sync with Firebase
        loadTasksFromDatabase()
        setupFirebaseListener()

        // Initialize Google Sign-In
        initializeGoogleSignIn()
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleSignIn = findViewById<SignInButton>(R.id.sign_in)
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Toast.makeText(this, "Sign-In Successful: ${account.email}", Toast.LENGTH_LONG).show()
                syncWithGoogleCalendar(account)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-In Failed: ${e.statusCode}", e)
            Toast.makeText(this, "Sign-In Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun syncWithGoogleCalendar(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf("https://www.googleapis.com/auth/calendar")
        )
        credential.selectedAccount = account.account

        val calendarService = Calendar.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("TaskManagerApp")
            .build()

        lifecycleScope.launch {
            try {
                val event = Event()
                    .setSummary("TaskManager Event")
                    .setDescription("This is a test event added by TaskManager")

                val startDateTime = EventDateTime()
                    .setDateTime(DateTime("2025-01-24T10:00:00-05:00"))
                    .setTimeZone("America/New_York")
                event.start = startDateTime

                val endDateTime = EventDateTime()
                    .setDateTime(DateTime("2025-01-24T11:00:00-05:00"))
                    .setTimeZone("America/New_York")
                event.end = endDateTime

                withContext(Dispatchers.IO) {
                    calendarService.events().insert("primary", event).execute()
                }

                Toast.makeText(this@MainActivity, "Event added to Google Calendar", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync with Google Calendar", e)
                Toast.makeText(this@MainActivity, "Failed to add event: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.setTasks(taskList)
    }

    private fun setupFirebaseListener() {
        firebaseDatabase.getReference("tasks")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    taskList.clear()

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null && task.firebaseKey.isNotEmpty()) {
                            dbHelper.upsertTask(task)
                        } else {
                            Log.e(TAG, "Task missing Firebase key: $task")
                        }
                    }

                    taskList.addAll(dbHelper.getAllTasks())
                    taskAdapter.setTasks(taskList)
                    taskAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Tasks updated from Firebase")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase listener cancelled: ${error.message}")
                    Toast.makeText(this@MainActivity, "Failed to load tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToAddTask() {
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivity(intent)
    }

    private fun showTaskOptions(task: Task, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> navigateToEditTask(task)
                    1 -> deleteTask(task, position)
                }
            }
            .show()
    }

    private fun navigateToEditTask(task: Task) {
        val intent = Intent(this, EditTaskActivity::class.java).apply {
            putExtra(EDIT_TASK_EXTRA, task)
        }
        startActivity(intent)
    }

    private fun deleteTask(task: Task, position: Int) {
        dbHelper.deleteTask(task.id)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)

        firebaseDatabase.getReference("tasks").child(task.firebaseKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete task from Firebase", Toast.LENGTH_SHORT).show()
            }
    }
}


