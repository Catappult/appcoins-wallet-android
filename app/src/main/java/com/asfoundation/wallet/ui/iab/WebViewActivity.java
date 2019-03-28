package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.asf.wallet.R;
import com.asfoundation.wallet.navigator.UriNavigator;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;

public class WebViewActivity extends AppCompatActivity {

  public static final int SUCCESS = 1;
  public static final int FAIL = 0;

  private static final String URL = "url";
  private static final String DOMAIN = "domain";
  private static final String SKUID = "skuid";
  private static final String AMOUNT = "amount";
  private static final String TYPE = "type";

  public static Intent newIntent(Activity activity, String url, String domain, String skuId,
      BigDecimal amount, String type) {
    Intent intent = new Intent(activity, WebViewActivity.class);
    intent.putExtra(URL, url);
    intent.putExtra(DOMAIN, domain);
    intent.putExtra(SKUID, skuId);
    intent.putExtra(AMOUNT, amount);
    intent.putExtra(TYPE, type);
    return intent;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_activity);

    if (savedInstanceState == null) {
      String url = getIntent().getStringExtra(URL);
      String domain = getIntent().getStringExtra(DOMAIN);
      String skuId = getIntent().getStringExtra(SKUID);
      BigDecimal amount = (BigDecimal) getIntent().getSerializableExtra(AMOUNT);
      String type = getIntent().getStringExtra(TYPE);
      BillingWebViewFragment billingWebViewFragment =
          BillingWebViewFragment.newInstance(url, domain, skuId, amount, type);

      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, billingWebViewFragment)
          .commit();
    }
  }
}
