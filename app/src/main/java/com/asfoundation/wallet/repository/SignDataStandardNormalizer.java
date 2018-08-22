package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.service.AccountWalletService;
import org.jetbrains.annotations.NotNull;

public class SignDataStandardNormalizer implements AccountWalletService.ContentNormalizer {
  @NotNull @Override public String normalize(@NotNull String content) {
    return "\\x19Ethereum Signed Message:\n" + content.length() + content;
  }
}
