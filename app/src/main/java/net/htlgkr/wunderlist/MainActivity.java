package net.htlgkr.wunderlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.htlgkr.wunderlist.todo.ToDo;
import net.htlgkr.wunderlist.todo.ToDoAdapter;
import net.htlgkr.wunderlist.todo.ToDoContract;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //TODO safe
    //TODO load
    //TODO fix margin

    private ActivityResultLauncher<ToDo> toDoLauncher;
    private ActivityResultLauncher<Intent> preferencesLauncher;
    private List<ToDo> todoList;
    private ListView todoListView;
    private ToDoAdapter mAdapter;
    private SharedPreferences prefs ;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;
    private boolean nextToDoIsEdited = false;
    private int posOfEditedToDo = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToDoLauncher();
        setUpSharedPreferences();
        setUpPreferencesLauncher();
        setUpListView();

        for (int i = 0; i < 10; i++) {
            addItem(new ToDo(
                "Test" + i,
                    "Description",
                    false,
                    LocalDateTime.now()
            ));
        }
    }

    private void setUpListView() {
        todoList = new ArrayList<>();
        todoListView = findViewById(R.id.toDoItemsLV);
        mAdapter = new ToDoAdapter(this, R.layout.todo_layout, todoList, prefs);
        todoListView.setAdapter(mAdapter);

        registerForContextMenu(todoListView);
    }

    private void setUpSharedPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = ( sharedPrefs , key ) -> preferenceChanged(sharedPrefs, key);
        prefs.registerOnSharedPreferenceChangeListener( preferencesChangeListener );
    }

    private void setUpPreferencesLauncher() {
        preferencesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //can't be null because then app crashes when changing preferences twice
            }
        });
    }

    private void setUpToDoLauncher() {
        toDoLauncher = registerForActivityResult(new ToDoContract(), new ActivityResultCallback<ToDo>() {
            @Override
            public void onActivityResult(ToDo result) {
                if (result != null) {
                    if (nextToDoIsEdited){
                        deleteItem(posOfEditedToDo);
                    }
                    addItem(result);
                }
                nextToDoIsEdited = false;
                posOfEditedToDo = -1;
            }
        });
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
        getMenuInflater().inflate(R.menu.main_actionbar_menu, menu);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        int viewId = v.getId();
        if (viewId == R.id.toDoItemsLV) {
            getMenuInflater().inflate(R.menu.listview_item_context_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.context_edit){
            if ( info != null ) {
                int pos = info.position;
                nextToDoIsEdited = true;
                posOfEditedToDo = pos;
                toDoLauncher.launch(todoList.get(pos));
            }
        }else if(item.getItemId() == R.id.context_delete){
            if ( info != null ) {
                int pos = info.position;
                deleteItem(pos);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        todoList.remove(index);
        mAdapter.notifyDataSetChanged();
    }
}