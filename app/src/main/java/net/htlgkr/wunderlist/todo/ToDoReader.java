package net.htlgkr.wunderlist.todo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ToDoReader {
    private InputStream inputStream;

    public ToDoReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public List<ToDo> readToDoList() throws IOException {
        List<ToDo> todos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            String title = null;
            String description = null;
            boolean completed = false;
            LocalDateTime deadline = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("title: ")) {
                    title = line.substring("title: ".length());
                } else if (line.startsWith("description: ")) {
                    description = line.substring("description: ".length());
                } else if (line.startsWith("completed: ")) {
                    completed = Boolean.parseBoolean(line.substring("completed: ".length()));
                } else if (line.startsWith("deadline: ")) {
                    deadline = LocalDateTime.parse(line.substring("deadline: ".length()));
                } else if ((line.startsWith("end"))) {
                    if (title != null && description != null && deadline != null) {
                        ToDo toDo = new ToDo(title, description, completed, deadline);
                        todos.add(toDo);
                        title = null;
                        description = null;
                        completed = false;
                        deadline = null;
                    }
                }
            }
        }

        return todos;
    }
}
