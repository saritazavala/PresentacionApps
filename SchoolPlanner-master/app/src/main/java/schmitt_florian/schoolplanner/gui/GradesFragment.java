package schmitt_florian.schoolplanner.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.objects.Grade;
import schmitt_florian.schoolplanner.logic.objects.Subject;

public class GradesFragment extends Fragment implements View.OnClickListener {
    @SuppressWarnings({"FieldNever", "unused"})
    private OnFragmentInteractionListener mListener;
    private Grade[] gradesCurrentlyShowing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grades, container, false);
//        ToggleButton editButton = (ToggleButton) view.findViewById(R.id.toggleEditSchedule);
//        editButton.setVisibility(View.INVISIBLE);

        initGui(view);
        initToolbarTitle();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        initGui(getView());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grades_floatingActionButton_add:
                startActivity(new Intent(getContext(), GradeDetailsActivity.class));
                break;
        }
    }


    interface OnFragmentInteractionListener {
        @SuppressWarnings({"FieldNever", "unused"})
        void onFragmentInteraction(Uri uri);
    }

    private void initGui(final View view) {
        defineSubjectListOnClick(view, fillSubjectListView(view));
        defineGridViewOnClick(view);

        GuiHelper.defineFloatingActionButtonOnClickListener(view, R.id.grades_floatingActionButton_add, this);
    }


    private void defineSubjectListOnClick(final View view, final Subject[] allSubjectsInList) {
        ListView subjectList = view.findViewById(R.id.grades_listSubjects);

        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                gradesCurrentlyShowing = fillGridView(view, allSubjectsInList[position]);
            }
        });
    }

 
    private void defineGridViewOnClick(View view) {
        final GridView gridView = view.findViewById(R.id.grades_gradesTable);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Intent intent = new Intent(getContext(), GradeDetailsActivity.class);
                if (position % 2 == 0) {
                    intent.putExtra("GradeID", gradesCurrentlyShowing[position / 2].getId());

                } else {
                    intent.putExtra("GradeID", gradesCurrentlyShowing[(position - 1) / 2].getId());
                }
                startActivity(intent);
            }
        });
    }


    private Subject[] fillSubjectListView(View view) {
        DatabaseHelper dbHelper = new DatabaseHelperImpl(view.getContext());

        ArrayList<String> subjectStrings = new ArrayList<>();
        ArrayList<Subject> subjectArrayList = new ArrayList<>();
        int[] subjectIndices = dbHelper.getIndices(DatabaseHelper.TABLE_SUBJECT);

        for (int subjectIndex : subjectIndices) {
            Subject subject = dbHelper.getSubjectAtId(subjectIndex);

            subjectStrings.add(GuiHelper.extractGuiString(subject));
            subjectArrayList.add(subject);
        }

        if (subjectStrings.size() != 0) {
            GuiHelper.fillListViewFromArray(view, R.id.grades_listSubjects, subjectStrings.toArray(new String[0]));
        }
        return subjectArrayList.toArray(new Subject[0]);
    }


    private Grade[] fillGridView(View view, Subject subject) {
        DatabaseHelper dbHelper = new DatabaseHelperImpl(view.getContext());

        ArrayList<String> gridStrings = new ArrayList<>();
        ArrayList<Grade> gradeArrayList = new ArrayList<>();
        int[] gradeIndices = dbHelper.getIndices(DatabaseHelper.TABLE_GRADE);

        for (int gradeIndex : gradeIndices) {
            Grade grade = dbHelper.getGradeAtId(gradeIndex);

            if (grade.getSubject().match(subject)) {
                gridStrings.add(grade.getName());
                gridStrings.add("\t" + "\t" + "\t" + "\t" + grade.getGrade());

                gradeArrayList.add(grade);
            }
        }

        //if (gridStrings.size() != 0) {
        GuiHelper.fillGridViewFromArray(view, gridStrings.toArray(new String[0]));
//        }
        return gradeArrayList.toArray(new Grade[0]);


    private void initToolbarTitle() {
        Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.string_grades);
    }
    
}
