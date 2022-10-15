package it.unipi.di.sam.m550358.fitmanager.fragment.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Run.StartFragment;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;

    private final String TITLE = "Impostazioni";

    public static String NAME;
    public static String GENDER;
    public static int HIGH;
    public static int KG;
    public static int STEPS_GOAL;
    public static boolean MAPS_ACTIVE;


    public SettingsFragment(Context context) {
        this.context = context;
        setMyPreferences();
    }



    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (getActivity() != null)
            getActivity().setTitle(TITLE);

        // registrazione del listener alle modifiche sulle preferences
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }




    /** Permette di settare i valori locali **/
    public void setMyPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        SettingsFragment.NAME = sharedPref.getString(
                context.getResources().getString(R.string.pref_profile_name_key),
                "Nome non impostato"
        );
        SettingsFragment.GENDER = sharedPref.getString(
                context.getResources().getString(R.string.pref_profile_gender_key),
                "Genere non impostato"
        );
        SettingsFragment.MAPS_ACTIVE = sharedPref.getBoolean(
                context.getResources().getString(R.string.pref_maps_active_key),
                true
        );
        try {
            SettingsFragment.KG = Integer.parseInt(sharedPref.getString(
                    context.getResources().getString(R.string.pref_profile_weight_key),
                    "0")
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
            SettingsFragment.KG = 0;
        }
        try {
            SettingsFragment.HIGH = Integer.parseInt(sharedPref.getString(
                            context.getResources().getString(R.string.pref_profile_high_key),
                            "0")
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
            SettingsFragment.HIGH = 0;
        }
        try {
            SettingsFragment.STEPS_GOAL = Integer.parseInt(sharedPref.getString(
                    context.getResources().getString(R.string.pref_goals_steps_key),
                    "4000")
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
            SettingsFragment.STEPS_GOAL = 4000;
        }
    }





    /** Callback chiamata alla modifica di una preferenza da parte dell'utente **/
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (findPreference(s) == null) {
            Log.e("SettingsFragment.onSharedPreferenceChanged", "not valid key");
            return;
        }

        if (context.getResources().getString(R.string.pref_profile_name_key).equals(s))
            NAME = sharedPreferences.getString(s, "Nome non impostato");

        else if (context.getResources().getString(R.string.pref_profile_gender_key).equals(s))
            GENDER = sharedPreferences.getString(s, "Genere non impostato");

        else if (context.getResources().getString(R.string.pref_profile_weight_key).equals(s))
            try {
                KG = Integer.parseInt(sharedPreferences.getString(s, "0").split(" ")[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Il peso deve essere un valore intero",Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString(s,"").apply();
                KG = 0;
            }

        else if (context.getResources().getString(R.string.pref_profile_high_key).equals(s))
            try {
                HIGH = Integer.parseInt(sharedPreferences.getString(s, "0").split(" ")[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"L'altezza deve essere un valore intero",Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString(s,"").apply();
                HIGH = 0;
            }

        else if (context.getResources().getString(R.string.pref_goals_steps_key).equals(s))
            STEPS_GOAL = Integer.parseInt(sharedPreferences.getString(s, "4000"));

        else if (context.getResources().getString(R.string.pref_maps_active_key).equals(s))
            MAPS_ACTIVE = sharedPreferences.getBoolean(s,true);

        else
            Log.e("SettingsFragment.onSharedPreferenceChanged", "else case in if");

    }





    @Override
    public void onDestroy() {
        super.onDestroy();

        // deregistrazione
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

}