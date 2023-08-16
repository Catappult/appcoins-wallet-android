package com.appcoins.wallet.core.network.eskills.download;

import com.appcoins.wallet.core.network.eskills.utils.utils.BaseException;

public class InvalidAppException extends BaseException {

  public InvalidAppException(String detailMessage) {
    super(detailMessage);
  }
}
