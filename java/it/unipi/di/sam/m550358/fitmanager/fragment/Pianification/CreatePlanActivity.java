package it.unipi.di.sam.m550358.fitmanager.fragment.Pianification;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;

public class CreatePlanActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean PERMISSIONS_GRANTED = false;
    private final String TITLE = "Pianifica Allenamento";

    //private static int NOTIFY_ID = 0;

    DatePicker datePicker;
    TimePicker timePicker;
    EditText editText;

    PlanAdapter planAdapter;
    NotificationManager notificationManager;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        setTitle(TITLE);

        planAdapter = PlanAdapter.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        datePicker = findViewById(R.id.pianification_createPlan_datePicker);
        timePicker = findViewById(R.id.pianification_createPlan_timePicker);
        editText = findViewById(R.id.pianification_createPlan_workText);
        ((ImageButton) findViewById(R.id.pianification_createPlan_imageButton))
                .setOnClickListener(this);

        // richiesta di permessi di accesso in scrittura al calendario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
            };
            ActivityCompat.requestPermissions(this, permissions, 1);
            return;
        }

        PERMISSIONS_GRANTED = true;
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.pianification_createPlan_imageButton) {
            // controllo del testo inserito dall'utente
            if (editText.getText() == null || editText.getText().toString().equals("")) {
                Toast.makeText(this,"Devi prima inserire il testo",Toast.LENGTH_SHORT).show();
                return;
            }

            // calcolo orario
            int min   = timePicker.getMinute();
            int hour  = timePicker.getHour();
            String hours = ((hour>9)?hour:("0"+hour)) + ":" + ((min>9)?min:("0"+min));

            // calcolo data
            int day   = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year  = datePicker.getYear();
            String date = MainActivity.fromIntToDate(day,month+1,year);

            // aggiunge il piano di allenamento
            planAdapter.addItem(
                    new Plan(
                            date,
                            hours,
                            editText.getText().toString()
                    )
            );

            // controlla la presenza dei permessi ed avvia l'inserimento dell'evento nel calendario
            if (PERMISSIONS_GRANTED)
                new CalendarWriter(this).doInBackground();
            else
                Log.e("CreatePlanActivity.onClick", "no Calendar permissions");


            // notification
            /*String channelID = getResources().getString(R.string.notification_channel_id);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelID,
                        channelID,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Notification notification = new NotificationCompat.Builder(this, channelID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Hai un allenamento impostato alle ore: "+hours)
                    .setContentText(editText.getText().toString())
                    .setTimeoutAfter(System.currentTimeMillis() + fromDateAndHoursToMillis(min,hour,day,month,year))
                    .build();

            notificationManager.notify(NOTIFY_ID, notification);
            NOTIFY_ID++;*/

            finish();
            return;
        }

        Log.e("CreatePlanActivity.onClick", "no matches");
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length != 0)
            if (requestCode == 1) {
                for (int grantResult : grantResults)
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        PERMISSIONS_GRANTED = false;
                        return;
                    }
                PERMISSIONS_GRANTED = true;
            }
    }



    /*********** Classe cha ha il compito di prendere i dati dal database ***********/
    private class CalendarWriter extends AsyncTask<Void, Void, Void> {
        Context context;
        int year, month, day, hour, min;


        /** Costruttore della classe **/
        public CalendarWriter(Context context) {
            this.context = context;
            year = datePicker.getYear();
            month = datePicker.getMonth();
            day = datePicker.getDayOfMonth();
            hour = timePicker.getHour();
            min = timePicker.getMinute();
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {

            // query al content provider per ottenere le informazioni del calendario
            Cursor calCursor = getContentResolver().query(
                    CalendarContract.Calendars.CONTENT_URI,
                    new String[] {CalendarContract.Calendars._ID},
                    null,
                    null,
                    null
            );
            int calendarID = 0;
            if (calCursor != null) {
                calCursor.moveToFirst();
                calendarID = calCursor.getInt(0);
            }

            // creazione dell'evento
            long startMillis;
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(year, month, day, hour, min);
            startMillis = beginTime.getTimeInMillis();

            long endMillis;
            Calendar endTime = Calendar.getInstance();
            endTime.set(year, month, day, hour+1, min); // modifica da solo se necessario
            endMillis = endTime.getTimeInMillis();

            ContentValues cv = new ContentValues();
            cv.put(CalendarContract.Events.DTSTART, startMillis);
            cv.put(CalendarContract.Events.DTEND, endMillis);
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, "Italia");
            cv.put(CalendarContract.Events.TITLE, editText.getText().toString());
            cv.put(CalendarContract.Events.CALENDAR_ID, calendarID);

            // inserimento dell'evento nel calendario
            getContentResolver().insert(CalendarContract.Events.CONTENT_URI, cv);

            return null;
        }

    }



}