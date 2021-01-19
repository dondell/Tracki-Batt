package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AlertModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int mode = -1; //1=plugged, 2=unpugge, 3=charging level,4=discharging level
    public int modeValue = 0;
    public int event = -1; //1=media, 2=ringtone, 3=text to speech
    public String eventString;
    public String name;
    public String description;
    public String logic;
    public boolean played = false;

    public AlertModel() {
    }

    public AlertModel(int mode, int modeValue, int event, String eventString, String name, String description) {
        this.mode = mode;
        this.modeValue = modeValue;
        this.event = event;
        this.eventString = eventString;
        this.name = name;
        this.description = description;
    }

    protected AlertModel(Parcel in) {
        id = in.readInt();
        mode = in.readInt();
        modeValue = in.readInt();
        event = in.readInt();
        eventString = in.readString();
        name = in.readString();
        description = in.readString();
        logic = in.readString();
        played = in.readByte() != 0;
    }

    public static final Creator<AlertModel> CREATOR = new Creator<AlertModel>() {
        @Override
        public AlertModel createFromParcel(Parcel in) {
            return new AlertModel(in);
        }

        @Override
        public AlertModel[] newArray(int size) {
            return new AlertModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(mode);
        parcel.writeInt(modeValue);
        parcel.writeInt(event);
        parcel.writeString(eventString);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(logic);
        parcel.writeByte((byte) (played ? 1 : 0));
    }
}
