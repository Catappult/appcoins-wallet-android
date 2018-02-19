package com.asf.wallet.util;

import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.TokenRepositoryType;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.Arrays;
import org.kethereum.erc681.ERC681;
import org.kethereum.erc681.ERC681ExtensionFunKt;
import org.kethereum.erc681.ERC681ParserKt;

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
          .flatMap(payment -> findDefaultWalletInteract.find()
              .flatMap(wallet -> tokenRepository.fetchAll(wallet.address)
                  .flatMapIterable(Arrays::asList)
                  .filter(token -> token.tokenInfo.address.equalsIgnoreCase(payment.getAddress()))
                  .toList())
              .flatMap(tokens -> {
                TransactionBuilder transactionBuilder;
                if (tokens.isEmpty()) {
                  if (!isTokenTransfer(payment)) {
                    transactionBuilder = new TransactionBuilder("ETH");
                    transactionBuilder.toAddress(payment.getAddress());
                    transactionBuilder.amount(getEtherTransferAmount(payment));
                  } else {
                    return Single.error(new RuntimeException("token not added"));
                  }
                } else {
                  Token token = tokens.get(0);
                  transactionBuilder = new TransactionBuilder(token.tokenInfo);
                  if (payment.getChainId() != null) {
                    transactionBuilder.setChainId(payment.getChainId());
                  }
                  transactionBuilder.toAddress(getReceiverAddress(payment));
                  transactionBuilder.amount(
                      getTokenTransferAmount(payment, token.tokenInfo.decimals));
                }
                return Single.just(transactionBuilder);
              }));
    }
    return Single.error(new RuntimeException("is not an supported URI"));
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
        .equals("transfer")) {
      address = payment.getFunctionParams()
          .get("address");
    } else {
      address = payment.getAddress();
    }
    return address;
  }
}
