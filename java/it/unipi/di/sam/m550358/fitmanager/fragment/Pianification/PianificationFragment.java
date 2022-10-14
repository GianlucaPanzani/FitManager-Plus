package it.unipi.di.sam.m550358.fitmanager.fragment.Pianification;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;


public class PianificationFragment extends Fragment implements View.OnClickListener {

    private Activity activity;

    private final String TITLE = "Pianifica i tuoi allenamenti";

    private PlanAdapter planAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DatabasePicker databasePicker;

    private ArrayList<Plan> planList;

    ProgressBar progressBar;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
        if (activity == null)
            Log.e("PianificationFragment.onCreate", "activity null");

        activity.setTitle(TITLE);

        layoutManager = new LinearLayoutManager(activity);
        databasePicker = new DatabasePicker(activity);

        // avvio del task asincrono per ricavare i dati dal database
        databasePicker.execute();
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pianification, vg, false);
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();

        progressBar = activity.findViewById(R.id.pianification_progressbar);
        ((CardView) activity.findViewById(R.id.pianification_card_view))
                .setOnClickListener(this);

        progressBar.setVisibility(ProgressBar.VISIBLE);

        // inizializzazione recycler view
        recyclerView = activity.findViewById(R.id.pianification_recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);

        // recupero dati dall'esecuzione dell'AsyncTask
        try {
            if (planList == null)
                planList = databasePicker.get(1000, TimeUnit.MILLISECONDS);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        planAdapter = PlanAdapter.getInstance(activity);
        planAdapter.setPlanList(planList);
        recyclerView.setAdapter(planAdapter);

        // setta la progress bar non visibile
        progressBar.setVisibility(ProgressBar.GONE);
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.pianification_card_view) {
            // setta la progress bar visibile in attesa dell'avvio della activity
            progressBar.setVisibility(ProgressBar.VISIBLE);

            // lancio dell'intent per l'avvio dell'activity
            Intent intent = new Intent(activity, CreatePlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return;
        }

        Log.e("PianificationFragment.onClick", "no matches");
    }





    /*********** Classe cha ha il compito di prendere i dati dal database ***********/
    private class DatabasePicker extends AsyncTask<Void,Void,ArrayList<Plan>> {
        Context context;

        /** Costruttore della classe **/
        public DatabasePicker(Context context) {
            this.context = context;
        }

        @Override
        protected ArrayList<Plan> doInBackground(Void... voids) {
            ArrayList<Plan> planList = new ArrayList<>();
            DatabaseHandler db = new DatabaseHandler(context);

            // allocazione dei dati presenti nel DB
            Cursor cursor;
            cursor = db.getAllRecordsFromPlanTable();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // creazione oggetto
                Plan plan = new Plan(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4) == 1
                );
                // aggiunta alla lista solo se non gia' presente
                if (!planListContainsObject(planList,plan))
                    planList.add(plan);
                cursor.moveToNext();
            }

            return planList;
        }

    }

    /** Restituisce true se la lista contiene l'oggetto "plan", false altrimenti **/
    private boolean planListContainsObject(ArrayList<Plan> planList, Plan plan) {
        for (Plan p : planList)
            if (p.getDate().equals(plan.getDate()) && p.getTextHours().equals(plan.getTextHours()))
                return true;
        return false;
    }



}
