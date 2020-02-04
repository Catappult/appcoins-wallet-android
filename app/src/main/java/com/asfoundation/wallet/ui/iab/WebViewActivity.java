package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.asf.wallet.R;
import dagger.android.AndroidInjection;

import static com.asfoundation.wallet.util.LogInterceptor.TEMPORARY_TAG;

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
    lockCurrentPosition();
    if (savedInstanceState == null) {
      String url = getIntent().getStringExtra(URL);
      BillingWebViewFragment billingWebViewFragment = BillingWebViewFragment.newInstance(url);
      Log.d(TEMPORARY_TAG, "WebViewActivity.onCreate(): Navigate to BillingWebViewFragment");
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, billingWebViewFragment)
          .commit();
    }
  }

  private void lockCurrentPosition() {
    //setRequestedOrientation requires translucent and floating to be false to work in API 26
    int orientation = getWindowManager().getDefaultDisplay()
        .getRotation();
    switch (orientation) {
      case Surface.ROTATION_0:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      case Surface.ROTATION_90:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        break;
      case Surface.ROTATION_180:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        break;
      case Surface.ROTATION_270:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        break;
      default:
        Log.w("WebView", "Invalid orientation value: " + orientation);
        break;
    }
  }
}
