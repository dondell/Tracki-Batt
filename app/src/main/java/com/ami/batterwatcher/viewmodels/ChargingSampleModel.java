package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChargingSampleModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long chargingSampleId;
    public long diffTime;

    public ChargingSampleModel() {
    }

    public ChargingSampleModel(long chargingSampleId, long diffTime) {
        this.chargingSampleId = chargingSampleId;
        this.diffTime = diffTime;
    }

    protected ChargingSampleModel(Parcel in) {
        chargingSampleId = in.readLong();
        diffTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(chargingSampleId);
        dest.writeLong(diffTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChargingSampleModel> CREATOR = new Creator<ChargingSampleModel>() {
        @Override
        public ChargingSampleModel createFromParcel(Parcel in) {
            return new ChargingSampleModel(in);
        }

        @Override
        public ChargingSampleModel[] newArray(int size) {
            return new ChargingSampleModel[size];
        }
    };
}
