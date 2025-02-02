package com.example.taskmanagerapp.services

import android.content.Context
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar as GoogleCalendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import java.io.IOException

object GoogleCalendarService {
    private lateinit var calendarService: GoogleCalendar

    fun init(context: Context) {
        try {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val credentials = GoogleAuthService.getCredentials(context)

            calendarService = GoogleCalendar.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),  // ✅ Fixed: Using Gson instead of Jackson
                credentials
            ).setApplicationName("TaskManagerApp").build()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Google Calendar Service", e)
        }
    }

    /**
     * Lists upcoming events from the Google Calendar.
     *
     * @param maxResults Maximum number of events to retrieve.
     */
    fun listUpcomingEvents(maxResults: Int = 10) {
        try {
            val now = DateTime(System.currentTimeMillis())
            val events = calendarService.events().list("primary")
                .setMaxResults(maxResults)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()

            if (events.items.isEmpty()) {
                println("No upcoming events found.")
            } else {
                for (event in events.items) {
                    val start = event.start.dateTime ?: event.start.date
                    println("Event: ${event.summary} | Start: $start")
                }
            }
        } catch (e: IOException) {
            println("Error retrieving events: ${e.localizedMessage}")
        }
    }

    /**
     * Adds a new event to the Google Calendar.
     *
     * @param summary Event title.
     * @param location Event location (optional).
     * @param description Event description (optional).
     * @param startDateTime Start date & time.
     * @param endDateTime End date & time.
     * @return Created Event object.
     */
    fun addEvent(summary: String, location: String?, description: String?, startDateTime: DateTime, endDateTime: DateTime): Event? {
        return try {
            val event = Event()
                .setSummary(summary)
                .setLocation(location)
                .setDescription(description)
                .setStart(EventDateTime().setDateTime(startDateTime).setTimeZone("America/Toronto"))
                .setEnd(EventDateTime().setDateTime(endDateTime).setTimeZone("America/Toronto"))

            calendarService.events().insert("primary", event).execute()
        } catch (e: IOException) {
            println("Failed to add event: ${e.localizedMessage}")
            null
        }
    }
}

