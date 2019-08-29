package com.asfoundation.wallet.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asfoundation.wallet.router.TransactionsRouter;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class SettingsActivity extends BaseActivity implements HasFragmentInjector {

  @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    toolbar();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container,
            new SettingsFragment(Schedulers.io(), AndroidSchedulers.mainThread()))
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

  @Override public AndroidInjector<android.app.Fragment> fragmentInjector() {
    return fragmentInjector;
  }
}
