package net.htlgkr.wunderlist.todo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ToDoWriter {
    private OutputStream outputStream;

    public ToDoWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeToDoList(List<ToDo> toDoList) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (ToDo toDo : toDoList) {
                writer.write("title: " + toDo.getTitle() + "\n");
                writer.write("description: " + toDo.getDescription() + "\n");
                writer.write("completed: " + toDo.isCompleted() + "\n");
                writer.write("deadline: " + toDo.getDeadline().toString() + "\n");
                writer.write("end\n");
            }
        }
    }
}