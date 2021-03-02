package com.ami.batterwatcher.data;

import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.ArrayList;
import java.util.List;

public class UsageWithCurrentSamplings {
    public UsageModel usageModel;
    public List<Integer> appCurrentSampling = new ArrayList<>();

    public UsageWithCurrentSamplings(UsageModel usageModel) {
        this.usageModel = usageModel;
    }
}
