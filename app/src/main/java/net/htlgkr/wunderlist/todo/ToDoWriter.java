package net.htlgkr.wunderlist.todo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ToDoWriter {
    private OutputStream outputStream;

    public ToDoWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeToDos(Map<String, List<ToDo>> toDoCategoryMap) throws IOException {
        if (toDoCategoryMap != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());

            Gson gson = gsonBuilder.setPrettyPrinting().create();
            String json = gson.toJson(toDoCategoryMap);
            try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)){
                outputStreamWriter.write(json);
            }
        }
    }
}