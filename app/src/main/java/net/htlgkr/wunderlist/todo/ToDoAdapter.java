package net.htlgkr.wunderlist.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.htlgkr.wunderlist.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToDoAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private Context context;
    private int layoutId;
    private List<ToDo> originalToDoList;
    private List<ToDo> filteredToDoList;
    private SharedPreferences preferences;

    public ToDoAdapter(Context context, int layoutId, List<ToDo> toDoList, SharedPreferences preferences) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.layoutId = layoutId;
        this.originalToDoList = toDoList;
        this.preferences = preferences;
    }

    @Override
    public int getCount() {
        boolean showOverdueNotes = preferences.getBoolean("showOverdueNotes", true);
        return filterToDoList(showOverdueNotes).size();
    }

    @Override
    public Object getItem(int position) {
        return originalToDoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean showOverdueNotes = preferences.getBoolean("showOverdueNotes", true);
        filteredToDoList = new ArrayList<>(
                filterToDoList(showOverdueNotes)
        );

        ToDo givenToDo = filteredToDoList.get(position);
        View listItem = (convertView == null) ? inflater.inflate(this.layoutId, null) : convertView;;

        listItem = adaptView(listItem, givenToDo);

        return listItem;
    }

    @NonNull
    private View adaptView(View listItem, ToDo givenToDo) {
        listItem.setLongClickable(true);

        ((TextView) listItem.findViewById(R.id.toDoLayoutTitle)).setText(givenToDo.getTitle());
        ((CheckBox) listItem.findViewById(R.id.toDoLayoutIsCompleteCheckBox)).setChecked(givenToDo.isCompleted());
        ((TextView) listItem.findViewById(R.id.toDoLayoutDeadline)).setText(givenToDo.getDeadline().format(DateTimeFormatter.ofPattern(ToDo.DATE_PATTERN)));
        ((TextView) listItem.findViewById(R.id.toDoLayoutDescription)).setText(givenToDo.getDescription());


        if (givenToDo.isCompleted()) {
            setPreference("doneNoteBackground", R.color.green, listItem);
        }
        else if(givenToDo.getDeadline().isBefore(LocalDateTime.now())){
            setPreference("overdueNoteBackground", R.color.red, listItem);
        }

        ((CheckBox) listItem .findViewById(R.id.toDoLayoutIsCompleteCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                givenToDo.setCompleted(isChecked);
                if (isChecked) {
                    setPreference("doneNoteBackground", R.color.green, listItem);
                } else if(givenToDo.getDeadline().isBefore(LocalDateTime.now())){
                    setPreference("overdueNoteBackground", R.color.red, listItem);
                } else {
                    setBackgroundColorOfNote(listItem, "#FFFFFF");
                }
            }
        });

        return listItem;
    }

    private void setPreference(String key, int defaultValue, View listItem) {
        String hexCode;
        int color = preferences.getInt(key, Integer.MAX_VALUE);
        if (color == Integer.MAX_VALUE){
            hexCode = idToHexString(defaultValue);
        }else {
            hexCode = String.format("#%06X", (0xFFFFFF & color));
        }

        setBackgroundColorOfNote(listItem, hexCode);
    }

    private String idToHexString(int id){
        return "#" + Integer.toHexString(ContextCompat.getColor(context, id));
    }

    //Why the f*** does this work
    private void setBackgroundColorOfNote(View listItem, String color) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.border);
        GradientDrawable gradientDrawable  = (GradientDrawable) drawable;
        gradientDrawable.setColor(Color.parseColor(color));
        if(listItem != null)
            listItem.setBackground(gradientDrawable);
    }

    private List<ToDo> filterToDoList(boolean showOverdueToDos){
        if (!showOverdueToDos) {
            List<ToDo> list = new ArrayList<>(originalToDoList);
            list = list.stream()
                    .filter(toDo -> toDo.getDeadline().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            return list;
        }else {
            return originalToDoList;
        }
    }
}
