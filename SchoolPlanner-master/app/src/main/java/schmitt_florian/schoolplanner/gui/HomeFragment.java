package schmitt_florian.schoolplanner.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.objects.Exam;
import schmitt_florian.schoolplanner.logic.objects.Homework;

public class HomeFragment extends Fragment {
    @SuppressWarnings({"FieldNever", "unused"})
    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initGui(view);
        initToolbarTitle();
        return view;
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


    interface OnFragmentInteractionListener {
        @SuppressWarnings({"FieldNever", "unused"})
        void onFragmentInteraction(Uri uri);
    }


    private void initGui(View view) {
        setDateToLabels(view);

        fillHomeworkListView(view);
        fillExamListView(view);
    }

 
    @SuppressLint("SwitchIntDef")
    private void setDateToLabels(View view) {
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_monday));
                break;
            case Calendar.TUESDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_tuesday));
                break;
            case Calendar.WEDNESDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_wednesday));
                break;
            case Calendar.THURSDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_thursday));
                break;
            case Calendar.FRIDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_friday));
                break;
            case Calendar.SATURDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_saturday));
                break;
            case Calendar.SUNDAY:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_day_sunday));
                break;
            default:
                GuiHelper.setTextToTextView(view, R.id.home_labelWeekday, getString(R.string.string_error));
                break;
        }
        GuiHelper.setTextToTextView(view, R.id.home_labelDate, GuiHelper.extractGuiString(calendar, false, getContext()));
    }


    private void fillHomeworkListView(View view) {
        DatabaseHelper dbHelper = new DatabaseHelperImpl(view.getContext());

        ArrayList<String> homeworkStrings = new ArrayList<>();
        int[] homeworkIndices = dbHelper.getIndices(DatabaseHelper.TABLE_HOMEWORK);

        for (int homeworkIndex : homeworkIndices) {
            Homework homework = dbHelper.getHomeworkAtId(homeworkIndex);

            if (isDateInThisWeek(homework.getDeadline()) && !homework.isDone()) {
                homeworkStrings.add(GuiHelper.extractGuiString(homework, getContext()));
            }
        }

        if (homeworkStrings.size() != 0) {
            GuiHelper.fillListViewFromArray(view, R.id.home_listHomework, homeworkStrings.toArray(new String[0]));
        }
    }

    private void fillExamListView(View view) {
        DatabaseHelper dbHelper = new DatabaseHelperImpl(view.getContext());

        ArrayList<String> examStrings = new ArrayList<>();
        int[] examIndices = dbHelper.getIndices(DatabaseHelper.TABLE_EXAM);

        for (int examIndex : examIndices) {
            Exam exam = dbHelper.getExamAtId(examIndex);

            if (isDateInThisWeek(exam.getDeadline())) {
                examStrings.add(GuiHelper.extractGuiString(exam, getContext()));
            }
        }

        if (examStrings.size() != 0) {
            GuiHelper.fillListViewFromArray(view, R.id.home_listExams, examStrings.toArray(new String[0]));
        }
    }


    private boolean isDateInThisWeek(Calendar date) {
        Calendar calendar = GregorianCalendar.getInstance();

        return date.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR) &&
                date.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    }



    private void initToolbarTitle() {
        Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
    }

    //endregion

}
