package schmitt_florian.schoolplanner.gui;

import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.Settings;
import schmitt_florian.schoolplanner.logic.objects.Homework;
import schmitt_florian.schoolplanner.logic.objects.Subject;


public class HomeworkDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private View rootView;
    private Homework showingHomework;
    private Subject[] subjectsInSpinner;
    private boolean addMode;
    private Button dateButton;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private int day;
    private int month;
    private int year;
    private View view;
    private String date;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        view = findViewById(R.id.homeworkDetails_main);

        dbHelper = new DatabaseHelperImpl(this);
        int homeworkID = getIntent().getIntExtra("HomeworkID", -1);
        if (homeworkID <= 0) {
            addMode = true;
        } else {
            addMode = false;
            showingHomework = dbHelper.getHomeworkAtId(homeworkID);
        }

        getDateForDatePicker();

        rootView = findViewById(R.id.homeworkDetails_main);
        initGUI();
    }


    public void onSaveClick(View view) {
        try {
            if (addMode) {
                dbHelper.insertIntoDB(readHomeworkFromGUI());
            } else {
                dbHelper.updateHomeworkAtId(readHomeworkFromGUI());
            }
            finish();
        } catch (IllegalArgumentException ignored) {
        }
    }

 
    public void onDeleteClick(View view) {
        dbHelper.deleteHomeworkAtId(showingHomework.getId());
    }

    public void onCloseClick(View view) {
        finish();
    }


    private void initGUI() {
        if (!addMode) {
            GuiHelper.setTextToTextView(rootView, R.id.homeworkDetails_textDescription, showingHomework.getDescription());
            GuiHelper.setTextToTextView(rootView, R.id.homeworkDetails_textDate,
                    GuiHelper.extractGuiString(showingHomework.getDeadline(), false, rootView.getContext()));
            ((SwitchCompat) findViewById(R.id.homeworkDetails_switchDone)).setChecked(showingHomework.isDone());

            GuiHelper.setVisibility(rootView, R.id.homeworkDetails_buttonDelete, View.VISIBLE);
            GuiHelper.setVisibility(rootView, R.id.homeworkDetails_switchDone, View.VISIBLE);
        } else {
            GuiHelper.setVisibility(rootView, R.id.homeworkDetails_buttonDelete, View.GONE);
            GuiHelper.setVisibility(rootView, R.id.homeworkDetails_switchDone, View.INVISIBLE);
        }

        subjectsInSpinner = fillSpinner();

        //preselect spinner
        if (!addMode) {
            for (int i = 0; i < subjectsInSpinner.length; i++) {
                if (subjectsInSpinner[i].match(showingHomework.getSubject())) {
                    Spinner spinner = findViewById(R.id.homeworkDetails_spinnerSubject);
                    spinner.setSelection(i);
                }
            }
        }
        implementDatePicker();
    }


    private Subject[] fillSpinner() {
        ArrayList<String> subjectStrings = new ArrayList<>();
        ArrayList<Subject> subjectArrayList = new ArrayList<>();

        int[] subjectIndices = dbHelper.getIndices(DatabaseHelper.TABLE_SUBJECT);

        for (int subjectIndex : subjectIndices) {
            Subject subject = dbHelper.getSubjectAtId(subjectIndex);

            subjectStrings.add(GuiHelper.extractGuiString(subject));
            subjectArrayList.add(subject);
        }

        if (subjectStrings.size() != 0) {
            GuiHelper.fillSpinnerFromArray(rootView, R.id.homeworkDetails_spinnerSubject, subjectStrings.toArray(new String[0]));
        } else {
            GuiHelper.setVisibility(rootView, R.id.homeworkDetails_labelSpinnerError, View.VISIBLE);
            findViewById(R.id.homeworkDetails_buttonSave).setEnabled(false);
        }
        return subjectArrayList.toArray(new Subject[0]);
    }

    private Homework readHomeworkFromGUI() throws IllegalArgumentException {
        Spinner spinner = findViewById(R.id.homeworkDetails_spinnerSubject);
        SwitchCompat switchCompat = findViewById(R.id.homeworkDetails_switchDone);

        if (addMode) {
            return new Homework(
                    -1,
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.homeworkDetails_textDescription),
                    GuiHelper.getDateFromMandatoryButton(rootView, R.id.homeworkDetails_textDate),
                    false
            );
        } else {
            return new Homework(
                    showingHomework.getId(),
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.homeworkDetails_textDescription),
                    GuiHelper.getDateFromMandatoryButton(rootView, R.id.homeworkDetails_textDate),
                    switchCompat.isChecked()
            );
        }

    }

    private void implementDatePicker() {
        dateButton = findViewById(R.id.homeworkDetails_textDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(
                        HomeworkDetailsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                updateDateForDatePicker(day, month, year);

                month = month + 1;
                dateButton.setText(formatDate(day, month, year));
            }
        };
    }

    private void getDateForDatePicker() {

        if (addMode) {
            Calendar cal = Calendar.getInstance();
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);
            System.out.println();
        } else {
            day = showingHomework.getDeadline().get(Calendar.DAY_OF_MONTH);
            month = showingHomework.getDeadline().get(Calendar.MONTH);
            year = showingHomework.getDeadline().get(Calendar.YEAR);
        }
    }

    private String formatDate(int day, int month, int year) {
        switch (Settings.getInstance(view.getContext()).getActiveDateFormat()) {
            case "DD.MM.YYYY":
                date = day + "." + month + "." + year;
                break;
            case "MM.DD.YYYY":
                date = month + "." + day + "." + year;
                break;
            case "YYYY.MM.DD":
                date = year + "." + month + "." + day;
        }
        return date;
    }

    private void updateDateForDatePicker(int day, int month, int year) {

        this.day = day;
        this.month = month;
        this.year = year;

    }

    
}


