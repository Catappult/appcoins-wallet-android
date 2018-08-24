package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import java.math.BigDecimal;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(Bundle data);

  void showError();

  void close(Bundle bundle);

  void navigateToCreditCardAuthorization();

  void showOnChain(BigDecimal amount);

  void showOffChain(BigDecimal amount);
}
