package it.unipi.di.sam.m550358.fitmanager.fragment.Home;

import android.os.Parcel;
import android.os.Parcelable;

import it.unipi.di.sam.m550358.fitmanager.fragment.Music.Song;

public class Workout implements Parcelable {
    String date, hours, description;
    int time, steps, stepsGoal;
    double speed;
    int imageId;


    public Workout(String date, String hours, String description, int time, int steps, int stepsGoal, double speed, int imageId) {
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.time = time;
        this.steps = steps;
        this.stepsGoal = stepsGoal;
        this.speed = speed;
        this.imageId = imageId;
    }


    protected Workout(Parcel in) {
        date = in.readString();
        hours = in.readString();
        description = in.readString();
        time = in.readInt();
        steps = in.readInt();
        stepsGoal = in.readInt();
        speed = in.readDouble();
        imageId = in.readInt();
    }


    public static final Creator<Workout> CREATOR = new Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date);
        parcel.writeString(hours);
        parcel.writeString(description);
        parcel.writeInt(time);
        parcel.writeInt(steps);
        parcel.writeInt(stepsGoal);
        parcel.writeDouble(speed);
        parcel.writeInt(imageId);
    }


    public int getImageId() {
        return imageId;
    }

    public String getDate() {
        return date;
    }

    public String getHours() {
        return hours;
    }

    public String getDescription() {
        return description;
    }

    public int getSteps() {
        return steps;
    }

    public int getStepsGoal() {
        return stepsGoal;
    }

    public double getSpeed() {
        return speed;
    }

    public int getTime() {
        return time;
    }
}

