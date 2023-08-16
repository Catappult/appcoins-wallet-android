package com.appcoins.wallet.core.network.eskills.download;

import com.appcoins.wallet.core.network.eskills.utils.utils.BaseException;

class GeneralDownloadErrorException extends BaseException {
  public GeneralDownloadErrorException(String errorMessage) {
    super(errorMessage);
  }
}
