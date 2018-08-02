package com.asfoundation.wallet.billing.view.card;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.AdyenBilling;
import com.asfoundation.wallet.billing.BillingSignerImpl;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.asfoundation.wallet.view.MyApp;
import com.asfoundation.wallet.view.fragment.BaseFragment;
import com.asfoundation.wallet.view.rx.RxAlertDialog;
import com.asfoundation.wallet.ws.BDSTransactionService;
import com.braintreepayments.cardform.view.CardForm;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxrelay.PublishRelay;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class CreditCardFragment extends BaseFragment implements CreditCardAuthorizationView {

  private static final String TAG = CreditCardFragment.class.getSimpleName();

  private View progressBar;
  private RxAlertDialog networkErrorDialog;
  private ClickHandler clickHandler;
  private View overlay;
  private CardForm cardForm;
  private Button buyButton;
  private Button cancelButton;
  private ImageView productIcon;
  private TextView productName;
  private TextView productDescription;
  private TextView productPrice;
  private TextView preAuthorizedCardText;
  private CheckBox rememberCardCheckBox;

  private PublishRelay<Void> backButton;
  private PublishRelay<Void> keyboardBuyRelay;
  private PaymentMethod paymentMethod;
  private boolean cvcOnly;
  private String publicKey;
  private String generationTime;

  public static CreditCardFragment newInstance() {
    return new CreditCardFragment();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_credit_card_authorization, container, false);
  }

  @Override public void showLoading() {
    progressBar.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    progressBar.setVisibility(View.GONE);
  }

  @Override public Observable<Void> errorDismisses() {
    return networkErrorDialog.dismisses()
        .map(dialogInterface -> null);
  }

  @Override public Observable<PaymentDetails> creditCardDetailsEvent() {
    return Observable.merge(keyboardBuyRelay, RxView.clicks(buyButton))
        .map(__ -> getPaymentDetails(publicKey, generationTime));
  }

  @Override public void showNetworkError() {
    if (!networkErrorDialog.isShowing()) {
      networkErrorDialog.show();
    }
  }

  @Override public Observable<Void> cancelEvent() {
    return Observable.merge(RxView.clicks(cancelButton), RxView.clicks(overlay), backButton);
  }

  @Override public void showCvcView(Amount amount, PaymentMethod paymentMethod) {
    cvcOnly = true;
    this.paymentMethod = paymentMethod;
    showProductPrice(amount);
    preAuthorizedCardText.setVisibility(View.VISIBLE);
    preAuthorizedCardText.setText(paymentMethod.getName());
    rememberCardCheckBox.setVisibility(View.GONE);
    cardForm.cardRequired(false)
        .expirationRequired(false)
        .cvvRequired(true)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.buy))
        .setup(getActivity());
  }

  @Override
  public void showCreditCardView(PaymentMethod paymentMethod, Amount amount, boolean cvcRequired,
      boolean allowSave, String publicKey, String generationTime) {
    this.paymentMethod = paymentMethod;
    this.publicKey = publicKey;
    this.generationTime = generationTime;
    cvcOnly = false;
    preAuthorizedCardText.setVisibility(View.GONE);
    rememberCardCheckBox.setVisibility(View.VISIBLE);
    showProductPrice(amount);
    cardForm.cardRequired(true)
        .expirationRequired(true)
        .cvvRequired(cvcRequired)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.buy))
        .setup(getActivity());
  }

  @Override public void showSuccess() {
    Toast.makeText(getContext(), "Purchase Successfull", Toast.LENGTH_LONG)
        .show();
  }

  private void showProductPrice(Amount amount) {
    //this.productPrice.setText(
    //    AmountUtil.format(amount, true, StringUtils.getLocale(getActivity())));
  }

  private PaymentDetails getPaymentDetails(String publicKey, String generationTime) {

    if (cvcOnly) {
      final PaymentDetails paymentDetails = new PaymentDetails(paymentMethod.getInputDetails());
      paymentDetails.fill("cardDetails.cvc", cardForm.getCvv());
      return paymentDetails;
    }

    final CreditCardPaymentDetails creditCardPaymentDetails =
        new CreditCardPaymentDetails(paymentMethod.getInputDetails());
    try {
      final JSONObject sensitiveData = new JSONObject();

      sensitiveData.put("holderName", "Checkout Shopper Placeholder");
      sensitiveData.put("number", cardForm.getCardNumber());
      sensitiveData.put("expiryMonth", cardForm.getExpirationMonth());
      sensitiveData.put("expiryYear", cardForm.getExpirationYear());
      sensitiveData.put("generationtime", generationTime);
      sensitiveData.put("cvc", cardForm.getCvv());
      creditCardPaymentDetails.fillCardToken(
          new ClientSideEncrypter(publicKey).encrypt(sensitiveData.toString()));
    } catch (JSONException e) {
      Log.e(TAG, "JSON Exception occurred while generating token.", e);
    } catch (EncrypterException e) {
      Log.e(TAG, "EncrypterException occurred while generating token.", e);
    }
    creditCardPaymentDetails.fillStoreDetails(rememberCardCheckBox.isChecked());
    return creditCardPaymentDetails;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    backButton = PublishRelay.create();
    keyboardBuyRelay = PublishRelay.create();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    preAuthorizedCardText =
        view.findViewById(R.id.fragment_credit_card_authorization_pre_authorized_card);
    progressBar = view.findViewById(R.id.fragment_credit_card_authorization_progress_bar);
    overlay = view.findViewById(R.id.fragment_credit_card_authorization_overlay);
    //productIcon = (ImageView) view.findViewById(R.id.include_payment_product_icon);
    //productName = (TextView) view.findViewById(R.id.include_payment_product_name);
    //productDescription = (TextView) view.findViewById(R.id.include_payment_product_description);
    //productPrice = (TextView) view.findViewById(R.id.include_payment_product_price);
    cancelButton = view.findViewById(R.id.include_payment_buttons_cancel_button);
    buyButton = view.findViewById(R.id.include_payment_buttons_buy_button);
    rememberCardCheckBox =
        view.findViewById(R.id.fragment_credit_card_authorization_remember_card_check_box);
    buyButton.setVisibility(View.GONE);
    cardForm = view.findViewById(R.id.fragment_braintree_credit_card_form);

    networkErrorDialog =
        new RxAlertDialog.Builder(getContext()).setMessage(R.string.connection_error)
            .setPositiveButton(R.string.iab_button_ok)
            .build();

    clickHandler = new ClickHandler() {
      @Override public boolean handle() {
        backButton.call(null);
        return true;
      }
    };
    registerClickHandler(clickHandler);

    cardForm.setOnCardFormValidListener(valid -> {
      if (valid) {
        buyButton.setVisibility(View.VISIBLE);
      } else {
        buyButton.setVisibility(View.GONE);
      }
    });
    cardForm.setOnCardFormSubmitListener(() -> {
      keyboardBuyRelay.call(null);
    });

    Adyen adyen = ((MyApp) getContext().getApplicationContext()).getAdyen();

    attachPresenter(
        new CreditCardAuthorizationPresenter(adyen, this, AndroidSchedulers.mainThread(),
            new AdyenBilling(new BDSTransactionService(), new BillingSignerImpl(), adyen),
            new CreditCardFragmentNavigator(getActivity().getSupportFragmentManager())));
  }

  @Override public void onDestroyView() {
    unregisterClickHandler(clickHandler);
    progressBar = null;
    networkErrorDialog.dismiss();
    networkErrorDialog = null;
    overlay = null;
    productIcon = null;
    productName = null;
    productDescription = null;
    productPrice = null;
    cancelButton = null;
    rememberCardCheckBox = null;
    buyButton = null;
    preAuthorizedCardText = null;
    cardForm.setOnCardFormSubmitListener(null);
    cardForm.setOnCardFormValidListener(null);
    cardForm = null;
    super.onDestroyView();
  }
}
