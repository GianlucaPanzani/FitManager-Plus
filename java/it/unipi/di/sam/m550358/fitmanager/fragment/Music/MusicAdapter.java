package it.unipi.di.sam.m550358.fitmanager.fragment.Music;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Home.WorkoutAdapter;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.SongViewHolder> {
    private Context context;
    private PopupMenu songMenu;

    public static MusicAdapter thisInstance;

    private ArrayList<Song> songsList;

    private DatabaseHandler db;

    private static MediaPlayer mediaPlayer;



    /**************** Classe ViewHolder ****************/
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView titleTV;
        CardView buttonCV;

        /** Costruttore della classe **/
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.music_title);
            buttonCV = itemView.findViewById(R.id.music_item_card_view);
        }
    }



    /** Costruttore della classe **/
    public MusicAdapter(Context context) {
        this.context = context;
        db = new DatabaseHandler(context);
    }



    /** Singleton: metodo che assicura la creazione di un'unica istanza di questa classe **/
    public static MusicAdapter getInstance(Context context) {
        if (thisInstance == null)
            thisInstance = new MusicAdapter(context);
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        return thisInstance;
    }



    /** Setta la lista di canzoni **/
    public void setSongsList(ArrayList<Song> songsList){
        if (this.songsList == null && songsList != null)
            this.songsList = songsList;
    }



    /** Restituisce l'oggetto MediaPlayer della classe **/
    public MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        return mediaPlayer;
    }



    /**
     *  Aggiunge la canzone identificata dall'uri passata come parametro alla lista e al
     *  database (a quest'ultimo in modo asincrono)
     **/
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addItem(String uri, String duration) {
        String title = uri.substring(uri.lastIndexOf("/") + 1);
        int durationSec = Integer.parseInt(duration)/1000;

        Song song = new Song(
                uri,
                title,
                durationSec,
                songsList.size()
        );

        // aggiunge la canzone al database se non gia' presente nella lista
        if (!songsListContainsSong(uri)) {
            // insert nel database
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!db.insertMusicRecord(title, String.valueOf(durationSec), uri))
                        Log.e("MusicAdapter.addItem","item not added on database");
                }
            }).start();

            // aggiunta alla lista
            songsList.add(song);
            this.notifyItemInserted(song.position);
            Toast.makeText(context, "Canzone aggiunta", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Canzone giá presente", Toast.LENGTH_SHORT).show();
    }




    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_song, parent, false)
        );
    }




    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songVH, int i) {
        Song song = songsList.get(i);

        songVH.titleTV.setText(song.title);

        // CardView che se cliccata avvia la canzone
        songVH.buttonCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // lancio dell'intent all'activity che si occupa della riproduzione musicale
                Intent intent = new Intent(context, MusicPlayerActivity.class);
                if (mediaPlayer == null || !mediaPlayer.isPlaying())
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /*if (mediaPlayer.isPlaying()) {
                    intent.putExtra("PROGRESS", mediaPlayer.getCurrentPosition());
                    intent.putExtra("POSITION", song.position);
                    intent.putParcelableArrayListExtra("SONGS_LIST", songsList);
                }*/
                context.startActivity(intent);
                MusicPlayerActivity.setPosition(songsList, song.position);
            }
        });

        // CardView che con un long click apre un menu che permette la rimozione della canzone
        songVH.buttonCV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // creazione del menu
                songMenu = new PopupMenu(context,view);
                songMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int i = song.position;

                        // if che non permette l'eliminazione delle canzoni in riproduzione
                        if (MusicPlayerActivity.position == i) {
                            Toast.makeText(context,"La canzone è in riproduzione",Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        // caso di rimozione della canzone dalla lista e dal database (in modo asincrono)
                        if (menuItem.getItemId() == R.id.remove_item) {
                            // rimozione da database
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!db.deleteMusicRecord(song.uriPath))
                                        Log.e("MusicAdapter.onMenuItemClick","song not deleted from database");
                                }
                            }).start();

                            // rimozione da lista
                            songsList.remove(i);

                            // aggiorna il campo "position" delle canzoni
                            updateSongsIndex(i);

                            // setta il campo "removed" dell'activity che si occupa della
                            // riproduzione musicale all'indice della canzone rimossa
                            MusicPlayerActivity.removed = i;

                            thisInstance.notifyItemRemoved(i);
                            return true;
                        }
                        return false;
                    }
                });
                songMenu.inflate(R.menu.remove_menu);
                songMenu.show();
                return false;
            }
        });

    }


    @Override
    public int getItemCount() {
        return songsList.size();
    }



    /** Restituisce true se la canzone e' presente nella lista, false altrimenti **/
    private boolean songsListContainsSong(String uri) {
        for (Song song : songsList)
            if (song.uriPath.equals(uri))
                return true;
        return false;
    }



    /**
     *  Setta il campo position delle canzoni all'indice corretto
     *  (chiamato dopo una rimozione dalla lista)
     **/
    public void updateSongsIndex(int index) {
        for (int i = index; i < songsList.size(); ++i)
            songsList.get(i).position = i;
    }

}
