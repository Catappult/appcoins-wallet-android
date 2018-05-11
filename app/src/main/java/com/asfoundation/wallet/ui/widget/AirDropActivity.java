package com.asfoundation.wallet.ui.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.BaseActivity;
import com.asfoundation.wallet.widget.SystemView;
import dagger.android.AndroidInjection;

public class AirDropActivity extends BaseActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    //AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_airdrop);

    toolbar();

  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    finish();
  }

  @Override protected void onResume() {
    super.onResume();
  }
}
