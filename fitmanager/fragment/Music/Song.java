package it.unipi.di.sam.m550358.fitmanager.fragment.Music;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.versionedparcelable.ParcelField;
import androidx.versionedparcelable.VersionedParcelable;

public class Song implements Parcelable {
    int currentSec;
    int durationSec;
    int position;
    String title;
    String uriPath;


    public Song(String uriPath, String title, int durationSec, int position) {
        this.uriPath = uriPath;
        this.title = title;
        this.durationSec = durationSec;
        this.position = position;
        currentSec = 0;
    }



    protected Song(Parcel in) {
        currentSec = in.readInt();
        durationSec = in.readInt();
        position = in.readInt();
        title = in.readString();
        uriPath = in.readString();
    }


    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(currentSec);
        parcel.writeInt(durationSec);
        parcel.writeInt(position);
        parcel.writeString(title);
        parcel.writeString(uriPath);
    }



    public String getTitle() {
        return title;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public String getUri() {
        return uriPath;
    }

    public int getCurrentSec() {
        return currentSec;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
