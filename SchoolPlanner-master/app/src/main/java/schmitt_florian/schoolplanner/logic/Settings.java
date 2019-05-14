package schmitt_florian.schoolplanner.logic;


import android.content.Context;
import android.content.SharedPreferences;


public class Settings {

    public static final String DATE_FORMAT_DDMMYYYY = "DD.MM.YYYY";

    public static final String DATE_FORMAT_MMDDYYYY = "MM.DD.YYYY";

    public static final String DATE_FORMAT_YYYYMMDD = "YYYY.MM.DD";


    public static final String TIME_FORMAT_HHMM = "HH:MM";



    private static final String DATE_FORMAT = "dateFormat";
 
    private static final String PERIODS_AT_DAY = "periodsAtDay";



    private final Context context;

    private String activeDateFormat;


    private int periodsAtDay;



    public static Settings getInstance(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getApplicationContext().toString(), Context.MODE_PRIVATE);

        return new Settings(context,
                preferences.getString(DATE_FORMAT, DATE_FORMAT_DDMMYYYY),
                preferences.getInt(PERIODS_AT_DAY, 6)
        );
    }

    private Settings(Context context, String activeDateFormat, int periodsAtDay) {
        this.context = context;
        this.activeDateFormat = activeDateFormat;
        this.periodsAtDay = periodsAtDay;
    }

    public void saveSettings() {
        SharedPreferences.Editor preferences = context.getSharedPreferences(context.getApplicationContext().toString(), Context.MODE_PRIVATE).edit();

        preferences.putString(DATE_FORMAT, activeDateFormat);
        preferences.putInt(PERIODS_AT_DAY, periodsAtDay);

        preferences.apply();
    }


    public String getActiveDateFormat() {
        return activeDateFormat;
    }


    public void setActiveDateFormat(String activeDateFormat) {
        this.activeDateFormat = activeDateFormat;
    }


    public int getPeriodsAtDay() {
        return periodsAtDay;
    }

    public void setPeriodsAtDay(int periodsAtDay) {
        this.periodsAtDay = periodsAtDay;
    }



}
