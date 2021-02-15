package com.ami.batterwatcher.view;

import android.app.usage.EventStats;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.usage.UsageViewModel;
import com.ami.batterwatcher.databinding.ActivityUsageListBinding;
import com.ami.batterwatcher.usage.UsageContract;
import com.ami.batterwatcher.usage.UsagePresenter;
import com.ami.batterwatcher.usage.UsageStatAdapter;
import com.ami.batterwatcher.usage.UsageStatsWrapper;
import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UsageActivity extends BaseActivity implements UsageContract.View {

    private ActivityUsageListBinding viewDataBinding;
    private ProgressBar progressBar;
    private TextView permissionMessage;

    private UsageContract.Presenter presenter;
    private UsageStatAdapter adapter;
    private UsageViewModel usageViewModel;

    @Override
    protected int setLayout() {
        return R.layout.activity_usage_list;
    }

    @Override
    protected View setLayoutBinding() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewDataBinding = DataBindingUtil.inflate(inflater, setLayout(), null, false);
        viewDataBinding.executePendingBindings();
        return viewDataBinding.getRoot();
    }

    @Override
    protected void setViews() {
        showBackButton(true);
        setTitle("Apps Usage");
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progress_bar);
        permissionMessage = findViewById(R.id.grant_permission_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsageStatAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        permissionMessage.setOnClickListener(v -> openSettings());
        usageViewModel = new ViewModelProvider
                .AndroidViewModelFactory(getApplication())
                .create(UsageViewModel.class);
        presenter = new UsagePresenter(this, this);
        viewDataBinding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewDataBinding.swiperefresh.setRefreshing(true);
                usageViewModel.getAll().observe(UsageActivity.this, new Observer<List<UsageModel>>() {
                    @Override
                    public void onChanged(List<UsageModel> usageModels) {
                        presenter.retrieveUsageStats(usageModels);
                        viewDataBinding.swiperefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    protected void setData() {

    }

    private void openSettings() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("xxx", "xxx onResume");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                permissionMessage.setVisibility(GONE);
                showProgressBar(true);
            }
        });
        usageViewModel.getAll().observe(UsageActivity.this, new Observer<List<UsageModel>>() {
            @Override
            public void onChanged(List<UsageModel> usageModels) {
                presenter.retrieveUsageStats(usageModels);
            }
        });
    }

    @Override
    public void onUsageStatsRetrieved(List<UsageStatsWrapper> list) {
        Log.e("xxx", "xxx onUsageStatsRetrieved");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                showProgressBar(false);
                permissionMessage.setVisibility(GONE);
                adapter.setList(list);
            }
        });
    }

    @Override
    public void onEventStatsRetrieved(List<EventStats> list) {

    }

    @Override
    public void onUserHasNoPermission() {
        Log.e("xxx", "xxx onUserHasNoPermission");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                showProgressBar(false);
                permissionMessage.setVisibility(VISIBLE);
            }
        });
    }

    private void showProgressBar(boolean show) {
        Log.e("xxx", "xxx showProgressBar " + show);
        if (show) {
            progressBar.setVisibility(VISIBLE);
        } else {
            progressBar.setVisibility(GONE);
        }
    }

}
