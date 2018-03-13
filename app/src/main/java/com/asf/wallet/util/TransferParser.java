package com.asf.wallet.util;

import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.TokenRepositoryType;
import io.reactivex.Single;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.kethereum.erc681.ERC681;
import org.kethereum.erc681.ERC681ExtensionFunKt;
import org.kethereum.erc681.ERC681ParserKt;
import org.spongycastle.util.encoders.Hex;

public class TransferParser {
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final TokenRepositoryType tokenRepository;

  public TransferParser(FindDefaultWalletInteract findDefaultWalletInteract,
      TokenRepositoryType tokenRepository) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.tokenRepository = tokenRepository;
  }

  public Single<TransactionBuilder> parse(String uri) {
    if (ERC681ExtensionFunKt.isEthereumURLString(uri)) {
      return Single.just(ERC681ParserKt.parseERC681(uri))
          .flatMap(erc681 -> {
            switch (getTransactionType(erc681)) {
              case APPC:
                return buildAppcTransaction(erc681);
              case TOKEN:
                return buildTokenTransaction(erc681);
              case ETH:
              default:
                return buildEthTransaction(erc681);
            }
          });

    }
    return Single.error(new RuntimeException("is not an supported URI"));
  }

  private Single<TransactionBuilder> buildEthTransaction(ERC681 payment) {
    return null;
  }

  private Single<TransactionBuilder> buildTokenTransaction(ERC681 payment) {
    return null;
  }

  private Single<TransactionBuilder> buildAppcTransaction(ERC681 payment) {
    return findDefaultWalletInteract.find()
        .flatMap(wallet -> tokenRepository.fetchAll(wallet.address)
            .flatMapIterable(Arrays::asList)
            .filter(token -> token.tokenInfo.address.equalsIgnoreCase(payment.getAddress()))
            .toList())
        .flatMap(tokens -> {
          if (tokens.isEmpty()) {
            return Single.error(new RuntimeException("token not added"));
          } else {
            return Single.just(tokens.get(0));
          }
        })
        .map(token -> new TransactionBuilder(token.tokenInfo.symbol, getIabContractAddress(payment),
            TransactionBuilder.TransactionType.APPC, payment.getChainId(),
            getReceiverAddress(payment), getTokenTransferAmount(payment, token.tokenInfo.decimals),
            getSkuId(payment), token.tokenInfo.decimals));
  }

  private String getIabContractAddress(ERC681 payment) {
    return payment.getAddress();
  }

  private TransactionBuilder.TransactionType getTransactionType(ERC681 payment) {
    if (payment.getFunction()
        .equalsIgnoreCase("buy")) {
      return TransactionBuilder.TransactionType.APPC;
    } else if (payment.getFunction()
        .equalsIgnoreCase("transfer")) {
      return TransactionBuilder.TransactionType.TOKEN;
    } else {
      return TransactionBuilder.TransactionType.ETH;
    }
  }

  private String getSkuId(ERC681 payment) throws UnsupportedEncodingException {
    return new String(Hex.decode(payment.getFunctionParams()
        .get("data")
        .substring(2)
        .getBytes("UTF-8")));
  }

  private BigDecimal getEtherTransferAmount(ERC681 payment) {
    return convertToMainMetric(new BigDecimal(payment.getValue()), 18);
  }

  private boolean isTokenTransfer(ERC681 erc681) {
    return erc681.getFunction() != null && erc681.getFunction()
        .equals("transfer");
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
      return value.divide(new BigDecimal(divider.toString()));
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
}
