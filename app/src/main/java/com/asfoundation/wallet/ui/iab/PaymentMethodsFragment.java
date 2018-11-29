package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.asf.wallet.R;
import dagger.android.support.DaggerFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class PaymentMethodsFragment extends DaggerFragment implements PaymentMethodsView {

  private static final String TAG = PaymentMethodsFragment.class.getSimpleName();

  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  PaymentMethodsPresenter presenter;

  public static Fragment newInstance() {
    Bundle args = new Bundle();
    Fragment fragment = new PaymentMethodsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    presenter = new PaymentMethodsPresenter(this, AndroidSchedulers.mainThread(), Schedulers.io(),
        new CompositeDisposable(), inAppPurchaseInteractor);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.payment_methods_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    presenter.present(savedInstanceState);
  }

  @Override public void onDestroyView() {
    presenter.stop();
    super.onDestroyView();
  }

  @Override public void showPaymentMethods(@NotNull List<PaymentMethod> paymentMethods) {
    Log.d(TAG, "showPaymentMethods() called with: paymentMethods = [$paymentMethods]");
  }

  @Override public void showError() {
    // TODO: 29-11-2018 neuro
  }
}
