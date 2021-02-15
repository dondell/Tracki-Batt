package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UsageModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long usageId;
    public String packageName;
    public float percentage;
    public float mAh; //this is the mAh added on top of the current mAh
    public float current_beforeLaunch; //this is the current mAh usage of the device before app launch
    public float current_mAh; //this is the current mAh usage of the device
    public float avg_mAh; //this is the current mAh usage of the device
    public float capacity_mAh; //this is the capacity of the device in mAh
    public float current_battery_percent; //this is the current battery percentage of the device

    public UsageModel() {
    }

    public UsageModel(String packageName, float percentage, float mAh, float current_mAh, float avg_mAh, float capacity_mAh) {
        this.packageName = packageName;
        this.percentage = percentage;
        this.mAh = mAh;
        this.current_mAh = current_mAh;
        this.avg_mAh = avg_mAh;
        this.capacity_mAh = capacity_mAh;
    }

    protected UsageModel(Parcel in) {
        packageName = in.readString();
        percentage = in.readFloat();
        mAh = in.readFloat();
        current_mAh = in.readFloat();
        avg_mAh = in.readFloat();
        capacity_mAh = in.readFloat();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(usageId);
        dest.writeString(packageName);
        dest.writeFloat(percentage);
        dest.writeFloat(mAh);
        dest.writeFloat(current_beforeLaunch);
        dest.writeFloat(current_mAh);
        dest.writeFloat(avg_mAh);
        dest.writeFloat(capacity_mAh);
        dest.writeFloat(current_battery_percent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UsageModel> CREATOR = new Creator<UsageModel>() {
        @Override
        public UsageModel createFromParcel(Parcel in) {
            return new UsageModel(in);
        }

        @Override
        public UsageModel[] newArray(int size) {
            return new UsageModel[size];
        }
    };
}
