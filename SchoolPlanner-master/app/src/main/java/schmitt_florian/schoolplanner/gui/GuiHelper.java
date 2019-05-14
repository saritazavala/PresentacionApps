package schmitt_florian.schoolplanner.gui;


import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import schmitt_florian.schoolplanner.R;
import schmitt_florian.schoolplanner.logic.Settings;
import schmitt_florian.schoolplanner.logic.objects.Exam;
import schmitt_florian.schoolplanner.logic.objects.Homework;
import schmitt_florian.schoolplanner.logic.objects.Subject;
import schmitt_florian.schoolplanner.logic.objects.Teacher;


class GuiHelper {


    static String getInputFromMandatoryEditText(View view, int id) throws IllegalArgumentException {
        EditText editText = view.findViewById(id);

        String input = editText.getText().toString();
        if (input.replaceAll("\\s+", "").replaceAll("\\s", "").isEmpty()) {
            handleEmptyMandatoryEditText(view, id);
            throw new IllegalArgumentException();
        } else {
            return input;
        }
    }


    static String getInputFromEditText(View view) {
        EditText editText = view.findViewById(R.id.teacherDetails_textAbbreviation);
        String input = editText.getText().toString();

        if (input.matches("")) {
            return "NULL";
        } else {
            return input;
        }
    }

    static GregorianCalendar getDateFromMandatoryButton(View view, int id) throws IllegalArgumentException {
        Button button = view.findViewById(id);

        String str = button.getText().toString();
        str = str.replaceAll(":", "-");
        str = str.replaceAll("\\.", "-");
        str = str.replaceAll(",", "-");
        str = str.replaceAll("/", "-");

        String date[];
        if (str.contains("-")) {
            date = str.split("-");

            if (date.length != 3) {
                handleEmptyMandatoryButton(view, id);
                throw new IllegalArgumentException("date must be contain three elements");
            }
        } else {
            handleEmptyMandatoryButton(view, id);
            throw new IllegalArgumentException("unknown date divider");
        }

        try {
            return (GregorianCalendar) parseCalendarFromStringArray(date, view.getContext());
        } catch (IllegalArgumentException ex) {
            handleEmptyMandatoryButton(view, id,
                    view.getContext().getResources().getString(R.string.string_date_must_comply_with) +
                            " " + Settings.getInstance(view.getContext()).getActiveDateFormat());
            throw ex;
        }

    }


    static GregorianCalendar getTimeFromMandatoryEditText(EditText editText) throws IllegalArgumentException {
        String str = editText.getText().toString();
        str = str.replaceAll(":", "-");
        str = str.replaceAll("\\.", "-");
        str = str.replaceAll(",", "-");
        str = str.replaceAll("/", "-");

        String time[];
        if (str.contains("-")) {
            time = str.split("-");

            if (time.length != 2) {
                handleEmptyMandatoryEditText(editText);
                throw new IllegalArgumentException("time must be contain two elements");
            }
        } else {
            handleEmptyMandatoryEditText(editText);
            throw new IllegalArgumentException("unknown time divider");
        }

        try {
            if ((time[0].length() <= 2 && time[1].length() <= 2) && (Integer.parseInt(time[0]) <= 24 && Integer.parseInt(time[1]) <= 60)) {
                return new GregorianCalendar(0, 0, 0, Integer.parseInt(time[0]), Integer.parseInt(time[1]));
            } else {
                throw new IllegalArgumentException((Arrays.toString(time) + " is no valid array for TIME_FORMAT_HHMM"));
            }
        } catch (IllegalArgumentException ex) {
            handleEmptyMandatoryEditText(editText,
                    editText.getContext().getResources().getString(R.string.string_time_must_comply_with) +
                            " " + Settings.TIME_FORMAT_HHMM);
            throw ex;
        }
    }


    static void setTextToTextView(View view, int id, String text) {
        TextView textView = view.findViewById(id);
        textView.setText(text);
    }


    static void setColorToButton(View view, int id, int colorId) {
        Button b = view.findViewById(id);
        b.setBackgroundResource(colorId);
    }


    static void setVisibility(View view, int id, int visibility) {
        View v = view.findViewById(id);
        v.setVisibility(visibility);
    }


    static void fillListViewFromArray(View view, int id, String[] content) {
        ListView listView = view.findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, content);

        listView.setAdapter(adapter);
    }

    static void fillGridViewFromArray(View view, String[] content) {
        GridView gridView = view.findViewById(R.id.grades_gradesTable);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, content);

        gridView.setAdapter(adapter);
    }


    static Spinner fillSpinnerFromArray(View view, int id, String[] content) {
        Spinner spinner = view.findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, content);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return spinner;
    }



    static String extractGuiString(Exam exam, Context context) {
        String dateString = extractGuiString(exam.getDeadline(), false, context);

        return exam.getSubject().getName() + " - " + dateString;
    }


    static String extractGuiString(Homework homework, Context context) {
        String dateString = extractGuiString(homework.getDeadline(), false, context);

        return homework.getSubject().getName() + " - " + dateString;
    }


    static String extractGuiString(Subject subject) {
        if (subject.getTeacher().getAbbreviation().matches("NULL")) {
            return subject.getName() + " - " + subject.getTeacher().getName();
        } else {
            return subject.getName() + " - " + subject.getTeacher().getAbbreviation();
        }
    }


    static String extractGuiString(Teacher teacher, Context context) {
        StringBuilder stringBuilder = new StringBuilder();

        if (teacher.getGender() == Teacher.MALE) {
            stringBuilder.append(context.getResources().getString(R.string.string_mr));
        } else {
            stringBuilder.append(context.getResources().getString(R.string.string_mrs));
        }

        stringBuilder.append(" ").append(teacher.getName());
        if (!teacher.getAbbreviation().matches("NULL")) {
            stringBuilder.append(" - ").append(teacher.getAbbreviation());
        }

        return stringBuilder.toString();
    }


    static String extractGuiString(Calendar calendar, boolean isTimeOnly, Context context) {
        String res = "";
        if (isTimeOnly) {
            if (calendar.get(Calendar.HOUR_OF_DAY) > 9 && calendar.get(Calendar.MINUTE) > 9) {
                res = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            } else if (calendar.get(Calendar.HOUR_OF_DAY) <= 9 && calendar.get(Calendar.MINUTE) > 9) {
                res = "0" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            } else if (calendar.get(Calendar.HOUR_OF_DAY) > 9 && calendar.get(Calendar.MINUTE) <= 9) {
                res = calendar.get(Calendar.HOUR_OF_DAY) + ":" + "0" + calendar.get(Calendar.MINUTE);
            } else {
                res = "0" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + "0" + calendar.get(Calendar.MINUTE);
            }
        } else {
            switch (Settings.getInstance(context).getActiveDateFormat()) {
                case Settings.DATE_FORMAT_DDMMYYYY:
                    res = calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." +
                            calendar.get(Calendar.YEAR);
                    break;
                case Settings.DATE_FORMAT_MMDDYYYY:
                    res = (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DAY_OF_MONTH) + "." +
                            calendar.get(Calendar.YEAR);
                    break;
                case Settings.DATE_FORMAT_YYYYMMDD:
                    res = calendar.get(Calendar.YEAR) + "." + (calendar.get(Calendar.MONTH) + 1) + "." +
                            calendar.get(Calendar.DAY_OF_MONTH);
                    break;
            }
        }
        return res;
    }
  

 
    static void defineButtonOnClickListener(View view, int id, View.OnClickListener onClickListener) {
        Button b = view.findViewById(id);
        b.setOnClickListener(onClickListener);
    }


    static void defineFloatingActionButtonOnClickListener(View view, int id, View.OnClickListener onClickListener) {
        FloatingActionButton b = view.findViewById(id);
        b.setOnClickListener(onClickListener);
    }


    static SeekBar defineSeekBarOnChangeListener(View view, SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        SeekBar seekBar = view.findViewById(R.id.settings_seekbarPeriods);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        return seekBar;
    }

    private static void handleEmptyMandatoryEditText(View view, int id) {
        handleEmptyMandatoryEditText(view, id, view.getContext().getResources().getString(R.string.string_mandatory_field));
    }


    private static void handleEmptyMandatoryEditText(View view, int id, String message) {
        EditText editText = view.findViewById(id);
        handleEmptyMandatoryEditText(editText, message);
    }


    private static void handleEmptyMandatoryEditText(EditText editText) {
        handleEmptyMandatoryEditText(editText, editText.getContext().getResources().getString(R.string.string_mandatory_field));
    }


    private static void handleEmptyMandatoryEditText(EditText editText, String message) {
        editText.setText("");
        editText.setHint(message);
        editText.setHintTextColor(Color.RED);
    }


=
    private static void handleEmptyMandatoryButton(View view, int id) {
        handleEmptyMandatoryButton(view, id, view.getContext().getResources().getString(R.string.string_mandatory_field));
    }

    private static void handleEmptyMandatoryButton(View view, int id, String message) {
        Button button = view.findViewById(id);
        handleEmptyMandatoryButton(button, message);
    }


    private static void handleEmptyMandatoryButton(Button button, String message) {
        button.setText("");
        button.setHint(message);
        button.setHintTextColor(Color.RED);
    }



    private static Calendar parseCalendarFromStringArray(String[] date, Context context) throws IllegalArgumentException {
        Calendar calendar;
        String activeDateFormat = Settings.getInstance(context).getActiveDateFormat();
        switch (activeDateFormat) {
            case Settings.DATE_FORMAT_DDMMYYYY:
                if (date[0].length() <= 2 && date[1].length() <= 2 && date[2].length() == 4) {
                    calendar = new GregorianCalendar(
                            Integer.parseInt(date[2]),
                            Integer.parseInt(date[1]) - 1,
                            Integer.parseInt(date[0])
                    );
                } else {
                    throw new IllegalArgumentException(Arrays.toString(date) + " is no valid array for DATE_FORMAT_DDMMYYYY");
                }
                break;
            case Settings.DATE_FORMAT_MMDDYYYY:
                if (date[0].length() <= 2 && date[1].length() <= 2 && date[2].length() == 4) {
                    calendar = new GregorianCalendar(
                            Integer.parseInt(date[2]),
                            Integer.parseInt(date[0]) - 1,
                            Integer.parseInt(date[1])
                    );
                } else {
                    throw new IllegalArgumentException(Arrays.toString(date) + " is no valid array for DATE_FORMAT_MMDDYYYY");
                }
                break;
            case Settings.DATE_FORMAT_YYYYMMDD:
                if (date[0].length() == 4 && date[1].length() <= 2 && date[2].length() <= 2) {
                    calendar = new GregorianCalendar(
                            Integer.parseInt(date[0]),
                            Integer.parseInt(date[1]) - 1,
                            Integer.parseInt(date[2])
                    );
                } else {
                    throw new IllegalArgumentException(Arrays.toString(date) + " is no valid array for DATE_FORMAT_YYYYMMDD");
                }
                break;
            default:
                throw new IllegalArgumentException(Settings.getInstance(context).getActiveDateFormat() + " is not supported");
        }
        return calendar;
    }



}
