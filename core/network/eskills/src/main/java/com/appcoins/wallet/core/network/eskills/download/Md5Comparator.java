package com.appcoins.wallet.core.network.eskills.download;

import android.text.TextUtils;

import com.appcoins.wallet.core.network.eskills.utils.utils.AptoideUtils;
import java.io.File;

public class Md5Comparator {

  private String generalFolderPath;

  public Md5Comparator(String generalFolderPath) {
    this.generalFolderPath = generalFolderPath;
  }

  public boolean compareMd5(String md5, String fileName) {
    return TextUtils.equals(
        AptoideUtils.AlgorithmU.computeMd5(new File(generalFolderPath + fileName)), md5);
  }
}
