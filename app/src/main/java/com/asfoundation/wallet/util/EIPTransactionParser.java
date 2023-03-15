package com.asfoundation.wallet.util;

import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.appcoins.wallet.core.utils.common.UnknownTokenException;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.bouncycastle.util.encoders.Hex;
import org.kethereum.erc681.ERC681;

public class EIPTransactionParser {
  private final DefaultTokenProvider defaultTokenProvider;

  public @Inject EIPTransactionParser(DefaultTokenProvider defaultTokenProvider) {
    this.defaultTokenProvider = defaultTokenProvider;
  }

  public Single<TransactionBuilder> buildTransaction(ERC681 erc681) {
    switch (getTransactionType(erc681)) {
      case APPC:
        return buildAppcTransaction(erc681);
      case TOKEN:
        return buildTokenTransaction(erc681);
      case ETH:
      default:
        return buildEthTransaction(erc681);
    }
  }

  private Single<TransactionBuilder> buildEthTransaction(ERC681 payment) {
    TransactionBuilder transactionBuilder = new TransactionBuilder("ETH");
    transactionBuilder.toAddress(payment.getAddress());
    transactionBuilder.amount(getEtherTransferAmount(payment));
    transactionBuilder.setType("inapp"); //Transfer only so it doesn't matter
    return Single.just(transactionBuilder);
  }

  private Single<TransactionBuilder> buildTokenTransaction(ERC681 payment) {
    return defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> {
          if (tokenInfo.address.equalsIgnoreCase(payment.getAddress())) {
            return Single.just(tokenInfo);
          } else {
            return Single.error(new UnknownTokenException());
          }
        })
        .map(tokenInfo -> new TransactionBuilder(tokenInfo.symbol, tokenInfo.address,
            payment.getChainId(), getReceiverAddress(payment),
            getTokenTransferAmount(payment, tokenInfo.decimals),
            tokenInfo.decimals).shouldSendToken(true));
  }

  private Single<TransactionBuilder> buildAppcTransaction(ERC681 payment) {
    return defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> {
          if (tokenInfo.address.equalsIgnoreCase(payment.getAddress())) {
            return Single.just(tokenInfo);
          } else {
            return Single.error(new UnknownTokenException());
          }
        })
        .map(tokenInfo -> {
          TransactionData data = retrieveData(payment);
          return new TransactionBuilder(tokenInfo.symbol, getIabContractAddress(payment),
              payment.getChainId(), getReceiverAddress(payment),
              getTokenTransferAmount(payment, tokenInfo.decimals), data.getSkuId(),
              tokenInfo.decimals, getIabContract(payment), data.getType(), data.getOrigin(),
              data.getDomain(), data.getPayload(), null, data.getOrderReference(), null, null,
              data.getPeriod(), data.getTrialPeriod()).shouldSendToken(true);
        });
  }

  private String getIabContract(ERC681 payment) {
    return payment.getFunctionParams()
        .get("iabContractAddress");
  }

  private String getIabContractAddress(ERC681 payment) {
    return payment.getAddress();
  }

  private TransactionType getTransactionType(ERC681 payment) {
    if (payment.getFunction() != null && payment.getFunction()
        .equalsIgnoreCase("buy")) {
      return TransactionType.APPC;
    } else if (payment.getFunction() != null && payment.getFunction()
        .equalsIgnoreCase("transfer")) {
      return TransactionType.TOKEN;
    } else {
      return TransactionType.ETH;
    }
  }

  private TransactionData retrieveData(ERC681 payment) {
    String data = new String(Hex.decode(payment.getFunctionParams()
        .get("data")
        .substring(2)
        .getBytes(StandardCharsets.UTF_8)));

    try {
      return new Gson().fromJson(data, TransactionData.class);
    } catch (JsonSyntaxException e) {
      return new TransactionData(data);
    }
  }

  private BigDecimal getEtherTransferAmount(ERC681 payment) {
    return convertToMainMetric(new BigDecimal(payment.getValue()), 18);
  }

  private BigDecimal getTokenTransferAmount(ERC681 payment, int decimals) {
    if (payment.getFunctionParams()
        .containsKey("uint256")) {
      return convertToMainMetric(new BigDecimal(payment.getFunctionParams()
          .get("uint256")), decimals);
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal convertToMainMetric(BigDecimal value, int decimals) {
    try {
      StringBuilder divider = new StringBuilder(18);
      divider.append("1");
      for (int i = 0; i < decimals; i++) {
        divider.append("0");
      }
      return value.divide(new BigDecimal(divider.toString()), decimals, RoundingMode.DOWN);
    } catch (NumberFormatException ex) {
      return BigDecimal.ZERO;
    }
  }

  private String getReceiverAddress(ERC681 payment) {
    String address;
    if (payment.getFunction()
        .equals("transfer") || payment.getFunction()
        .equals("buy")) {
      address = payment.getFunctionParams()
          .get("address");
    } else {
      address = payment.getAddress();
    }
    return address;
  }

  private enum TransactionType {
    APPC, TOKEN, ETH
  }
}
