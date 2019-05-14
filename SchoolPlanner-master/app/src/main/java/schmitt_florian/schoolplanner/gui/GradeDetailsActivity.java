package schmitt_florian.schoolplanner.gui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.objects.Grade;
import schmitt_florian.schoolplanner.logic.objects.Subject;


public class GradeDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private View rootView;
    private Grade showingGrade;
    private Subject[] subjectsInSpinner;
    private boolean addMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbHelper = new DatabaseHelperImpl(this);
        int gradeId = getIntent().getIntExtra("GradeID", -1);
        if (gradeId <= 0) {
            addMode = true;
        } else {
            addMode = false;
            showingGrade = dbHelper.getGradeAtId(gradeId);
        }

        rootView = findViewById(R.id.gradeDetails_main);
        initGUI();
    }

   
    public void onSaveClick(View view) {
        try {
            if (addMode) {
                dbHelper.insertIntoDB(readGradeFromGUI());
            } else {
                dbHelper.updateGradeAtId(readGradeFromGUI());
            }
            finish();
        } catch (IllegalArgumentException ignored) {
        }
    }

  
    public void onDeleteClick(View view) {
        dbHelper.deleteGradeAtId(showingGrade.getId());
    }

    
    public void onCloseClick(View view) {
        finish();
    }

    
    private void initGUI() {
        if (!addMode) {
            GuiHelper.setTextToTextView(rootView, R.id.gradeDetails_textName, showingGrade.getName());
            GuiHelper.setTextToTextView(rootView, R.id.gradeDetails_textGrade, showingGrade.getGrade());

            GuiHelper.setVisibility(rootView, R.id.gradeDetails_buttonDelete, View.VISIBLE);
        } else {
            GuiHelper.setVisibility(rootView, R.id.gradeDetails_buttonDelete, View.GONE);
        }

        subjectsInSpinner = fillSpinner();

        //preselect spinner
        if (!addMode) {
            for (int i = 0; i < subjectsInSpinner.length; i++) {
                if (subjectsInSpinner[i].match(showingGrade.getSubject())) {
                    Spinner spinner = findViewById(R.id.gradeDetails_spinnerSubject);
                    spinner.setSelection(i);
                }
            }
        }
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
            GuiHelper.fillSpinnerFromArray(rootView, R.id.gradeDetails_spinnerSubject, subjectStrings.toArray(new String[0]));
        } else {
            GuiHelper.setVisibility(rootView, R.id.gradeDetails_labelSpinnerError, View.VISIBLE);
            findViewById(R.id.gradeDetails_buttonSave).setEnabled(false);
        }
        return subjectArrayList.toArray(new Subject[0]);
    }

   
    private Grade readGradeFromGUI() throws IllegalArgumentException {
        Spinner spinner = findViewById(R.id.gradeDetails_spinnerSubject);

        if (addMode) {
            return new Grade(
                    -1,
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.gradeDetails_textName),
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.gradeDetails_textGrade)
            );
        } else {
            return new Grade(
                    showingGrade.getId(),
                    subjectsInSpinner[spinner.getSelectedItemPosition()],
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.gradeDetails_textName),
                    GuiHelper.getInputFromMandatoryEditText(rootView, R.id.gradeDetails_textGrade)
            );
        }

    }

}
