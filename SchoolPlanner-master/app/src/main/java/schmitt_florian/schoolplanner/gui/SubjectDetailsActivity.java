package schmitt_florian.schoolplanner.gui;


import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.objects.Subject;
import schmitt_florian.schoolplanner.logic.objects.Teacher;


public class SubjectDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private View rootView;
    private Subject showingSubject;
    private boolean addMode;
    private String subjectColor;
    private Teacher[] teachersInSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbHelper = new DatabaseHelperImpl(this);
        int subjectId = getIntent().getIntExtra("SubjectID", -1);
        if (subjectId <= 0) {
            addMode = true;
            subjectColor = Subject.DEFAULT_COLOR;
        } else {
            addMode = false;
            showingSubject = dbHelper.getSubjectAtId(subjectId);
            subjectColor = showingSubject.getColor();
        }

        rootView = findViewById(R.id.subjectDetails_main);
        initGUI();
    }


    public void onSelectColorClick(View view) {
        ColorPickerDialogBuilder
                .with(rootView.getContext())
                .setTitle(R.string.string_select_color)
                .initialColor(Color.parseColor(subjectColor))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton(R.string.string_save, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        subjectColor = "#" + Integer.toHexString(selectedColor);
                        initGUI();
                    }
                })
                .setNegativeButton(R.string.string_cancel, null)
                .build()
                .show();
    }

    public void onSaveClick(View view) {
        try {
            if (addMode) {
                dbHelper.insertIntoDB(readSubjectFromGUI());
            } else {
                dbHelper.updateSubjectAtId(readSubjectFromGUI());
            }
            finish();
        } catch (IllegalArgumentException ignored) {
        }
    }


    public void onDeleteClick(View view) {
        dbHelper.deleteSubjectAtId(showingSubject.getId());
    }

    public void onCloseClick(View view) {
        finish();
    }


    private void initGUI() {
        if (!addMode) {
            GuiHelper.setTextToTextView(rootView, R.id.subjectDetails_textName, showingSubject.getName());
            GuiHelper.setTextToTextView(rootView, R.id.subjectDetails_textRoom, showingSubject.getRoom());
            GuiHelper.setVisibility(rootView, R.id.subjectDetails_buttonDelete, View.VISIBLE);
        } else {
            GuiHelper.setVisibility(rootView, R.id.subjectDetails_buttonDelete, View.GONE);
        }
        //prevents spinner from being reset
        if (teachersInSpinner == null) {
            teachersInSpinner = fillSpinner();
        }
        //preselect spinner
        if (!addMode) {
            for (int i = 0; i < teachersInSpinner.length; i++) {
                if (teachersInSpinner[i].match(showingSubject.getTeacher())) {
                    Spinner spinner = findViewById(R.id.subjectDetails_spinnerTeacher);
                    spinner.setSelection(i);
                }
            }
        }

        findViewById(R.id.subjectDetails_buttonColor).getBackground().setColorFilter(Color.parseColor(subjectColor), PorterDuff.Mode.MULTIPLY);

    }


    private Teacher[] fillSpinner() {
        ArrayList<String> teacherStrings = new ArrayList<>();
        ArrayList<Teacher> teacherArrayList = new ArrayList<>();

        int[] teacherIndices = dbHelper.getIndices(DatabaseHelper.TABLE_TEACHER);

        for (int teacherIndex : teacherIndices) {
            Teacher teacher = dbHelper.getTeacherAtId(teacherIndex);

            teacherStrings.add(GuiHelper.extractGuiString(teacher, getBaseContext()));
            teacherArrayList.add(teacher);
        }

        if (teacherStrings.size() != 0) {
            GuiHelper.fillSpinnerFromArray(rootView, R.id.subjectDetails_spinnerTeacher, teacherStrings.toArray(new String[0]));
        } else {
            GuiHelper.setVisibility(rootView, R.id.subjectDetails_labelSpinnerError, View.VISIBLE);
            findViewById(R.id.subjectDetails_buttonSave).setEnabled(false);
        }
        return teacherArrayList.toArray(new Teacher[0]);
    }



    private Subject readSubjectFromGUI() throws IllegalArgumentException {
        Spinner spinner = findViewById(R.id.subjectDetails_spinnerTeacher);

        if (addMode) {
            return new Subject(
                    -1,
                    teachersInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.subjectDetails_textName),
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.subjectDetails_textRoom),
                    subjectColor
            );
        } else {
            return new Subject(
                    showingSubject.getId(),
                    teachersInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.subjectDetails_textName),
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.subjectDetails_textRoom),
                    subjectColor
            );
        }
    }
    
}
