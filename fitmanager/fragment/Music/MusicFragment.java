package it.unipi.di.sam.m550358.fitmanager.fragment.Music;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.Plan;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.PlanAdapter;


public class MusicFragment extends Fragment implements View.OnClickListener {

    private Activity activity;
    private final String TITLE = "La tua Musica";

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabasePicker databasePicker;

    private ProgressBar progressBar;

    private ArrayList<Song> songsList;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        if (activity == null)
            Log.e("MusicFragment.onCreate", "activity null");
        activity.setTitle(TITLE);

        layoutManager = new LinearLayoutManager(activity);
        databasePicker = new DatabasePicker(activity);

        // avvio del task asincrono per ricavare i dati dal database
        databasePicker.execute();
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, vg, false);
    }



    @Override
    public void onStart() {
        super.onStart();

        progressBar = activity.findViewById(R.id.music_progressbar);
        ((CardView) activity.findViewById(R.id.music_card_view))
                .setOnClickListener(this);

        // inizializzazione recycler view
        recyclerView = (RecyclerView) activity.findViewById(R.id.music_recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);

        // recupero dati dall'esecuzione dell'AsyncTask
        progressBar.setVisibility(ProgressBar.VISIBLE);
        try {
            if (songsList == null)
                songsList = databasePicker.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        musicAdapter = MusicAdapter.getInstance(activity);
        musicAdapter.setSongsList(songsList);
        recyclerView.setAdapter(musicAdapter);

        progressBar.setVisibility(ProgressBar.GONE);
    }




    /*********** Classe cha ha il compito di prendere i dati dal database ***********/
    private class DatabasePicker extends AsyncTask<Void,Void,ArrayList<Song>> {
        Context context;

        /** Costruttore della classe **/
        public DatabasePicker(Context context) {
            this.context = context;
        }

        @Override
        protected ArrayList<Song> doInBackground(Void... voids) {
            ArrayList<Song> songsList = new ArrayList<>();
            DatabaseHandler db = new DatabaseHandler(context);

            // allocazione dei dati nel DB nella struttura dati songsMap
            Cursor cursor;
            cursor = db.getAllRecordsFromMusicTable();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (!songsListContainsObject(songsList,cursor.getString(1)))
                    songsList.add(
                            new Song(
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    cursor.getInt(3),
                                    -1
                            )
                    );
                cursor.moveToNext();
            }
            cursor.close();

            // setta le posizioni delle canzoni
            for (int i = 0; i < songsList.size(); ++i)
                songsList.get(i).setPosition(i);

            return songsList;
        }

        /** Restituisce true se la lista contiene la canzone identificata da "uri", false altrimenti **/
        private boolean songsListContainsObject(ArrayList<Song> songsList, String uri) {
            for (Song song : songsList)
                if (song.getUri().equals(uri))
                    return true;
            return false;
        }

    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        requestStoragePermissions();

        // caso di click per l'aggiunta di una canzone alla playlist
        if (view.getId() == R.id.music_card_view || view.getId() == R.id.music_title) {
            Intent intent = Intent.createChooser(
                    new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    ),
                    "Seleziona una canzone"
            );
            try {
                startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("MusicFragment.onClick", "else clause");
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || requestCode != 1) {
            progressBar.setVisibility(ProgressBar.GONE);
            Log.e("MusicFragment.onActivityResult", "bad parameters");
            return;
        }

        // recupero dei dati in memoria
        String[] projection = new String[]{
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = activity.getContentResolver()
                .query(data.getData(), projection, null, null,null);
        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        cursor.moveToFirst();
        String realPath = cursor.getString(dataIndex);
        String duration = cursor.getString(durationIndex);

        // aggiunta della canzone alla lista dell adapter e al database
        musicAdapter.addItem(realPath, duration);
        progressBar.setVisibility(ProgressBar.GONE);
    }



    /** Richiede all'utente i permessi di salvataggio dei dati in memoria **/
    private void requestStoragePermissions() {
        // caso di mancanza dei permessi di lettura dei dati in memoria
        if (!(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {

            // caso di necessita' di richiesta esplicita dei permessi all'utente
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.READ_EXTERNAL_STORAGE))
                new AlertDialog.Builder(activity)
                        .setTitle("Permessi necessari")
                        .setMessage("Per accedere ai file musicali é necessario il consenso dell'utente")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(
                                        activity,
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1
                                );
                            }
                        }).create().show();
            else
                ActivityCompat.requestPermissions(
                        activity,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        1
                );

        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(activity, "Permissions granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(activity, "Permissions denied", Toast.LENGTH_SHORT).show();
    }


}