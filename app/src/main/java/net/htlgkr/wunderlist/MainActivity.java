package net.htlgkr.wunderlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import net.htlgkr.wunderlist.todo.ToDo;
import net.htlgkr.wunderlist.todo.ToDoAdapter;
import net.htlgkr.wunderlist.todo.ToDoContract;
import net.htlgkr.wunderlist.todo.ToDoReader;
import net.htlgkr.wunderlist.todo.ToDoWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int RQ_WRITE_STORAGE = 12345;
    private static final String saveFilename = "savedToDos.json";
    private Map<String, List<ToDo>> toDoCategoryMap;
    private Spinner categorySpinner;
    private ActivityResultLauncher<ToDo> toDoLauncher;
    private ActivityResultLauncher<Intent> preferencesLauncher;
    private List<ToDo> todoList;
    private ListView todoListView;
    private ToDoAdapter mAdapter;
    private ArrayAdapter categoryAdapter;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;
    private Comparator<ToDo> timeComparator;
    private boolean nextToDoIsEdited = false;
    private int posOfEditedToDo = -1;
    private boolean writeToExternalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeComparator = new Comparator<ToDo>() {
            @Override
            public int compare(ToDo o1, ToDo o2) {
                if (o1.getDeadline().isAfter(o2.getDeadline())){
                    return 1;
                }else if (o1.getDeadline().isBefore(o2.getDeadline())){
                    return -1;
                }
                return 0;
            }
        };

        toDoCategoryMap = new HashMap<>();

        setUpToDoLauncher();
        setUpSharedPreferences();
        setUpPreferencesLauncher();
        setUpListView();
        loadToDos();
        setUpActionbar();
        setUpCategorySpinner(); //Note: also sets toDoList
    }

    private void loadToDos() {
        File saveFile = new File(getFilesDir(), saveFilename);
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
                List<ToDo> defaultList = new ArrayList<>();
                defaultList.add(new ToDo(
                        "Getting Started",
                        "Thanks for using 'Wunderlist'! This is a default ToDo to get you started. Create your first own Category by holding the Menu saying 'Default' on the Top",
                        false,
                        LocalDateTime.now().plusDays(7)
                ));

                toDoCategoryMap.put("Default", defaultList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                ToDoReader toDoReader;

                if (writeToExternalStorage){
                    toDoReader = new ToDoReader(
                            new FileInputStream(new File(getExternalFilesDir(null), saveFilename)
                    ));
                }else{
                    toDoReader = new ToDoReader(
                            openFileInput(saveFilename)
                    );
                }
                toDoCategoryMap.putAll(toDoReader.readToDos());

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Load", "Failed to load ToDo's from file");
            }
        }
    }

    private void setUpCategorySpinner() {
        categorySpinner = findViewById(R.id.toDo_Category_spinner);
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(toDoCategoryMap.keySet())
        );
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = (String) parent.getItemAtPosition(position);
                List<ToDo> newToDoList =  toDoCategoryMap.get(category);
                if(newToDoList != null){
                    todoList = newToDoList;
                    mAdapter.setToDoList(newToDoList);
                }else {
                    Toast.makeText(MainActivity.this, "Error! Category not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        categorySpinner.setSelection(0);

        //spinner doesn't support long click events so i used this workaround
        final Handler actionHandler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder newCategoryDialog = new AlertDialog.Builder(MainActivity.this);
                newCategoryDialog.setTitle("New Category");
                TextInputEditText input = new TextInputEditText(MainActivity.this);
                input.setHint("Enter Category name");
                newCategoryDialog.setView(input);
                newCategoryDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCategory = input.getText().toString();
                        if (newCategory.isEmpty()){
                            Toast.makeText(MainActivity.this, "You have to enter a Category name", Toast.LENGTH_SHORT).show();
                        } else if (toDoCategoryMap.containsKey(newCategory)) {
                            Toast.makeText(MainActivity.this, "Category already exists", Toast.LENGTH_SHORT).show();
                        }else {
                            toDoCategoryMap.put(newCategory, new ArrayList<>());
                            categoryAdapter.add(newCategory);
                            categoryAdapter.notifyDataSetChanged();
                        }
                    }
                });
                newCategoryDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newCategoryDialog.show();
            }
        };

        categorySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    actionHandler.postDelayed(runnable, 1000);
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    actionHandler.removeCallbacks(runnable);
                }
                return false;
            }
        });
    }

    private void setUpActionbar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.main_actionbar_menu_layout);

        findViewById(R.id.newToDo_actionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categorySpinner.getSelectedItem() != null) {
                    if (categorySpinner.getSelectedItem() != null) {
                        toDoLauncher.launch(null);
                    } else {
                        Toast.makeText(MainActivity.this, "Please select a category first!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.settings_actionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                preferencesLauncher.launch(intent);
            }
        });

        findViewById(R.id.settings_deleteCategory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteCategoryDialog = new AlertDialog.Builder(MainActivity.this);
                deleteCategoryDialog.setTitle("Delete Category?");
                deleteCategoryDialog.setMessage("Are you sure you want to delete this Category? This will delete all ToDos in this Category!");
                deleteCategoryDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = (String) categorySpinner.getSelectedItem();
                        if (category != null) {
                            toDoCategoryMap.remove(category);
                            categoryAdapter.remove(category);
                            todoList.clear();
                            mAdapter.setToDoList(new ArrayList<>()); //sets to empty list
                            if (categorySpinner.getCount() > 0) {
                                categorySpinner.setSelection(0);
                            }else {
                                Toast.makeText(MainActivity.this, "You do not have any category's left", Toast.LENGTH_SHORT).show();
                            }
                            categoryAdapter.notifyDataSetChanged();
                        }
                    }
                });
                deleteCategoryDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                deleteCategoryDialog.show();
            }
        });
    }

    @Override
    protected void onStop() {
        saveToDos();
        super.onStop();
    }

    private void saveToDos(){
        try {
            ToDoWriter toDoWriter;
            if (writeToExternalStorage){
                toDoWriter = new ToDoWriter(
                        new FileOutputStream(new File(getExternalFilesDir(null), saveFilename))
                );
            }else {
                toDoWriter = new ToDoWriter(
                        openFileOutput(saveFilename, MODE_PRIVATE)
                );
            }

            toDoWriter.writeToDos(toDoCategoryMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("save", "failed to save ToDos");
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
        preferencesChangeListener = ( sharedPrefs , key ) -> onPreferenceChanged(key);
        prefs.registerOnSharedPreferenceChangeListener( preferencesChangeListener );
        writeToExternalStorage = prefs.getBoolean("writeToExternalStorage", false);
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
                        System.out.println("WTF");
                    }
                    addItem(result);
                }
                nextToDoIsEdited = false;
                posOfEditedToDo = -1;
            }
        });
    }

    private void onPreferenceChanged(String key) {
        todoListView.setAdapter(mAdapter);

        if (prefs.getBoolean("writeToExternalStorage", false) != writeToExternalStorage){
            writeToExternalStorage = prefs.getBoolean("writeToExternalStorage", false);
            if (key.equals("writeToExternalStorage") && writeToExternalStorage){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission needed");
                    builder.setMessage("This permission is needed to access the external storage.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RQ_WRITE_STORAGE);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                else if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) //Read External Storage and not Write External Storage because the latter is deleted after API Level 29
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions (new String [] {android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            RQ_WRITE_STORAGE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RQ_WRITE_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                writeToExternalStorage = true;
            }else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                writeToExternalStorage = false;
                prefs.edit().putBoolean("writeToExternalStorage", false).apply();
            }
            copySavesToNewMedium(writeToExternalStorage);
        }
    }

    private void copySavesToNewMedium(boolean writeToExternalStorage) {
        if (writeToExternalStorage){
            try {
                ToDoWriter toDoWriter = new ToDoWriter(
                        new FileOutputStream(new File(getExternalFilesDir(null), saveFilename))
                );
                toDoWriter.writeToDos(toDoCategoryMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("save", "failed to save ToDos");
            }
        }else {
            try {
                ToDoWriter toDoWriter = new ToDoWriter(
                        openFileOutput(saveFilename, MODE_PRIVATE)
                );
                toDoWriter.writeToDos(toDoCategoryMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("save", "failed to save ToDos");
            }
        }
    }

    private void addItem(ToDo toDo) {
        todoList.add(toDo);
        Collections.sort(todoList, timeComparator);
        mAdapter.notifyDataSetChanged();
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
                toDoLauncher.launch(mAdapter.getFilteredToDoList().get(pos));
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
        todoList.remove(mAdapter.getFilteredToDoList().get(index));
        mAdapter.getFilteredToDoList().remove(index);
        mAdapter.notifyDataSetChanged();
    }
}