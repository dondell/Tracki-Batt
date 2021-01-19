package com.ami.batterwatcher.viewmodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PercentageModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long percentageId;
    public int percentage;
    public int chargeModelId;
    public boolean selected;

    public PercentageModel() {
    }

    public PercentageModel(long percentageId, int percentage, int chargeModelId, boolean selected) {
        this.percentageId = percentageId;
        this.percentage = percentage;
        this.chargeModelId = chargeModelId;
        this.selected = selected;
    }

    protected PercentageModel(Parcel in) {
        percentageId = in.readLong();
        percentage = in.readInt();
        chargeModelId = in.readInt();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(percentageId);
        dest.writeInt(percentage);
        dest.writeInt(chargeModelId);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PercentageModel> CREATOR = new Creator<PercentageModel>() {
        @Override
        public PercentageModel createFromParcel(Parcel in) {
            return new PercentageModel(in);
        }

        @Override
        public PercentageModel[] newArray(int size) {
            return new PercentageModel[size];
        }
    };
}
