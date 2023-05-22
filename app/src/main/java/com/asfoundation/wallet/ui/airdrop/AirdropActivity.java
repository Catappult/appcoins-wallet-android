package com.asfoundation.wallet.ui.airdrop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.asf.wallet.R;
import com.appcoins.wallet.core.arch.legacy.BaseActivity;

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

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  protected Toolbar toolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setVisibility(View.VISIBLE);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      toolbar.setTitle(getTitle());
    }
    enableDisplayHomeAsUp();
    return toolbar;
  }


  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onAirdropFinish() {
    finish();
  }
}
