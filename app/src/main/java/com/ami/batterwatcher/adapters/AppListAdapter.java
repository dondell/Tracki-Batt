package com.ami.batterwatcher.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.BR;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.ItemAlertBinding;
import com.ami.batterwatcher.databinding.ItemAppBinding;
import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.AppModel;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.DataViewHolder> {

    private BaseActivity context;
    private List<AppModel> list;
    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(int position);
        void onLongPress(int position);
    }

    public AppListAdapter(BaseActivity context, List<AppModel> list, ClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ItemAppBinding binding = ItemAppBinding.inflate(layoutInflater, parent, false);
        return new DataViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        AppModel p = list.get(position);
        holder.binding.setVariable(BR.app, p);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        ItemAppBinding binding;

        DataViewHolder(@NonNull ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClick(getLayoutPosition());
                }
            });
            this.binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    clickListener.onLongPress(getLayoutPosition());
                    return false;
                }
            });
        }
    }


}
