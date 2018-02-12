package com.wallet.crypto.trustapp.util;

import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.TransactionBuilder;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
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
                if (tokens.isEmpty()) {
                  return Single.error(new RuntimeException("token not added"));
                } else {
                  Token token = tokens.get(0);
                  TransactionBuilder transactionBuilder = new TransactionBuilder(token.tokenInfo);
                  transactionBuilder.toAddress(getReceiverAddress(payment));
                  transactionBuilder.amount(
                      getTokenTransferAmount(payment, token.tokenInfo.decimals));
                  return Single.just(transactionBuilder);
                }
              }));
    }
    return Single.error(new RuntimeException("token not added"));
  }

  private BigDecimal getTokenTransferAmount(ERC681 payment, int decimals) {
    if (payment.getFunctionParams()
        .containsKey("uint256")) {
      try {
        StringBuilder divider = new StringBuilder(18);
        divider.append("1");
        for (int i = 0; i < decimals; i++) {
          divider.append("0");
        }
        return new BigDecimal((payment.getFunctionParams()
            .get("uint256"))).divide(new BigDecimal(divider.toString()));
      } catch (NumberFormatException ex) {
        return BigDecimal.ZERO;
      }
    }
    return BigDecimal.ZERO;
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
