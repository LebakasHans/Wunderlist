package net.htlgkr.wunderlist;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.htlgkr.wunderlist.todo.ToDo;
import net.htlgkr.wunderlist.todo.ToDoContract;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<ToDo> toDoLauncher;
    private ActivityResultLauncher<Intent> preferencesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toDoLauncher = registerForActivityResult(new ToDoContract(), new ActivityResultCallback<ToDo>() {
            @Override
            public void onActivityResult(ToDo result) {
                if (result != null) {
                    //TODO add toDo object
                }
            }
        });
        preferencesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_actionbar) {
            Intent intent = new Intent(this,
                    SettingsActivity.class);
            preferencesLauncher.launch(intent);
        }else if (item.getItemId() == R.id.newToDo_actionbar){
            toDoLauncher.launch(null);
        }
        return super.onOptionsItemSelected(item);
    }
}