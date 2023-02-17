package net.htlgkr.wunderlist.todo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ToDo implements Serializable {
    public static final String DATE_PATTERN = "dd.MM.yyyy HH:mm";

    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime deadline;

    public ToDo(String title, String description, boolean completed, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
