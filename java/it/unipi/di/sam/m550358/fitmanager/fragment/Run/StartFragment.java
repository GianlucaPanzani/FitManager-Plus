package it.unipi.di.sam.m550358.fitmanager.fragment.Run;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;

import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.HomeFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.Workout;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicPlayerActivity;
import it.unipi.di.sam.m550358.fitmanager.fragment.Settings.SettingsFragment;


public class StartFragment extends Fragment implements View.OnClickListener {

    private Activity activity;

    private final String TITLE = "Scegli il tipo di allenamento";
    private boolean PERMISSIONS_GRANTED = false;
    private final int REQUEST_CODE = 1234;

    public static boolean GMAP_IS_SETTED = false;

    ImageView walkIV, runIV, bikeIV, soccerIV, basketIV, volleyIV, matialartsIV, tennisIV, swimmingIV;
    Button startButton;

    private int lastSelected = 0;

    private final HomeFragment homeFragment;



    public StartFragment(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        if (activity == null)
            Log.e("StartFragment.onCreate", "activity null");
        activity.setTitle(TITLE);
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, vg, false);
    }



    @Override
    public void onStart() {
        super.onStart();

        walkIV = activity.findViewById(R.id.start_walk_imageView);
        runIV = activity.findViewById(R.id.start_run_imageView);
        bikeIV = activity.findViewById(R.id.start_bike_imageView);
        soccerIV = activity.findViewById(R.id.start_soccer_imageView);
        basketIV = activity.findViewById(R.id.start_basketball_imageView);
        volleyIV = activity.findViewById(R.id.start_volleyball_imageView);
        matialartsIV = activity.findViewById(R.id.start_martialarts_imageView);
        tennisIV = activity.findViewById(R.id.start_tennis_imageView);
        swimmingIV = activity.findViewById(R.id.start_swimming_imageView);
        startButton = activity.findViewById(R.id.start_button);

        walkIV.setOnClickListener(this);
        runIV.setOnClickListener(this);
        bikeIV.setOnClickListener(this);
        soccerIV.setOnClickListener(this);
        basketIV.setOnClickListener(this);
        volleyIV.setOnClickListener(this);
        matialartsIV.setOnClickListener(this);
        tennisIV.setOnClickListener(this);
        swimmingIV.setOnClickListener(this);
        startButton.setOnClickListener(this);

        // deseleziona l'ultima ImageView selezionata
        deSelectionImage(lastSelected);
        lastSelected = 0;

        // controllo e acquisizione dei permessi
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            if (!PERMISSIONS_GRANTED)
                Log.e("StartFragment.onMapReady", "no permissions");
        }

        // controllo della disponibilita' dei servizi Google
        if (!isGoogleServicesAvailable())
            Log.e("StartFragment.isGoogleServicesAvailable", "false");
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_walk_imageView:
                changeSelectionImage(lastSelected, 1);
                lastSelected = 1;
                break;
            case R.id.start_run_imageView:
                changeSelectionImage(lastSelected, 2);
                lastSelected = 2;
                break;
            case R.id.start_bike_imageView:
                changeSelectionImage(lastSelected, 3);
                lastSelected = 3;
                break;
            case R.id.start_soccer_imageView:
                changeSelectionImage(lastSelected, 4);
                lastSelected = 4;
                break;
            case R.id.start_basketball_imageView:
                changeSelectionImage(lastSelected, 5);
                lastSelected = 5;
                break;
            case R.id.start_volleyball_imageView:
                changeSelectionImage(lastSelected, 6);
                lastSelected = 6;
                break;
            case R.id.start_martialarts_imageView:
                changeSelectionImage(lastSelected, 7);
                lastSelected = 7;
                break;
            case R.id.start_tennis_imageView:
                changeSelectionImage(lastSelected, 8);
                lastSelected = 8;
                break;
            case R.id.start_swimming_imageView:
                changeSelectionImage(lastSelected, 9);
                lastSelected = 9;
                break;
            case R.id.start_button:
                // caso di nessun allenamento
                if (lastSelected == 0)
                    break;

                // caso di google maps gia' avviato in precedenza
                if (GMAP_IS_SETTED)
                    GoogleMapsActivity.resetMap();

                // lancio dell'intent per l'avvio dell'activity
                Intent intent = new Intent(activity, GoogleMapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("NUM_WORKOUT_TYPE", lastSelected);
                startActivityForResult(intent,REQUEST_CODE);

                GMAP_IS_SETTED = true;
                break;
            default:
                Log.e("StartFragment.onClick", "default case");
                break;
        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.e("StartFragment.onActivityResult", "bad parameter");
            return;
        }

        // riceve come risultato i dati sull'allenamento effettuato dall'utente
        Workout w = data.getParcelableExtra("WORKOUT");
        if (w == null) {
            Log.e("StartFragment.onActivityResult", "null data");
            return;
        }

        // caso di allenamento di breve durata
        if (w.getTime() < 3*60) {
            new AlertDialog.Builder(activity)
                    .setTitle("Salvataggio allenamento")
                    .setMessage("L'allenamento é stato di breve durata. Vuoi comunque salvarlo?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // invio dell'allenamento al fragment di competenza
                            homeFragment.addWorkout(activity, w);
                        }
                    }).create().show();
            return;
        }

        homeFragment.addWorkout(activity,w);
    }





    /** Controlla se i Servizi Google sono disponibili su questo dispositivo **/
    private boolean isGoogleServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(activity);

        // caso di servizi disponibili
        if (ConnectionResult.SUCCESS == result)
            return true;

        // caso di servizi non disponibili per un problema risolvibile dall'utente
        if (availability.isUserResolvableError(result)) {
            availability.getErrorDialog(activity, result, 9001).show();
            return false;
        }

        Toast.makeText(activity, "Non puoi utilizzare i servizi di Google", Toast.LENGTH_SHORT).show();
        return false;
    }




    /** Permette di ottenere i permessi di localizzazione del dispositivo **/
    private void getLocationPermission() {
        boolean fine_location_permission = false;
        boolean course_location_permission = false;

        // permessi da acquisire (se non gia' acquisiti)
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        // controlla se il primo permesso e' gia' stato acquisito
        if (ContextCompat.checkSelfPermission(activity,permissions[0]) == PackageManager.PERMISSION_GRANTED)
            fine_location_permission = true;

        // controlla se il secondo permesso e' gia' stato acquisito
        if (ContextCompat.checkSelfPermission(activity,permissions[1]) == PackageManager.PERMISSION_GRANTED)
            course_location_permission = true;

        // caso di entrambi i permessi acquisiti
        if (fine_location_permission && course_location_permission) {
            PERMISSIONS_GRANTED = true;
            return;
        }

        // richiede i permessi all'utente
        ActivityCompat.requestPermissions(activity, permissions, 1);
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




    /** Deseleziona l'immagine relativa all'indice passato come parametro **/
    private void deSelectionImage(int selected) {
        switch (selected) {
            case 1:
                walkIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 2:
                runIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 3:
                bikeIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 4:
                soccerIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 5:
                basketIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 6:
                volleyIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 7:
                matialartsIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 8:
                tennisIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 9:
                swimmingIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            default:
                Log.e("StartFragment.deSelectionImage", "default case");
                break;
        }
    }





    /** Seleziona l'immagine relativa all'indice "selected" e deseleziona l'immagine precedentemente selezionata **/
    private void changeSelectionImage(int precSelected, int selected) {
        if (precSelected == selected)
            return;

        switch (selected) {
            case 1:
                walkIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 2:
                runIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 3:
                bikeIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 4:
                soccerIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 5:
                basketIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 6:
                volleyIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 7:
                matialartsIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 8:
                tennisIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            case 9:
                swimmingIV.setBackgroundColor(getResources().getColor(R.color.accentSelected));
                break;
            default:
                Log.e("StartFragment.changeSelectionImage", "default case");
                break;
        }

        switch (precSelected) {
            case 1:
                walkIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 2:
                runIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 3:
                bikeIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 4:
                soccerIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 5:
                basketIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 6:
                volleyIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 7:
                matialartsIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 8:
                tennisIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            case 9:
                swimmingIV.setBackgroundColor(getResources().getColor(R.color.accent));
                break;
            default:
                Log.e("StartFragment.changeSelectionImage", "default case");
                break;
        }
    }


}
