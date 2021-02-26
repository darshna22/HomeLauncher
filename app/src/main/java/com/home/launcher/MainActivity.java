package com.home.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.home.launcher.fragments.AppDrawerFragment;
import com.home.launcher.fragments.HomescreenFragment;
import com.layoutxml.applistmanagerlibrary.AppList;
import com.layoutxml.applistmanagerlibrary.interfaces.ActivityListener;
import com.layoutxml.applistmanagerlibrary.interfaces.NewActivityListener;
import com.layoutxml.applistmanagerlibrary.interfaces.SortListener;
import com.layoutxml.applistmanagerlibrary.interfaces.UninstalledActivityListener;
import com.layoutxml.applistmanagerlibrary.objects.AppData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityListener, NewActivityListener, UninstalledActivityListener, SortListener {

  private static final String TAG = "MainActivity";
  public static List<AppData> appDataList = new ArrayList<>();

  @Override
  public void onBackPressed() {
  }

  private Intent mainIntent = new Intent();
  AppDrawerFragment appdrawerFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    AppList.registerListeners(null, MainActivity.this, null, MainActivity.this, null, MainActivity.this, MainActivity.this);
    appdrawerFragment = new AppDrawerFragment();

    mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    AppList.getAllActivities(getApplicationContext(), mainIntent, 0);
    if (appdrawerFragment.progressBar != null)
      appdrawerFragment.progressBar.setVisibility(View.VISIBLE);

    BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
    navigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (menuItem.getItemId()) {
              case R.id.drawerItem:
                appdrawerFragment = new AppDrawerFragment();
                appdrawerFragment.setArguments(getIntent().getExtras());
                transaction.replace(R.id.fragment_container, appdrawerFragment);
                transaction.commit();
                return true;
              case R.id.homeItem:
                HomescreenFragment homescreenFragmentFragment = new HomescreenFragment();
                homescreenFragmentFragment.setArguments(getIntent().getExtras());
                transaction.replace(R.id.fragment_container, homescreenFragmentFragment);
                transaction.commit();
                return true;
            }
            return false;
          }
        }
    );

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    AppList.destroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (appDataList != null && appDataList.size() != 0) {
      AppList.getAllNewActivities(getApplicationContext(), appDataList, mainIntent, 1);
      AppList.getAllUninstalledActivities(getApplicationContext(), appDataList, mainIntent, 2);
    }
  }

  @Override
  public void activityListener(List<AppData> list, Intent intent, Integer integer, Integer integer1, Boolean aBoolean, Integer integer2) {
    AppList.sort(list, AppList.BY_APPNAME_IGNORE_CASE, AppList.IN_ASCENDING, integer2);
  }

  @Override
  public void newActivityListener(List<AppData> list, Intent intent, Integer integer, Integer integer1, Boolean aBoolean, Boolean aBoolean1, Integer integer2) {
    list.addAll(appDataList);
    if (aBoolean1)
      AppList.getAllNewActivities(getApplicationContext(), appDataList, mainIntent, 1);
    else
      AppList.sort(list, AppList.BY_APPNAME_IGNORE_CASE, AppList.IN_ASCENDING, integer2);
  }

  @Override
  public void uninstalledActivityListener(List<AppData> list, Intent intent, Integer integer, Integer integer1, Boolean aBoolean, Boolean aBoolean1, Integer integer2) {
    appDataList.removeAll(list);
  }

  @Override
  public void sortListener(List<AppData> list, Integer integer, Integer integer1, Integer integer2) {
    if (appdrawerFragment.progressBar != null)
      appdrawerFragment.progressBar.setVisibility(View.GONE);
    appDataList.clear();
    appDataList.addAll(list);
    AppData itself = new AppData();
    itself.setPackageName(getPackageName());
    appDataList.remove(itself);
    if (appdrawerFragment.appListAdapter != null)
      appdrawerFragment.appListAdapter.notifyDataSetChanged();
  }
}
