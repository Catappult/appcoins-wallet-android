package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.appcoins.wallet.billing.repository.entity.Purchase;
import io.reactivex.Observable;
import java.io.IOException;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface ExpressCheckoutBuyView {

  void setup(FiatValue convertToFiatResponseBody);

  void showError();

  Observable<Object> getCancelClick();

  void close(Bundle bundle);

  Observable<Object> errorDismisses();

  void hideLoading();

  void showLoading();

  Observable<Boolean> setupUiCompleted();

  void showProcessingLoadingDialog();

  void finish(Purchase purchase) throws IOException;
}
