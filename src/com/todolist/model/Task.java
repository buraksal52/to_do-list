package com.todolist.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Task {
    private final int id;
    private final String description;
    private final String dayOfWeek;
    private final LocalTime timeOfDay;
    private final boolean isDone;
    private final String status;
    private final LocalDate completionDate;
    private final int weekNumber; // Yeni eklenen alan

    public Task(int id, String description, String dayOfWeek, LocalTime timeOfDay, boolean isDone, String status, LocalDate completionDate, int weekNumber) {
        this.id = id;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.timeOfDay = timeOfDay;
        this.isDone = isDone;
        this.status = status;
        this.completionDate = completionDate;
        this.weekNumber = weekNumber;
    }

    public Task(String description) {
        this(0, description, null, null, false, "POOL", null, 0);
    }

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getDayOfWeek() { return dayOfWeek; }
    public LocalTime getTimeOfDay() { return timeOfDay; }
    public boolean isDone() { return isDone; }
    public String getStatus() { return status; }
    public LocalDate getCompletionDate() { return completionDate; }
    public int getWeekNumber() { return weekNumber; } // Yeni getter

    @Override
    public String toString() {
        String timeStr = (timeOfDay != null) ? " (" + timeOfDay.format(DateTimeFormatter.ofPattern("HH:mm")) + ")" : "";
        String prefix = "";

        if ("COMPLETED".equals(status) && dayOfWeek != null) {
            prefix = "[" + dayOfWeek + "] ";
        } else if ("ARCHIVED".equals(status) && completionDate != null) {
            // Arşivlenmiş görev için hafta numarasını göster
            prefix = "[Hafta " + weekNumber + "] ";
        }

        return (isDone ? "✓ " : "") + prefix + description + timeStr;
    }
}