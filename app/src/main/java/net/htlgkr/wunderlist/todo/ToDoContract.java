package net.htlgkr.wunderlist.todo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;

public class ToDoContract extends ActivityResultContract<ToDo, ToDo> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, ToDo input) {
        Intent intent = new Intent(context, ToDoActivity.class);
        intent.putExtra("inputToDo", input);
        return intent;
    }

    @Override
    public ToDo parseResult(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            return (ToDo) data.getSerializableExtra("outputToDo");
        } else {
            return null;
        }
    }
}

