package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.asf.wallet.R;
import com.asfoundation.wallet.App;
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics;
import com.appcoins.wallet.core.utils.common.KeyboardUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class BaseActivity extends AppCompatActivity implements ActivityResultSharer {

  private List<ActivityResultListener> activityResultListeners;
  private PageViewAnalytics pageViewAnalytics;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    activityResultListeners = new ArrayList<>();
    pageViewAnalytics = new PageViewAnalytics(((App) getApplication()).analyticsManager());
    super.onCreate(savedInstanceState);
    Window window = getWindow();

    // clear FLAG_TRANSLUCENT_STATUS flag:
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
  }

  protected void sendPageViewEvent() {
    pageViewAnalytics.sendPageViewEvent(getClass().getSimpleName());
  }

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

  protected void setTitle(String title) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(title);
    }
  }

  protected void setCollapsingTitle(String title) {
    CollapsingToolbarLayout collapsing = findViewById(R.id.toolbar_layout);
    if (collapsing != null) {
      collapsing.setTitle(title);
    }
  }

  protected void enableDisplayHomeAsUp() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  protected void disableDisplayHomeAsUp() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(false);
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      KeyboardUtils.hideKeyboard(getWindow().getDecorView()
          .getRootView());
      finish();
    }
    return true;
  }

  @Override public void addOnActivityListener(@NotNull ActivityResultListener listener) {
    activityResultListeners.add(listener);
  }

  @Override public void remove(@NotNull ActivityResultListener listener) {
    activityResultListeners.remove(listener);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    for (ActivityResultListener listener : activityResultListeners) {
      listener.onActivityResult(requestCode, resultCode, data);
    }
  }
}
