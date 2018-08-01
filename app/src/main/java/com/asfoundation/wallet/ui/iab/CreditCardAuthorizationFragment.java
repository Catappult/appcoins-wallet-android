package com.asfoundation.wallet.ui.iab;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.braintreepayments.cardform.view.CardForm;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.support.DaggerFragment;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class CreditCardAuthorizationFragment extends DaggerFragment
    implements CreditCardAuthorizationView {
  private static final String PACKAGE_NAME = "packageName";
  private static final String APP_NAME = "appName";
  private static final String APP_DESCRIPTION = "appDescription";
  private static final String FIAT_VALUE = "fiatValue";
  private static final String APPC_VALUE = "appcValue";

  private View progressBar;
  private CardForm cardForm;
  private Button buyButton;
  private Button cancelButton;
  private ImageView productIcon;
  private TextView productName;
  private TextView productDescription;
  private TextView fiatPrice;
  private TextView appcPrice;
  private TextView preAuthorizedCardText;
  private CheckBox rememberCardCheckBox;

  private PublishRelay<Void> backButton;
  private PublishRelay<Void> keyboardBuyRelay;

  public static CreditCardAuthorizationFragment newInstance(String packageName, String appName,
      String appDescription, FiatValue fiatValue, double appcValue) {

    final CreditCardAuthorizationFragment fragment = new CreditCardAuthorizationFragment();
    Bundle appInfo = new Bundle();
    appInfo.putString(PACKAGE_NAME, packageName);
    appInfo.putString(APP_NAME, appName);
    appInfo.putString(APP_DESCRIPTION, appDescription);
    appInfo.putSerializable(FIAT_VALUE, fiatValue);
    appInfo.putDouble(APPC_VALUE, appcValue);
    fragment.setArguments(appInfo);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backButton = PublishRelay.create();
    keyboardBuyRelay = PublishRelay.create();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_credit_card_authorization, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    preAuthorizedCardText =
        view.findViewById(R.id.fragment_credit_card_authorization_pre_authorized_card);
    progressBar = view.findViewById(R.id.fragment_credit_card_authorization_progress_bar);
    productIcon = view.findViewById(R.id.app_icon);
    productName = view.findViewById(R.id.app_name);
    productDescription = view.findViewById(R.id.app_sku_description);
    fiatPrice = view.findViewById(R.id.fiat_price);
    appcPrice = view.findViewById(R.id.appc_price);
    cancelButton = view.findViewById(R.id.cancel_button);
    buyButton = view.findViewById(R.id.buy_button);
    cardForm = view.findViewById(R.id.fragment_braintree_credit_card_form);
    rememberCardCheckBox =
        view.findViewById(R.id.fragment_credit_card_authorization_remember_card_check_box);

    cardForm.setOnCardFormValidListener(valid -> {
      if (valid) {
        buyButton.setVisibility(View.VISIBLE);
      } else {
        buyButton.setVisibility(View.GONE);
      }
    });
    cardForm.setOnCardFormSubmitListener(() -> {

    });

    try {
      populateAppInfo(getArguments(), getActivity().getPackageManager()
          .getApplicationIcon(getArguments().getString(PACKAGE_NAME)));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override public void onDestroyView() {
    progressBar = null;
    productIcon = null;
    productName = null;
    productDescription = null;
    fiatPrice = null;
    appcPrice = null;
    cancelButton = null;
    rememberCardCheckBox = null;
    buyButton = null;
    preAuthorizedCardText = null;
    cardForm.setOnCardFormSubmitListener(null);
    cardForm.setOnCardFormValidListener(null);
    cardForm = null;
    super.onDestroyView();
  }

  private void populateAppInfo(Bundle appInfo, Drawable appIcon) {
    Formatter formatter = new Formatter();
    productIcon.setImageDrawable(appIcon);
    productDescription.setText(appInfo.getString(APP_DESCRIPTION));
    productName.setText(appInfo.getString(APP_NAME));

    String fiatValue = getCurrency(((FiatValue) appInfo.getSerializable(FIAT_VALUE)).getCurrency())
        + formatter.format(Locale.getDefault(), "%(,.2f",
        ((FiatValue) appInfo.getSerializable(FIAT_VALUE)).getAmount())
        .toString();
    formatter = new Formatter();
    String appcValue =
        formatter.format(Locale.getDefault(), "%(,.2f", appInfo.getDouble(APPC_VALUE))
            .toString() + " APPC";

    fiatPrice.setText(fiatValue);
    appcPrice.setText(appcValue);
  }

  public String getCurrency(String currency) {
    switch (currency) {
      case "EUR":
        return "€ ";
      default:
        return "$ ";
    }
  }
}
