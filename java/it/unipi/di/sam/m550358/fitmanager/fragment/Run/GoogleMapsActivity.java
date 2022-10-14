package it.unipi.di.sam.m550358.fitmanager.fragment.Run;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.HomeFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.Workout;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.WorkoutAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicPlayerActivity;
import it.unipi.di.sam.m550358.fitmanager.fragment.Settings.SettingsFragment;

public class GoogleMapsActivity extends AppCompatActivity implements
        LocationSource.OnLocationChangedListener, OnMapReadyCallback, SensorEventListener {

    private final String TITLE = "Go!";

    private static GoogleMap map;
    private static SupportMapFragment mapFragment;
    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView timeTextView;
    private TextView stepsTextView;
    private ImageView stepsImageView;
    private Button stopButton;

    private int counter;

    private final float DEFAULT_ZOOM = 15f;
    private boolean STOP = false;
    private long START;

    private String WORKOUT_DESCRIPTION;
    private int IMAGE_ID;
    private int UPDATE_TIME;
    private double MIN_DISTANCE;

    private long TIME;
    private int STEPS;
    private int START_STEPS;
    private boolean IS_RUNNING;
    private double AVG_SPEED;

    private Location precLocation;
    private LocationManager locationManager;
    private String provider;





    /** Thread di terminazione che invia i dati dell'allenamento alla fine di questo **/
    Thread stopThread = new Thread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            // inizializzazioni
            SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
            Date date = new Date();
            int min  = date.getMinutes();
            int hour = date.getHours();

            // creazione della data attuale
            String[] dateArray = formatter.format(date).split(" ");
            String completeDate = MainActivity.fromIntToDate(
                    Integer.parseInt(dateArray[0]),
                    Integer.parseInt(dateArray[1]),
                    Integer.parseInt(dateArray[2])
            );

            // ciclo di attesa della terminazione dell'allenamento
            while (!STOP) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // setta la velocita' media
            AVG_SPEED = AVG_SPEED/counter;

            // creazione oggetto da restituire coi dati dell'allenamento
            Workout workout = new Workout(
                    completeDate,
                    hour + ":" + min,
                    WORKOUT_DESCRIPTION,
                    (int) TIME/1000,
                    STEPS,
                    SettingsFragment.STEPS_GOAL,
                    AVG_SPEED,
                    IMAGE_ID
            );

            // invio del risultato tramite intent
            Intent intent = new Intent(getApplicationContext(), StartFragment.class);
            intent.putExtra("WORKOUT", workout);
            setResult(Activity.RESULT_OK, intent);

            finish();
        }
    });






    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmaps);

        setTitle(TITLE);

        // inizializzazione valori
        counter = 0;
        TIME = 0;
        STEPS = 0;
        AVG_SPEED = 0;

        // inizializzazione campi del layout
        timeTextView = findViewById(R.id.gmap_time_textView);
        stepsTextView = findViewById(R.id.gmap_steps_textView);
        stepsImageView = findViewById(R.id.gmap_steps_imageView);
        stopButton = findViewById(R.id.gmap_stop_button);

        // valori passati tramite intent
        setWorkoutValues(getIntent().getIntExtra("NUM_WORKOUT_TYPE", 2));

        // inizializzazione del SupportMapFragment
        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.gmap_fragment_map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
        }

        // inizializzazione degli oggetti relativi alla posizione corrente
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setCostAllowed(false);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);

        // controllo di presenza del sensore sul dispositivo
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            // inizializzazione sensore
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            // acquisizione dei permessi per l'uso del sensore contapassi
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] {Manifest.permission.ACTIVITY_RECOGNITION},
                        Sensor.TYPE_STEP_COUNTER
                );
            }
        }

        // listener per lo stop button
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == stopButton.getId())
                    STOP = true;
                try {
                    stopThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // avvio del thread di chiusura
        stopThread.start();

        // avvio del cronometro
        startChronometer();
    }





    /** Stampa a video il tempo dall'inizio dell'allenamento col formato "h:m:s" **/
    private void startChronometer() {
        START = System.currentTimeMillis();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!STOP) {
                    TIME = System.currentTimeMillis()-START;
                    timeTextView.setText(fromMillisToString(TIME));
                    new Handler().postDelayed(this, 100);
                }
            }
        });
    }





    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        if (SettingsFragment.MAPS_ACTIVE)
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, (float) MIN_DISTANCE, this::onLocationChanged);

        IS_RUNNING = true;
        if (sensor == null) {
            Log.e("GoogleMapsActivity.onResume", "no sensor on device");
            Toast.makeText(this,"Nessun sensore di passi rilevato sul dispositivo", Toast.LENGTH_SHORT).show();
            return;
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }




    @Override
    protected void onPause() {
        super.onPause();

        if (SettingsFragment.MAPS_ACTIVE)
            locationManager.removeUpdates(this::onLocationChanged);

        IS_RUNNING = false;
        if (STOP && sensor != null)
            sensorManager.unregisterListener(this);

    }




    @Override
    public void onLocationChanged(@NonNull Location currentLocation) {
        double lastLat = currentLocation.getLatitude();
        double lastLng = currentLocation.getLongitude();

        // caso di avvio della mappa
        if (precLocation == null) {
            animateCamera(lastLat, lastLng,DEFAULT_ZOOM);

        // caso di calcolo della distanza tra la posizione precedente e quella attuale
        } else {
            AVG_SPEED += currentLocation.getSpeed();
            counter++;
            moveCamera(lastLat,lastLng);
        }

        precLocation = currentLocation;
    }




    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (IS_RUNNING && sensorEvent.sensor == sensor) {
            if (START_STEPS == 0)
                START_STEPS = (int) sensorEvent.values[0];
            else
                STEPS = ((int) sensorEvent.values[0]) - START_STEPS;
            stepsTextView.setText(String.valueOf(STEPS));
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }





    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (!SettingsFragment.MAPS_ACTIVE)
            return;

        map = googleMap;
        map.setMyLocationEnabled(true);
    }






    /** Resetta alcuni campi statici della classe preparando la activity ad una nuova esecuzione **/
    public static void resetMap() {
        if (map != null)
            map.clear();
        mapFragment = null;
    }





    /** Converte i millisecondi passati come parametro in una stringa dal formato "h:m:s" **/
    private String fromMillisToString(long t) {
        t = t/10;
        int sec   = (int) (t/100)%60;
        int min   = (int) ((t/100)/60)%60;
        int hours = (int) ((t/100)/60)/60;
        return  hours + ":" +
                ((min<10)?"0":"") + min + ":" +
                ((sec<10)?"0":"") + sec;
    }





    /** Permette il movimento della camera **/
    private void moveCamera(double lat, double lng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
    }




    /** Permette il movimento della camera con animazione **/
    private void animateCamera(double lat, double lng, float zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), zoom));
    }





    /** Setta alcuni valori globali in base al tipo di allenamento scelto **/
    private void setWorkoutValues(int imageId) {
        switch (imageId) {
            case 1:
                IMAGE_ID = R.drawable.ic_walk;
                WORKOUT_DESCRIPTION = "Camminata";
                break;
            case 2:
                IMAGE_ID = R.drawable.ic_run;
                WORKOUT_DESCRIPTION = "Corsa";
                break;
            case 3:
                IMAGE_ID = R.drawable.ic_bike;
                WORKOUT_DESCRIPTION = "Ciclismo";
                break;
            case 4:
                IMAGE_ID = R.drawable.ic_sports_soccer;
                WORKOUT_DESCRIPTION = "Calcio";
                break;
            case 5:
                IMAGE_ID = R.drawable.ic_sports_basketball;
                WORKOUT_DESCRIPTION = "Basket";
                break;
            case 6:
                IMAGE_ID = R.drawable.ic_sports_volleyball;
                WORKOUT_DESCRIPTION = "Pallavolo";
                break;
            case 7:
                IMAGE_ID = R.drawable.ic_sports_martialarts;
                WORKOUT_DESCRIPTION = "Arti Marziali";
                break;
            case 8:
                IMAGE_ID = R.drawable.ic_sports_tennis;
                WORKOUT_DESCRIPTION = "Tennis";
                break;
            case 9:
                IMAGE_ID = R.drawable.ic_sports_swimming;
                WORKOUT_DESCRIPTION = "Nuoto";
                break;
            default:
                IMAGE_ID = R.drawable.ic_run;
                WORKOUT_DESCRIPTION = "Corsa";
                Log.e("GoogleMapsActivity.onCreate", "default case in switch clause");
                break;
        }
        UPDATE_TIME = 1000;
        MIN_DISTANCE = 1;
        stepsImageView.setImageResource(IMAGE_ID);
    }

}