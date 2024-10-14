package com.appcoins.wallet.feature.walletInfo.data.wallet.repository;

import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService;
import org.jetbrains.annotations.NotNull;

public class SignDataStandardNormalizer implements AccountWalletService.ContentNormalizer {
  @NotNull @Override public String normalize(@NotNull String content) {
    return "\\x19Ethereum Signed Message:\n" + content.length() + content;
  }
}
