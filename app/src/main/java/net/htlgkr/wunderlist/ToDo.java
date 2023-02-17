package net.htlgkr.wunderlist;

import java.time.LocalDateTime;

public class ToDo {
    public static final String DATE_PATTERN = "dd.MM.yyyy HH:mm";

    private String title;
    private String notes;
    private LocalDateTime deadline;

    public ToDo(String title, String notes, LocalDateTime date){
        this.title = title;
        this.notes = notes;
        this.deadline = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
