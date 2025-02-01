package com.example.taskmanagerapp.services

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime

object GoogleCalendarService {
    private val calendarService: Calendar by lazy {
        val httpTransport = com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport()
        Calendar.Builder(httpTransport, com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(), GoogleAuthService.getCredentials())
            .setApplicationName("TaskManagerApp")
            .build()
    }

    fun listUpcomingEvents(maxResults: Int = 10) {
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
                println("${event.summary} ($start)")
            }
        }
    }

    fun addEvent(summary: String, location: String?, description: String?, startDateTime: DateTime, endDateTime: DateTime): Event {
        val event = Event()
            .setSummary(summary)
            .setLocation(location)
            .setDescription(description)
            .setStart(EventDateTime().setDateTime(startDateTime).setTimeZone("America/Toronto"))
            .setEnd(EventDateTime().setDateTime(endDateTime).setTimeZone("America/Toronto"))

        return calendarService.events().insert("primary", event).execute()
    }
}
