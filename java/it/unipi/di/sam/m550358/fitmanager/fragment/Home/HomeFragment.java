package it.unipi.di.sam.m550358.fitmanager.fragment.Home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.Song;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.Plan;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.PlanAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Settings.SettingsFragment;


public class HomeFragment extends Fragment {

    private Activity activity;
    private final String TITLE = "Home";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Workout> workoutList;
    private DatabasePicker databasePicker;

    private WorkoutAdapter workoutAdapter;

    public double AVG_TIMEMIN, AVG_STEPS, AVG_SPEED, TOT_KCAL;
    public int TOT_STEPS;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        if (activity == null)
            Log.e("HomeFragment.onCreate", "activity null");
        activity.setTitle(TITLE);

        layoutManager = new LinearLayoutManager(activity);
        databasePicker = new DatabasePicker(activity);

        // avvio del task asincrono per ricavare i dati dal database
        databasePicker.execute();
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, vg, false);
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();

        // inizializzazione oggetti relativi al profilo
        ((TextView) activity.findViewById(R.id.home_profile_high))
                .setText("Altezza: "+((SettingsFragment.HIGH==0)?"Non impostata":SettingsFragment.HIGH+" cm"));
        ((TextView) activity.findViewById(R.id.home_profile_kg))
                .setText("Peso: "+((SettingsFragment.KG==0)?("Non impostato"):(SettingsFragment.KG+" kg")));
        ((TextView) activity.findViewById(R.id.home_profile_bmi))
                .setText("BMI: "+getBMI(SettingsFragment.KG,SettingsFragment.HIGH));
        ((TextView) activity.findViewById(R.id.home_profile_stepsgoal))
                .setText("Obiettivo: "+SettingsFragment.STEPS_GOAL+" passi");

        // recupero dati dall'esecuzione dell'AsyncTask
        try {
            if (workoutList == null)
                workoutList = databasePicker.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        workoutAdapter = WorkoutAdapter.getInstance(activity);
        workoutAdapter.setWorkoutList(workoutList);

        // inizializzazione oggetti relativi al resoconto mensile
        DecimalFormat df = new DecimalFormat();
        ((TextView) activity.findViewById(R.id.home_month_avgSteps))
                .setText(((Double.isNaN(AVG_STEPS))?"0":(df.format(AVG_STEPS)))+" passi");
        ((TextView) activity.findViewById(R.id.home_month_avgSpeed))
                .setText(((Double.isNaN(AVG_SPEED))?"0":(df.format(AVG_SPEED)))+" m/s");
        ((TextView) activity.findViewById(R.id.home_month_avgTime))
                .setText(((Double.isNaN(AVG_TIMEMIN))?"0":(df.format(AVG_TIMEMIN)))+" min");
        ((TextView) activity.findViewById(R.id.home_month_totSteps))
                .setText(TOT_STEPS+" passi");
        ((TextView) activity.findViewById(R.id.home_month_totKcal))
                .setText(df.format(TOT_KCAL)+" Kcal");

        // inizializzazione recycler view
        recyclerView = activity.findViewById(R.id.home_run_recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(workoutAdapter);

    }




    /*********** Classe cha ha il compito di prendere i dati dal database ***********/
    private class DatabasePicker extends AsyncTask<Void,Void,ArrayList<Workout>> {
        Context context;

        /** Costruttore della classe **/
        public DatabasePicker(Context context) {
            this.context = context;
        }

        @Override
        protected ArrayList<Workout> doInBackground(Void... voids) {
            ArrayList<Workout> workoutList = new ArrayList<>();
            DatabaseHandler db = new DatabaseHandler(context);

            double AVG_STEPS = 0, AVG_TIMEMIN = 0, TOT_KCAL = 0, AVG_SPEED = 0;
            int TOT_STEPS = 0;

            // mappa che serve al metodo di controllo delle date (dateIsBeforeOneMonthAgo)
            Map<String,Integer> monthsMap = new HashMap<>();
            monthsMap.put("Gen", 1);
            monthsMap.put("Feb", 2);
            monthsMap.put("Mar", 3);
            monthsMap.put("Apr", 4);
            monthsMap.put("Mag", 5);
            monthsMap.put("Giu", 6);
            monthsMap.put("Lug", 7);
            monthsMap.put("Ago", 8);
            monthsMap.put("Set", 9);
            monthsMap.put("Ott", 10);
            monthsMap.put("Nov", 11);
            monthsMap.put("Dic", 12);

            // allocazione dei dati della pianificatione presenti nel DB
            Cursor cursor = db.getAllRecordsFromWorkoutTable();
            if (cursor == null) {
                Log.e("HomeFragment.run", "cursor null");
                return null;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // creazione oggetto allenamento
                Workout workout = new Workout(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getDouble(7),
                        cursor.getInt(8)
                );
                Log.d("HOMEFRAGMENT",workout.date+" "+workout.hours);

                // caso di rimozione dell'allenamento (se non e' stato fatto nell'ultimo mese)
                if (dateIsBeforeOneMonthAgo(monthsMap,workout.getDate())) {
                    Log.d("DATE IS BEFORE ONE MONTH AGO", workout.getDate());
                    if (!db.deleteWorkoutRecord(workout.getDate(), workout.getHours()))
                        Log.e("HomeFragment.run", "workout not removed from database");

                // aggiunge l'allenamento se non gia' presente
                } else if (!workoutListContainsObject(workoutList,workout)) {
                    // inizializzazione dei minuti di allenamento
                    int minutes = workout.getTime()/60;

                    // inizializzazione di velocita' e kcal
                    double speed = workout.getSpeed();
                    double kcal = WorkoutAdapter.getMETs(workout.getImageId())*0.0175*SettingsFragment.KG*minutes;

                    // aggiornamento dei valori
                    AVG_TIMEMIN += minutes;
                    AVG_STEPS += workout.getSteps();
                    TOT_STEPS += workout.getSteps();
                    if (!Double.isNaN(speed))
                        AVG_SPEED += speed;
                    if (!Double.isNaN(kcal))
                        TOT_KCAL += kcal;

                    // aggiunta dell'allenamento alla lista
                    workoutList.add(workout);
                }
                cursor.moveToNext();
            }
            cursor.close();

            // calcolo valori medi
            AVG_STEPS = AVG_STEPS / workoutList.size();
            AVG_TIMEMIN  = AVG_TIMEMIN / workoutList.size();
            AVG_SPEED = AVG_SPEED / workoutList.size();

            setData(workoutList,AVG_STEPS,AVG_TIMEMIN,AVG_SPEED,TOT_STEPS,TOT_KCAL);

            return workoutList;
        }

    }



    /** Restituisce true se la lista contiene l'oggetto "workout", false altrimenti **/
    private boolean workoutListContainsObject(ArrayList<Workout> workoutList, Workout workout) {
        if (workoutList == null)
            return false;
        for (Workout w : workoutList)
            if ((workout.getHours()+workout.getTime()).equals(w.getHours()+w.getTime()))
                return true;
        return false;
    }



    /** Setta i dati locali **/
    public void setData(ArrayList<Workout> workoutList, double AVG_STEPS, double AVG_TIMEMIN,
                        double AVG_SPEED, int TOT_STEPS, double TOT_KCAL) {
        this.workoutList = workoutList;
        this.AVG_TIMEMIN = AVG_TIMEMIN;
        this.AVG_SPEED = AVG_SPEED;
        this.AVG_STEPS = AVG_STEPS;
        this.TOT_STEPS = TOT_STEPS;
        this.TOT_KCAL = TOT_KCAL;
    }



    /** Restituisce true se la data "data1" risale a piu' di un mese fa, false altrimenti **/
    private boolean dateIsBeforeOneMonthAgo(Map<String,Integer> monthsMap, String date1) {
        Calendar calendar = Calendar.getInstance();
        String[] dateArray1 = date1.split(" ");

        // data passata come parametro
        int day1 = Integer.parseInt(dateArray1[0]);
        int month1 = monthsMap.get(dateArray1[1]); // range: 1 - 12
        int year1 = Integer.parseInt(dateArray1[2]);

        // data attuale
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);
        int month2 = calendar.get(Calendar.MONTH); // range: 0 - 11
        int year2 = calendar.get(Calendar.YEAR);

        Log.d("HOMEFRAGMENT.dateIsBefore...",
                day1+" "+
                    month1+" "+
                    year1);
        Log.d("HOMEFRAGMENT.dateIsBefore...",
                day2+" "+
                    month2+" "+
                    year2);

        if (year1 < year2)
            return true;
        else if (year1 == year2 && month1 < month2)
            return true;
        else if (year1 == year2 && month1 == month2 && day1 < day2)
            return true;
        return false;
    }



    /** Dati i kg e l'altezza (in cm) dell'utente, restituisce il valore del BMI (come stringa) **/
    private String getBMI(int kg, int highCm) {
        if (highCm == 0 || kg == 0)
            return "Sconosciuto";

        double highM = (double) highCm/100;
        return new DecimalFormat("0.00").format(kg / (highM*highM));
    }



    /** Aggiunge l'oggetto "workout" alla lista di questa classe e agli item dell'adapter **/
    public void addWorkout(Context context, Workout workout) {
        // aggiunta del workout al database e alla lista dell'adapter (con addItem)
        workoutAdapter = WorkoutAdapter.getInstance(context);
        workoutAdapter.addItem(workout);
    }



}
