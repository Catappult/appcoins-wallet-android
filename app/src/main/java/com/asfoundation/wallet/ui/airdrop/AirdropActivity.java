package com.asfoundation.wallet.ui.airdrop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.BaseActivity;

public class AirdropActivity extends BaseActivity {
  public static Intent newIntent(Context context) {
    return new Intent(context, AirdropActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.airdrop_activity_layout);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, AirdropFragment.newInstance())
          .commit();
    }
  }
}
