package com.asfoundation.wallet.ui.iab;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.google.android.material.snackbar.Snackbar;
import dagger.android.support.DaggerFragment;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;

public class BillingWebViewFragment extends DaggerFragment {

  private static final String LOCAL_PAYMENTS_SCHEMA = "myappcoins.com/t/";
  private static final String GO_PAY_PAYMENTS_SCHEMA = "gojek://gopay/merchanttransfer";
  private static final String URL = "url";
  private static final String CURRENT_URL = "currentUrl";
  private final AtomicReference<ScheduledFuture<?>> timeoutReference;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject BillingAnalytics analytics;
  private ProgressBar webviewProgressBar;
  private String currentUrl;
  private ScheduledExecutorService executorService;
  private AndroidBug5497Workaround androidBug5497Workaround;
  private WebViewActivity webViewActivity;
  private WebView webView;

  public BillingWebViewFragment() {
    this.timeoutReference = new AtomicReference<>();
  }

  public static BillingWebViewFragment newInstance(String url) {
    Bundle args = new Bundle();
    args.putString(URL, url);
    BillingWebViewFragment fragment = new BillingWebViewFragment();
    fragment.setArguments(args);
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof WebViewActivity)) {
      throw new IllegalStateException("WebView fragment must be attached to WebView Activity");
    }
    webViewActivity = (WebViewActivity) context;
    androidBug5497Workaround = new AndroidBug5497Workaround(webViewActivity);
    androidBug5497Workaround.addListener();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    executorService = Executors.newScheduledThreadPool(0);

    if (getArguments() == null || !getArguments().containsKey(URL)) {
      throw new IllegalArgumentException("Provided url is null!");
    }

    if (savedInstanceState == null) {
      currentUrl = getArguments().getString(URL);
    } else {
      currentUrl = savedInstanceState.getString(CURRENT_URL);
    }

    CookieManager.getInstance()
        .setAcceptCookie(true);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.webview_fragment, container, false);

    webView = view.findViewById(R.id.webview);
    webviewProgressBar = view.findViewById(R.id.webview_progress_bar);

    webView.setWebViewClient(new WebViewClient() {

      @Override public boolean shouldOverrideUrlLoading(WebView view, String clickUrl) {
        if (clickUrl.contains(LOCAL_PAYMENTS_SCHEMA)) {
          currentUrl = clickUrl;
          Intent intent = new Intent();
          intent.setData(Uri.parse(clickUrl));
          webViewActivity.setResult(WebViewActivity.SUCCESS, intent);
          webViewActivity.finish();
        } else if (clickUrl.contains(GO_PAY_PAYMENTS_SCHEMA)) {
          launchActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl)));
        } else {
          currentUrl = clickUrl;
          return false;
        }
        return true;
      }

      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
      }

      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (!url.contains("/redirect")) {
          ScheduledFuture<?> timeout = timeoutReference.getAndSet(null);
          if (timeout != null) {
            timeout.cancel(false);
          }
          webviewProgressBar.setVisibility(View.GONE);
        }
      }
    });

    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    webView.loadUrl(currentUrl);

    return view;
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(CURRENT_URL, currentUrl);
  }

  @Override public void onDestroy() {
    executorService.shutdown();

    super.onDestroy();
  }

  @Override public void onDetach() {
    androidBug5497Workaround.removeListener();
    webViewActivity = null;
    webView.setWebViewClient(null);
    super.onDetach();
  }

  private void launchActivity(Intent intent) {
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException exception) {
      exception.printStackTrace();
      if (getView() != null) {
        Snackbar.make(getView(), R.string.unknown_error, Snackbar.LENGTH_SHORT)
            .show();
      }
    }
  }
}
