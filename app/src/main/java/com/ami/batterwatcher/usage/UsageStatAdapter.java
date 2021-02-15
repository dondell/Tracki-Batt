package com.ami.batterwatcher.usage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class UsageStatAdapter extends RecyclerView.Adapter<UsageStatVH> {

    private List<UsageStatsWrapper> list;
    private BaseActivity baseActivity;

    public UsageStatAdapter(BaseActivity baseActivity){
        this.baseActivity = baseActivity;
        list = new ArrayList<>();
    }

    @Override
    public UsageStatVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usage_stat, parent, false);
        return new UsageStatVH(view, baseActivity);
    }

    @Override
    public void onBindViewHolder(UsageStatVH holder, int position) {
        holder.bindTo(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<UsageStatsWrapper> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
