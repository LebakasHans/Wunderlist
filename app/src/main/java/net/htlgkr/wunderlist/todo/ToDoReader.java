package net.htlgkr.wunderlist.todo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.htlgkr.wunderlist.LocalDateTimeDeserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToDoReader {
    private static final Type type = new TypeToken<Map<String, List<ToDo>>>(){}.getType();
    private InputStream inputStream;

    public ToDoReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Map<String, List<ToDo>> readToDos() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());

        Gson gson = gsonBuilder.setPrettyPrinting().create();
        String json;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                json = bufferedReader.lines().collect(Collectors.joining("\n"));
        }

        Map<String, List<ToDo>> categoryMap = gson.fromJson(json, type);
        if (categoryMap == null) {
            categoryMap = new HashMap<>();
        }
        return categoryMap;
    }
}
