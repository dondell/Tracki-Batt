package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChargeModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long chargeId;
    public String name;
    public int event = -1; //1=media, 2=ringtone, 3=text to speech
    public String eventString;

    public ChargeModel() {
    }

    public ChargeModel(long chargeId, String name) {
        this.chargeId = chargeId;
        this.name = name;
    }

    public ChargeModel(long chargeId, String name, String eventString) {
        this.chargeId = chargeId;
        this.name = name;
        this.eventString = eventString;
    }

    public ChargeModel(long chargeId, String name, int event, String eventString) {
        this.chargeId = chargeId;
        this.name = name;
        this.event = event;
        this.eventString = eventString;
    }

    protected ChargeModel(Parcel in) {
        chargeId = in.readLong();
        name = in.readString();
        event = in.readInt();
        eventString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(chargeId);
        dest.writeString(name);
        dest.writeInt(event);
        dest.writeString(eventString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChargeModel> CREATOR = new Creator<ChargeModel>() {
        @Override
        public ChargeModel createFromParcel(Parcel in) {
            return new ChargeModel(in);
        }

        @Override
        public ChargeModel[] newArray(int size) {
            return new ChargeModel[size];
        }
    };
}
