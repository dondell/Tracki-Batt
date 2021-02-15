package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DischargingSampleModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long dischargingSampleId;
    public long diffTime;

    public DischargingSampleModel() {
    }

    public DischargingSampleModel(long dischargingSampleId, long diffTime) {
        this.dischargingSampleId = dischargingSampleId;
        this.diffTime = diffTime;
    }


    protected DischargingSampleModel(Parcel in) {
        dischargingSampleId = in.readLong();
        diffTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dischargingSampleId);
        dest.writeLong(diffTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DischargingSampleModel> CREATOR = new Creator<DischargingSampleModel>() {
        @Override
        public DischargingSampleModel createFromParcel(Parcel in) {
            return new DischargingSampleModel(in);
        }

        @Override
        public DischargingSampleModel[] newArray(int size) {
            return new DischargingSampleModel[size];
        }
    };
}
