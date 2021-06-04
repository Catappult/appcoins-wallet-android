package com.asfoundation.wallet.viewmodel;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.asfoundation.wallet.ui.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseNavigationActivity extends BaseActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener {

  //protected void initBottomNavigation() {
  //  BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
  //  navigation.setOnNavigationItemSelectedListener(this);
  //}

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    return false;
  }
}
