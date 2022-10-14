package it.unipi.di.sam.m550358.fitmanager.fragment.Music;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Run.GoogleMapsActivity;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener {
    TextView durationTimeTV, currentTimeTV;
    ImageView playAndPauseImage, skipPreviousImage, skipNextImage;
    SeekBar seekBar;

    private final String PROGRESS_KEY = "PROGRESS";
    private final String POSITION_KEY = "POSITION";
    private final String SONGS_LIST_KEY = "SONGS_LIST";

    private MusicAdapter musicAdapter;
    private static MediaPlayer mediaPlayer;
    private static ArrayList<Song> songsList;
    public static int position = 0;
    public static int removed = -1;

    private int precPosition = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // recupero dell'istanza della classe MusicAdapter
        if (musicAdapter == null)
            musicAdapter = MusicAdapter.getInstance(this);

        // inizializzazione media player
        if (mediaPlayer == null) {
            mediaPlayer = musicAdapter.getMediaPlayer();
            mediaPlayer.reset();
        }
        mediaPlayer.setOnPreparedListener(this);

        // inizializzazioni oggetti di layout
        durationTimeTV = findViewById(R.id.music_song_duration);
        currentTimeTV = findViewById(R.id.music_song_currentTime);
        skipPreviousImage = findViewById(R.id.music_previous_image);
        skipNextImage = findViewById(R.id.music_next_image);
        playAndPauseImage = findViewById(R.id.music_play_image);
        seekBar = findViewById(R.id.music_song_seekbar);

        skipNextImage.setOnClickListener(this);
        skipPreviousImage.setOnClickListener(this);
        playAndPauseImage.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean changedByUser) {
                if (mediaPlayer == null)
                    return;
                if (changedByUser)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        // runnable eseguito sul thread UI per aggiornare l'interfaccia utente ai cambiamenti della
        // posizione della seekbar e dei minuti/secondi della canzone
        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // caso di canzone rimossa in posizione "removed"
                if (removed > -1) {
                    if (removed < position)
                        position--;
                    removed = -1;
                    setMusicResourcesAndStart();

                // caso di posizione modificata e di avvio della canzone nella nuova posizione
                } else if(precPosition != position) {
                    setMusicResourcesAndStart();

                // caso di aggiornamento della progressbar e del tempo
                } else if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    // caso di canzone finita e di avvio canzone successiva
                    if (mediaPlayer.getCurrentPosition() >= seekBar.getMax() && seekBar.getMax() > 0)
                        playNext();
                    currentTimeTV.setText(fromMillisToString(mediaPlayer.getCurrentPosition()));
                }
                // codice preso da spiegazione account youtube "Easy Tuto"
                new Handler().postDelayed(this, 100);
            }
        });

        // resetta il media player e avvia la canzone nella posizione "position"
        setMusicResourcesAndStart();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.music_play_image:
                playOrPause();
                break;
            case R.id.music_next_image:
                playNext();
                break;
            case R.id.music_previous_image:
                playPrevious();
                break;
            default:
                Log.e("MusicPlayerActivity.onClick", "default case in switch clause");
                break;
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }



    /** Converte il parametro da millisecondi a una stringa con il seguente formato: "h:m:s" **/
    public String fromMillisToString(long millis) {
        int hours   = (int) (millis/(1000*60*60));
        int minutes = (int) (millis%(1000*60*60))/(1000*60);
        int seconds = (int) (((millis%(1000*60*60))%(1000*60))/1000);
        return  ((hours>0)?(hours+":"):("")) +
                ((minutes>9)?(minutes):("0"+minutes)) + ":" +
                ((seconds>9)?(seconds):("0"+seconds));
    }




    /** Resetta il media player e avvia la canzone nella posizione "position" **/
    public void setMusicResourcesAndStart() {
        mediaPlayer = musicAdapter.getMediaPlayer();
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();

        // sostituzione della canzone da riprodurre con quella nella nuova posizione "position"
        Song song = songsList.get(position);
        precPosition = position;
        durationTimeTV.setText(fromMillisToString(((long) song.getDurationSec()*1000)-1000));
        playAndPauseImage.setImageResource(R.mipmap.ic_music_pause);
        setTitle(song.getTitle());

        // preparazione del media player alla riproduzione e setting della seekbar
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(song.getUri());
            mediaPlayer.prepareAsync();
            seekBar.setProgress(0);
            seekBar.setMax((song.getDurationSec()*1000)-2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /** Avvia la canzone se e' in pausa, altrimenti la stoppa **/
    public void playOrPause() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playAndPauseImage.setImageResource(R.mipmap.ic_music_pause);
        } else {
            mediaPlayer.pause();
            playAndPauseImage.setImageResource(R.mipmap.ic_music_play);
        }
    }



    /** Avvio della canzone precedente a quella attuale **/
    public void playPrevious() {
        position = (position+musicAdapter.getItemCount()-1) % musicAdapter.getItemCount();
        setMusicResourcesAndStart();
    }


    /** Avvio della canzone successiva a quella attuale **/
    public void playNext() {
        position = (position+1) % musicAdapter.getItemCount();
        setMusicResourcesAndStart();
    }



    /**
     *  Setta la lista delle canzoni (nel caso fosse cambiata) e setta la nuova posizione della
     *  canzone da riprodurre
     **/
    public static void setPosition(ArrayList<Song> songsList, int pos) {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        MusicPlayerActivity.songsList = songsList;
        position = pos;
    }


    /*@Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PROGRESS_KEY, mediaPlayer.getCurrentPosition());
        outState.putInt(POSITION_KEY, position);
        outState.putParcelableArrayList(SONGS_LIST_KEY, songsList);
    }*/
}