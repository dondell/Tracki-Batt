package com.ami.batterwatcher.viewmodels;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ChargeWithPercentageModel {
    @Embedded
    public ChargeModel chargeModel;
    @Relation(
            parentColumn = "chargeId",
            entityColumn = "chargeModelId"
    )
    public List<PercentageModel> percentageModels;

}
