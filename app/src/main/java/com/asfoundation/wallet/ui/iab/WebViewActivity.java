package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.asf.wallet.R;
import dagger.android.AndroidInjection;

public class WebViewActivity extends AppCompatActivity {

  public static final int SUCCESS = 1;
  public static final int FAIL = 0;

  private static final String URL = "url";

  public static Intent newIntent(Activity activity, String url) {
    Intent intent = new Intent(activity, WebViewActivity.class);
    intent.putExtra(URL, url);
    return intent;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_activity);

    if (savedInstanceState == null) {
      String url = getIntent().getStringExtra(URL);
      BillingWebViewFragment billingWebViewFragment = BillingWebViewFragment.newInstance(url);

      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, billingWebViewFragment)
          .commit();
    }
  }
}
