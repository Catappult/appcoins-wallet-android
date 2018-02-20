package com.asf.wallet.util;

import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.TokenInfo;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.TokenRepositoryType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferParserTest {
  @Test public void parse() throws Exception {
    TokenRepositoryType tokenRepositoryType = mock(TokenRepositoryType.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);

    String contractAddress = "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    Token token = new Token(new TokenInfo(contractAddress, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;

    when(tokenRepositoryType.fetchAll(any())).thenReturn(Observable.just(tokens));

    TransferParser transferParser =
        new TransferParser(findDefaultWalletInteract, tokenRepositoryType);
    TestObserver<TransactionBuilder> test = transferParser.parse("ethereum:"
        + contractAddress
        + "@3"
        + "/transfer?address=0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be&uint256"
        + "=1000000000000000000")
        .test();
    test.assertValue(transactionBuilder -> transactionBuilder.amount()
        .equals(new BigDecimal(1)));
    test.assertValue(transactionBuilder -> transactionBuilder.toAddress()
        .equals("0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be"));
    test.assertValue(transactionBuilder -> transactionBuilder.contractAddress()
        .equals("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3"));
  }
}