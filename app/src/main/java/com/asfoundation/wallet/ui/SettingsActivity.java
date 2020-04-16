package com.asfoundation.wallet.ui;

import android.os.Bundle;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asfoundation.wallet.router.TransactionsRouter;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import javax.inject.Inject;

public class SettingsActivity extends BaseActivity implements HasAndroidInjector {

  @Inject DispatchingAndroidInjector<Object> androidInjector;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    toolbar();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, new SettingsFragment())
        .commit();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      new TransactionsRouter().open(this, true);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }
}
