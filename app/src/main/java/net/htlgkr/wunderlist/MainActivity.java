package net.htlgkr.wunderlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import net.htlgkr.wunderlist.todo.ToDo;
import net.htlgkr.wunderlist.todo.ToDoAdapter;
import net.htlgkr.wunderlist.todo.ToDoContract;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<ToDo> toDoLauncher;
    private ActivityResultLauncher<Intent> preferencesLauncher;
    private List<ToDo> todoList;
    private ListView todoListView;
    private ToDoAdapter mAdapter;
    private SharedPreferences prefs ;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toDoLauncher = registerForActivityResult(new ToDoContract(), new ActivityResultCallback<ToDo>() {
            @Override
            public void onActivityResult(ToDo result) {
                if (result != null) {
                    addItem(result);
                }
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = ( sharedPrefs , key ) -> preferenceChanged(sharedPrefs, key);
        prefs.registerOnSharedPreferenceChangeListener( preferencesChangeListener );


        preferencesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //can't be null because then app crashes when changing preferences twice
            }
        });

        todoList = new ArrayList<>();
        todoListView = findViewById(R.id.toDoItemsLV);
        mAdapter = new ToDoAdapter(this, R.layout.todo_layout, todoList, prefs);
        todoListView.setAdapter(mAdapter);
        //TODO delete
        addItem(new ToDo(
                "Title",
                "description",
                true,
                LocalDateTime.now().minusDays(10)
                ));
    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        todoListView.setAdapter(mAdapter);
    }

    private void addItem(ToDo toDo) {
        todoList.add(toDo);
        mAdapter.notifyDataSetChanged();
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