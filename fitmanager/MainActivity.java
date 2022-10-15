package it.unipi.di.sam.m550358.fitmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import it.unipi.di.sam.m550358.fitmanager.fragment.Home.HomeFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.Workout;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.WorkoutAdapter;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.Song;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.PianificationFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.Plan;
import it.unipi.di.sam.m550358.fitmanager.fragment.Run.GoogleMapsActivity;
import it.unipi.di.sam.m550358.fitmanager.fragment.Run.StartFragment;
import it.unipi.di.sam.m550358.fitmanager.fragment.Settings.SettingsFragment;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, MenuItem.OnMenuItemClickListener {

    private FragmentManager fragmentManager;

    private static HomeFragment homeFragment;
    private static StartFragment startFragment;
    private static MusicFragment musicFragment;
    private static SettingsFragment settingsFragment;
    private static PianificationFragment planFragment;

    private Fragment currentFragment;




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creazione delle istanze della classe
        if (settingsFragment == null)
            settingsFragment = new SettingsFragment(this);
        if (homeFragment == null)
            homeFragment = new HomeFragment();
        if (startFragment == null)
            startFragment = new StartFragment(homeFragment);
        if (planFragment == null)
            planFragment = new PianificationFragment();
        if (musicFragment == null)
            musicFragment = new MusicFragment();

        fragmentManager = getSupportFragmentManager();

        ((FloatingActionButton) findViewById(R.id.run_floating_button))
                .setOnClickListener(this);

        ((BottomNavigationView) findViewById(R.id.bottom_nav_bar_view))
                .setOnItemSelectedListener(this::onMenuItemClick);

        transactionFromCurrentFragmentTo(homeFragment);
    }




    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.run_floating_button)
            return;

        // transazione + transizione
        transactionFromCurrentFragmentTo(startFragment);
    }



    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.home_menu:
                transactionFromCurrentFragmentTo(homeFragment);
                break;
            case R.id.music_menu:
                transactionFromCurrentFragmentTo(musicFragment);
                break;
            case R.id.goals_menu:
                transactionFromCurrentFragmentTo(planFragment);
                break;
            case R.id.settings_menu:
                transactionFromCurrentFragmentTo(settingsFragment);
                break;
            case R.id.empty_menu:
                return true;
            default:
                return false;
        }

        return true;
    }



    /** Effettua la transazione dal fragment corrente a quello passato come parametro **/
    public void transactionFromCurrentFragmentTo(Fragment fragment) {
        if (fragment == null)
            return;

        currentFragment = fragment;
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, currentFragment)
                .commit();

    }




    /**
     *  Restituisce la stringa che rappresenta la data ottenuta concatenando i parametri del
     *  metodo ottenendo il seguente formato: "day stringOf(month) year".
     **/
    public static String fromIntToDate(int day, int month, int year) {
        String date = day + " ";

        switch (month) {
            case 1:  date += "Gen "; break;
            case 2:  date += "Feb "; break;
            case 3:  date += "Mar "; break;
            case 4:  date += "Apr "; break;
            case 5:  date += "Mag "; break;
            case 6:  date += "Giu "; break;
            case 7:  date += "Lug "; break;
            case 8:  date += "Ago "; break;
            case 9:  date += "Set "; break;
            case 10: date += "Ott "; break;
            case 11: date += "Nov "; break;
            case 12: date += "Dic "; break;
            default: date += month + " ";
        }
        date += year;
        return date;
    }


}