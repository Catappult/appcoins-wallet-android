package com.asfoundation.wallet.ui.iab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.TransactionService;
import com.asfoundation.wallet.billing.purchase.BillingFactory;
import dagger.android.support.DaggerFragment;
import javax.inject.Inject;

public class BillingWebViewFragment extends DaggerFragment {

  private static final String BILLING_SCHEMA = "billing://";

  private static final String URL = "url";
  private static final String CURRENT_URL = "currentUrl";

  @Inject Billing billing;
  @Inject BillingFactory billingFactory;
  @Inject WalletService walletService;
  @Inject TransactionService transactionService;

  private WebView webView;
  private String currentUrl;

  public static BillingWebViewFragment newInstance(String url) {
    Bundle args = new Bundle();
    args.putString(URL, url);
    BillingWebViewFragment fragment = new BillingWebViewFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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

    int visibility = View.GONE;
    if (webView != null) {
      visibility = webView.getVisibility();
    }

    webView = view.findViewById(R.id.webview);

    webView.setVisibility(visibility);

    webView.setWebViewClient(new WebViewClient() {

      @Override public boolean shouldOverrideUrlLoading(WebView view, String clickUrl) {
        currentUrl = clickUrl;

        if (clickUrl.startsWith(BILLING_SCHEMA)) {
          Intent intent = new Intent(getContext(), IabActivity.class);
          intent.setData(Uri.parse(clickUrl));
          getActivity().setResult(WebViewActivity.SUCCESS);
          getActivity().finish();
          getContext().startActivity(intent);

          return true;
        } else {
          return false;
        }
      }

      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
      }

      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (!url.contains("/redirect")) {
          webView.setVisibility(View.VISIBLE);
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
}
