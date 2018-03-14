package com.asf.wallet.ui.iab;

import io.reactivex.Observable;

/**
 * Created by trinkes on 13/03/2018.
 */

public interface IabView {
  Observable<String> getBuyClick();

  void finish(String hash);
}
