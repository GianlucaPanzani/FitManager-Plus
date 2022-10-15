package it.unipi.di.sam.m550358.fitmanager.fragment.Pianification;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;


public class Plan implements Parcelable {
    String textHours;
    String textWork;
    String date;
    boolean checked;


    public Plan(String date, String hours, String work) {
        this.date = date;
        this.textHours = hours;
        this.textWork = work;
        this.checked = false;
    }



    public Plan(String date, String hours, String work, boolean checked) {
        this.date = date;
        this.textHours = hours;
        this.textWork = work;
        this.checked = checked;
    }



    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Plan(Parcel in) {
        date = in.readString();
        textHours = in.readString();
        textWork = in.readString();
        checked = in.readBoolean();
    }



    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date);
        parcel.writeString(textHours);
        parcel.writeString(textWork);
        parcel.writeBoolean(checked);
    }



    public void setChecked(boolean b) { checked = b; }

    public String getDate() {
        return date;
    }

    public String getTextHours() {
        return textHours;
    }

    public String getTextWork() {
        return textWork;
    }
}
