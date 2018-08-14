package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;

public interface CreditCardNavigator {

  void popView(Bundle bundle);

  void popViewWithError();
}
