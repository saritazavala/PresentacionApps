package schmitt_florian.schoolplanner.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Objects;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.DatabaseHelper;
import schmitt_florian.schoolplanner.logic.DatabaseHelperImpl;
import schmitt_florian.schoolplanner.logic.Settings;
import schmitt_florian.schoolplanner.logic.objects.Lesson;
import schmitt_florian.schoolplanner.logic.objects.Period;
import schmitt_florian.schoolplanner.logic.objects.Schedule;
import schmitt_florian.schoolplanner.logic.objects.Subject;
import schmitt_florian.schoolplanner.logic.objects.Weekday;


public class ScheduleFragment extends Fragment {
    @SuppressWarnings({"FieldNever", "unused"})
    private OnFragmentInteractionListener mListener;
    private View rootView;

    private TableLayout table;
    private Schedule schedule;
    private TableRow[] rows;
    private Button[][] buttons;

    private DatabaseHelperImpl databaseHelper;
    private boolean editMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        initGui();
        return rootView;

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

  
    public interface OnFragmentInteractionListener {
        @SuppressWarnings({"FieldNever", "unused"})
        void onFragmentInteraction(Uri uri);
    }

 
    private void initGui() {
        updateValues();

        rows = getScheduleRowsInArray();
        initVisibilityForSchedule();

        buttons = getButtonsAsArray();
        initButtons();

        initAppbarEditSwitch();
        initToolbarTitle();
    }

    private void updateValues() {
        databaseHelper = new DatabaseHelperImpl(getContext());
        table = rootView.findViewById(R.id.schedule_table);
        schedule = databaseHelper.getScheduleAtId(1);
    }

    private TableRow[] getScheduleRowsInArray() {
        ArrayList<TableRow> rowArrayList = new ArrayList<>();

        for (int i = 0; i < table.getChildCount(); i++) {
            rowArrayList.add((TableRow) table.getChildAt(i));
        }

        return rowArrayList.toArray(new TableRow[0]);
    }

 
    private Button[][] getButtonsAsArray() {
        Button[][] buttons = new Button[((LinearLayout) rows[0].getChildAt(0)).getChildCount()][table.getChildCount()];

        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < ((LinearLayout) rows[i].getChildAt(0)).getChildCount(); j++) {
                buttons[j][i] = (Button) ((LinearLayout) rows[i].getChildAt(0)).getChildAt(j);
            }
        }
        return buttons;
    }

    private void initVisibilityForSchedule() {
        int visibleRowCount = Settings.getInstance(Objects.requireNonNull(getContext())).getPeriodsAtDay() + 1;
        for (int i = 0; i < visibleRowCount; i++) {
            rows[i].setVisibility(View.VISIBLE);
        }
        for (int i = rows.length; i > visibleRowCount; i--) {
            rows[i - 1].setVisibility(View.GONE);
        }
    }



    private void initToolbarTitle() {
        Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.string_schedule);
    }

    private void initAppbarEditSwitch() {
        SwitchCompat editSwitch = Objects.requireNonNull(getActivity()).findViewById(R.id.appbar_switch);

        editSwitch.setVisibility(View.VISIBLE);
        editSwitch.setChecked(editMode);
        editMode = editSwitch.isChecked();

        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editMode = !editMode;
                initGui();
            }
        });
    }

 
    private void initButtons() {
        for (int x = 1; x < buttons.length; x++) {
            for (int y = 1; y < buttons[x].length; y++) {
                buttons[x][y].setOnClickListener(new OnScheduleButtonClickListener(x, y));
                buttons[x][y].setClickable(editMode);

                if (x >= 2) {
                    buttons[x][y].getBackground().setColorFilter(Color.parseColor(Subject.DEFAULT_COLOR), PorterDuff.Mode.MULTIPLY);
                }

                if (editMode) {
                    buttons[x][y].setText("+");
                } else {
                    buttons[x][y].setText("");
                }
            }
        }
        loadSubjectButtons();
        initPeriodButtons();
    }

    private void loadSubjectButtons() {
        for (int i = 0; i < schedule.getDays().length; i++) {
            for (Lesson lesson : schedule.getDays()[i].getLessons()) {
                int columnIdx = 2;

                switch (schedule.getDays()[i].getName()) {
                    case Weekday.MONDAY:
                        columnIdx = 2;
                        break;
                    case Weekday.TUESDAY:
                        columnIdx = 3;
                        break;
                    case Weekday.WEDNESDAY:
                        columnIdx = 4;
                        break;
                    case Weekday.THURSDAY:
                        columnIdx = 5;
                        break;
                    case Weekday.FRIDAY:
                        columnIdx = 6;
                        break;
                    case Weekday.SATURDAY:
                        columnIdx = 7;
                        break;
                }

                Button currButton = buttons[columnIdx][lesson.getPeriod().getSchoolHourNo()];

                currButton.setText(lesson.getSubject().getName());
                currButton.getBackground().setColorFilter(Color.parseColor(lesson.getSubject().getColor()), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

 
    private void initPeriodButtons() {
        Period[] periods = getAllPeriodsInDb();
        for (Period p : periods) {
            buttons[1][p.getSchoolHourNo()].setText(GuiHelper.extractGuiString(p.getStartTime(), true, getContext()) + " - " +
                    GuiHelper.extractGuiString(p.getEndTime(), true, getContext()));
        }
    }

    private Period[] getAllPeriodsInDb() {
        ArrayList<Period> periodArrayList = new ArrayList<>();

        int[] periodIndices = databaseHelper.getIndices(DatabaseHelper.TABLE_PERIOD);

        for (int periodIndex : periodIndices) {
            Period period = databaseHelper.getPeriodAtId(periodIndex);

            periodArrayList.add(period);
        }

        return periodArrayList.toArray(new Period[0]);
    }
    //endregion


 
    private class OnScheduleButtonClickListener implements View.OnClickListener {
        private boolean timeHasChanged;
        private final boolean isTimeButton;
        private final int x;
        private final int y;



        private OnScheduleButtonClickListener(int xPos, int yPos) {
            this.isTimeButton = xPos <= 1;
            this.x = xPos;
            this.y = yPos;
        }


        @Override
        public void onClick(View v) {
            if (isTimeButton) {
                showTimeAlertDialog();
            } else {
                showSubjectAlertDialog();
            }

        }


        private void showTimeAlertDialog() {
            final InsertPeriodTimesDialog timeDialog = new InsertPeriodTimesDialog(Objects.requireNonNull(getContext()));
            timeDialog.positiveButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnPositiveButtonInTimeDialogClick(timeDialog);
                }
            });

            timeDialog.show();
        }

        private void handleOnPositiveButtonInTimeDialogClick(InsertPeriodTimesDialog timesDialog) {
            try {
                try {
                    Period period = databaseHelper.getPeriodAtIdOrThrow(y);

                    databaseHelper.updatePeriodAtId(
                            new Period(period.getId(), period.getSchoolHourNo(), timesDialog.getStartTime(), timesDialog.getEndTime()));

                    timeHasChanged = true;
                } catch (NoSuchFieldException e) {
                    databaseHelper.insertIntoDB(
                            new Period(y, y, timesDialog.getStartTime(), timesDialog.getEndTime()));

                    timeHasChanged = true;
                }
            } catch (IllegalArgumentException ex) {
                timeHasChanged = false;
            }
            initGui();
        }


        private void showSubjectAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle(R.string.string_select_subject);

            builder.setItems(getSubjectSelectorContent(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handleOnSubjectInSubjectDialogClick(dialog, which);
                }
            });

            builder.setNegativeButton(R.string.string_cancel, null);

            builder.show();
        }

 
        private void handleOnSubjectInSubjectDialogClick(DialogInterface dialog, int which) {
            Weekday weekday = schedule.getDay(getClickedWeekdayName());

            if (which == 0) {
                try {
                    Lesson lesson = databaseHelper.getLessonOrThrowAtDate(weekday, getAllPeriodsInDb()[y - 1]);
                    databaseHelper.deleteLessonAtId(lesson.getId());
                } catch (NoSuchFieldException | ArrayIndexOutOfBoundsException ignore) {
                    dialog.dismiss();
                }
            } else {
                try {
                    insertOrUpdateLesson(getAllSubjectsInDb()[which - 1]);
                } catch (NullPointerException ex) {
                    insertNewWeekdayInDb();
                    insertOrUpdateLesson(getAllSubjectsInDb()[which - 1]);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    handleMissingPeriod();
                }
            }
            initGui();
        }


        private void insertOrUpdateLesson(Subject subject) throws ArrayIndexOutOfBoundsException {
            Weekday weekday = schedule.getDay(getClickedWeekdayName());

            try {
                Lesson lesson = databaseHelper.getLessonOrThrowAtDate(weekday, getAllPeriodsInDb()[y - 1]);
                Lesson newLesson = new Lesson(lesson.getId(), subject, lesson.getPeriod());

                databaseHelper.updateLessonAtId(newLesson);

            } catch (NoSuchFieldException e) {
                Lesson newLesson = new Lesson(-1, subject, getAllPeriodsInDb()[y - 1]);

                int newLessonId = databaseHelper.insertIntoDB(newLesson);
                newLesson = new Lesson(newLessonId, newLesson.getSubject(), newLesson.getPeriod());

                ArrayList<Lesson> lessons = new ArrayList<>(Arrays.asList(weekday.getLessons()));
                lessons.add(newLesson);
                databaseHelper.updateWeekdayAtId(new Weekday(
                        weekday.getId(),
                        weekday.getName(),
                        lessons.toArray(new Lesson[0])
                ));
            }
        }

      
        private void insertNewWeekdayInDb() {
            Weekday newWeekday = new Weekday(-1, getClickedWeekdayName(), new Lesson[0]);

            int newWeekdayID = databaseHelper.insertIntoDB(newWeekday);
            newWeekday = new Weekday(newWeekdayID, newWeekday.getName(), newWeekday.getLessons());

            ArrayList<Weekday> weekdays = new ArrayList<>(Arrays.asList(schedule.getDays()));
            weekdays.add(newWeekday);

            databaseHelper.updateScheduleAtId(new Schedule(
                    schedule.getId(),
                    schedule.getName(),
                    weekdays.toArray(new Weekday[0])
            ));

            updateValues();
        }


        private String getClickedWeekdayName() {
            switch (x - 1) {
                case 1:
                    return Weekday.MONDAY;
                case 2:
                    return Weekday.TUESDAY;
                case 3:
                    return Weekday.WEDNESDAY;
                case 4:
                    return Weekday.THURSDAY;
                case 5:
                    return Weekday.FRIDAY;
                case 6:
                    return Weekday.SATURDAY;
                default:
                    return null;
            }
        }


        private String[] getSubjectSelectorContent() {
            ArrayList<String> guiStrings = new ArrayList<>();

            guiStrings.add(getResources().getString(R.string.string_none));

            for (Subject subject : getAllSubjectsInDb()) {
                guiStrings.add(GuiHelper.extractGuiString(subject));
            }

            return guiStrings.toArray(new String[0]);
        }

 
        private Subject[] getAllSubjectsInDb() {
            ArrayList<Subject> subjectArrayList = new ArrayList<>();

            int[] subjectIndices = databaseHelper.getIndices(DatabaseHelper.TABLE_SUBJECT);

            for (int subjectIndex : subjectIndices) {
                Subject subject = databaseHelper.getSubjectAtId(subjectIndex);

                subjectArrayList.add(subject);
            }

            return subjectArrayList.toArray(new Subject[0]);
        }

 
        private void handleMissingPeriod() {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

            builder.setIconAttribute(android.R.attr.alertDialogIcon);
            builder.setTitle(R.string.string_missing_period);

            builder.setMessage(R.string.text_dialog_schedule_missing_period);
            builder.setNegativeButton(R.string.string_cancel, null);

            builder.show();
        }

        private class InsertPeriodTimesDialog extends AlertDialog {
            private EditText startTime;
            private EditText endTime;


            InsertPeriodTimesDialog(@NonNull Context context) {
                super(context);
                setTitle(R.string.string_select_time);
                setView(getLayoutForTimeDialog());
                setCancelable(false);

                setButton(BUTTON_NEGATIVE, context.getResources().getString(R.string.string_cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
            }


            void positiveButton(final View.OnClickListener onClickListener) {
                setButton(BUTTON_POSITIVE, getContext().getResources().getString(R.string.string_save), (OnClickListener) null);

                setOnShowListener(new OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onClickListener.onClick(v);

                                if (timeHasChanged) {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
            }


            GregorianCalendar getStartTime() {
                return GuiHelper.getTimeFromMandatoryEditText(startTime);
            }

            GregorianCalendar getEndTime() {
                return GuiHelper.getTimeFromMandatoryEditText(endTime);
            }

            @NonNull
            private LinearLayout getLayoutForTimeDialog() {
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                startTime = new EditText(getContext());
                startTime.setHint(R.string.string_start_time);
                startTime.setInputType(InputType.TYPE_CLASS_DATETIME);
                layout.addView(startTime);

                endTime = new EditText(getContext());
                endTime.setHint(R.string.string_end_time);
                endTime.setInputType(InputType.TYPE_CLASS_DATETIME);
                layout.addView(endTime);

                return layout;
            }
            

        }
    }

}
