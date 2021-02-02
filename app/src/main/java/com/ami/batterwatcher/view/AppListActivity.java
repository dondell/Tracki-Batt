package com.ami.batterwatcher.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.adapters.AppListAdapter;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.ActivityApplistBinding;
import com.ami.batterwatcher.viewmodels.AppModel;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends BaseActivity {
    private ActivityApplistBinding viewDataBinding;
    private PackageManager packageManager;
    private ArrayList<AppModel> appListMainArrayList;
    private RecyclerView recyclerView_list;
    private AppListAdapter listAdapter;

    @Override
    protected int setLayout() {
        return R.layout.activity_applist;
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
        setActivityTitle("Applications");
        recyclerView_list = viewDataBinding.recyclerViewList;
        recyclerView_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView_list.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_list.setItemAnimator(new DefaultItemAnimator());
        showBackButton(false);
        listAdapter = new AppListAdapter(this, appListMainArrayList, new AppListAdapter.ClickListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onLongPress(int position) {

            }
        });
        recyclerView_list.setAdapter(listAdapter);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void setData() {
        try {
            packageManager = getPackageManager();
            appListMainArrayList = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

            for (ResolveInfo resolveInfo : resolveInfoList) {
                AppModel appListMain = new AppModel();
                appListMain.drawable = resolveInfo.activityInfo.loadIcon(packageManager);
                appListMain.appName = resolveInfo.loadLabel(packageManager).toString();
                appListMain.packageName = resolveInfo.activityInfo.packageName;
                appListMainArrayList.add(appListMain);
            }
            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
