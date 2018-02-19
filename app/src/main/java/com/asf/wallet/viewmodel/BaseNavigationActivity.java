package com.asf.wallet.viewmodel;

import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asf.wallet.ui.BaseActivity;

public class BaseNavigationActivity extends BaseActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener {

  private BottomNavigationView navigation;

  protected void initBottomNavigation() {
    navigation = findViewById(R.id.bottom_navigation);
    navigation.setOnNavigationItemSelectedListener(this);
  }

  protected void setBottomMenu(@MenuRes int menuRes) {
    navigation.getMenu()
        .clear();
    navigation.inflateMenu(menuRes);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    return false;
  }
}
