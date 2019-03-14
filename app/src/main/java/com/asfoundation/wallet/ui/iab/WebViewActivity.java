package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.navigator.UriNavigator;
import dagger.android.AndroidInjection;

public class WebViewActivity extends AppCompatActivity {

  public static final int SUCCESS = 1;
  public static final int FAIL = 0;

  private static final String URL = "url";

  private static TransactionBuilder transactionBuilder;
  private static UriNavigator navigator;

  public static Intent newIntent(Activity activity, String url, TransactionBuilder transaction,
      UriNavigator uriNavigator) {
    Intent intent = new Intent(activity, WebViewActivity.class);
    intent.putExtra(URL, url);
    transactionBuilder = transaction;
    navigator = uriNavigator;
    return intent;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_activity);

    if (savedInstanceState == null) {
      String url = getIntent().getStringExtra(URL);
      BillingWebViewFragment billingWebViewFragment =
          BillingWebViewFragment.newInstance(url, transactionBuilder, navigator);

      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, billingWebViewFragment)
          .commit();
    }
  }
}
