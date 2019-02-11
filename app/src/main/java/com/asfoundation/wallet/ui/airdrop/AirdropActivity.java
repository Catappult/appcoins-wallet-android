package com.asfoundation.wallet.ui.airdrop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.BaseActivity;

public class AirdropActivity extends BaseActivity implements AirdropFragment.AirdropBack {
  public static Intent newIntent(Context context) {
    return new Intent(context, AirdropActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_airdrop);
    toolbar();

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, AirdropFragment.newInstance())
          .commit();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onAirdropFinish() {
    finish();
  }
}
