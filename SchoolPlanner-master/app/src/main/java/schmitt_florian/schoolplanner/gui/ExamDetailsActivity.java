package schmitt_florian.schoolplanner.gui;

import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import schmitt_florian.schoolplanner.logic.objects.Exam;
import schmitt_florian.schoolplanner.logic.objects.Subject;


public class ExamDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private View rootView;
    private Exam showingExam;
    private Subject[] subjectsInSpinner;
    private boolean addMode;
    private Button dateButton;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private int day;
    private int month;
    private int year;
    private String date;
    private View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        view = findViewById(R.id.examDetails_main);

        dbHelper = new DatabaseHelperImpl(this);
        int examID = getIntent().getIntExtra("ExamID", -1);
        if (examID <= 0) {
            addMode = true;
        } else {
            addMode = false;
            showingExam = dbHelper.getExamAtId(examID);
        }

        getDateForDatePicker();

        rootView = findViewById(R.id.examDetails_main);
        initGUI();
    }

    public void onSaveClick(View view) {
        try {
            if (addMode) {
                dbHelper.insertIntoDB(readHomeworkFromGUI());
            } else {
                dbHelper.updateExamAtId(readHomeworkFromGUI());
            }
            finish();
        } catch (IllegalArgumentException ignored) {
        }
    }


    public void onDeleteClick(View view) {
        dbHelper.deleteExamAtId(showingExam.getId());
    }


    public void onCloseClick(View view) {
        finish();
    }


    private void initGUI() {
        if (!addMode) {
            GuiHelper.setTextToTextView(rootView, R.id.examDetails_textDescription, showingExam.getDescription());
            GuiHelper.setTextToTextView(rootView, R.id.examDetails_textDate,
                    GuiHelper.extractGuiString(showingExam.getDeadline(), false, rootView.getContext()));

            GuiHelper.setVisibility(rootView, R.id.examDetails_buttonDelete, View.VISIBLE);
        } else {
            GuiHelper.setVisibility(rootView, R.id.examDetails_buttonDelete, View.GONE);
        }

        subjectsInSpinner = fillSpinner();

       
        if (!addMode) {
            for (int i = 0; i < subjectsInSpinner.length; i++) {
                if (subjectsInSpinner[i].match(showingExam.getSubject())) {
                    Spinner spinner = findViewById(R.id.examDetails_spinnerSubject);
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
            GuiHelper.fillSpinnerFromArray(rootView, R.id.examDetails_spinnerSubject, subjectStrings.toArray(new String[0]));
        } else {
            GuiHelper.setVisibility(rootView, R.id.examDetails_labelSpinnerError, View.VISIBLE);
            findViewById(R.id.examDetails_buttonSave).setEnabled(false);
        }
        return subjectArrayList.toArray(new Subject[0]);
    }


    private Exam readHomeworkFromGUI() throws IllegalArgumentException {
        Spinner spinner = findViewById(R.id.examDetails_spinnerSubject);

        if (addMode) {
            return new Exam(
                    -1,
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.examDetails_textDescription),
                    GuiHelper.getDateFromMandatoryButton(rootView, R.id.examDetails_textDate)
            );
        } else {
            return new Exam(
                    showingExam.getId(),
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.examDetails_textDescription),
                    GuiHelper.getDateFromMandatoryButton(rootView, R.id.examDetails_textDate)
            );
        }
    }

    private void implementDatePicker() {
        dateButton = findViewById(R.id.examDetails_textDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(
                        ExamDetailsActivity.this,
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
        } else {
            day = showingExam.getDeadline().get(Calendar.DAY_OF_MONTH);
            month = showingExam.getDeadline().get(Calendar.MONTH);
            year = showingExam.getDeadline().get(Calendar.YEAR);
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
