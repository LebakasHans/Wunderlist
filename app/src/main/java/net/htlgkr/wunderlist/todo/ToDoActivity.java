package net.htlgkr.wunderlist.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.htlgkr.wunderlist.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ToDoActivity extends AppCompatActivity {
    private FloatingActionButton finishedButton;
    private CalendarView deadlineCalendarView;
    private EditText title;
    private EditText description;
    private CheckBox isCompleteCheckBox;
    private LocalDateTime deadline;
    private TextView currentlyPickedDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_acticity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.toDoTitleTxt);
        description = findViewById(R.id.toDoDescriptionTxt);
        isCompleteCheckBox = findViewById(R.id.isCompleteCheckBox);
        setUpDate();
        setUpFinishButton();

        if (getIntent().hasExtra("inputToDo")){
            ToDo inputToDo = (ToDo) getIntent().getSerializableExtra("inputToDo");

            long date = ZonedDateTime.of(deadline, ZoneId.systemDefault()).toInstant().toEpochMilli();
            deadlineCalendarView.setDate(date);
            currentlyPickedDeadline.setText(DateTimeFormatter.ofPattern(ToDo.DATE_PATTERN).format(inputToDo.getDeadline()));
            isCompleteCheckBox.setChecked(inputToDo.isCompleted());
            title.setText(inputToDo.getTitle());
            description.setText(inputToDo.getDescription());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setUpDate() {
        deadlineCalendarView = findViewById(R.id.deadlineCV);
        currentlyPickedDeadline = findViewById(R.id.currentlyPickedDeadline);
        deadlineCalendarView.setMinDate(Instant.now().toEpochMilli());

        deadlineCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                new TimePickerDialog(ToDoActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                LocalDate date = LocalDate.of(year, month+1, dayOfMonth);
                                LocalTime time = LocalTime.of(hourOfDay, minute);
                                deadline = LocalDateTime.of(date, time);
                                currentlyPickedDeadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(deadline));
                            }
                        },LocalTime.now().getHour(), LocalTime.now().getMinute(), true)
                        .show();
            }
        });
    }

    private void setUpFinishButton() {
        finishedButton = findViewById(R.id.finishedToDoBtd);

        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!title.getText().toString().isEmpty()
                && !description.getText().toString().isEmpty()
                && deadline != null){
                    if (deadline.isBefore(LocalDateTime.now())){
                        Toast.makeText(ToDoActivity.this, "The deadline has to be after the current date!", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent();
                        intent.putExtra("outputToDo", new ToDo(
                                title.getText().toString()
                                , description.getText().toString()
                                , isCompleteCheckBox.isChecked()
                                , deadline
                        ));

                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }else{
                    Toast.makeText(ToDoActivity.this, "All the fields have to be filled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}