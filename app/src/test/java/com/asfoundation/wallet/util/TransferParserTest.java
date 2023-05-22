package com.asfoundation.wallet.util;

import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferParserTest {

  @Test public void parse() {
    DefaultTokenProvider defaultTokenProvider = mock(DefaultTokenProvider.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    EIPTransactionParser eipTransactionParser = new EIPTransactionParser(defaultTokenProvider);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    TokenInfo tokenInfo = new TokenInfo(contractAddress, "AppCoins", "APPC", 18);
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));

    TransferParser transferParser =
        new TransferParser(eipTransactionParser, oneStepTransactionParser);
    TestObserver<TransactionBuilder> test = transferParser.parse("ethereum:"
        + contractAddress
        + "@3"
        + "/transfer?address=0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be&uint256"
        + "=1000000000000000000")
        .test();
    test.assertValue(transactionBuilder -> transactionBuilder.amount()
        .equals(new BigDecimal(1).setScale(18)));
    test.assertValue(transactionBuilder -> transactionBuilder.toAddress()
        .equals("0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be"));
    test.assertValue(transactionBuilder -> transactionBuilder.contractAddress()
        .equals("0xab949343e6c369c6b17c7ae302c1debd4b7b61c3"));
  }

  @Test public void parseWithData() {
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    DefaultTokenProvider defaultTokenProvider = mock(DefaultTokenProvider.class);
    EIPTransactionParser eipTransactionParser = new EIPTransactionParser(defaultTokenProvider);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));

    TokenInfo tokenInfo = new TokenInfo(contractAddress, "AppCoins", "APPC", 18);
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));

    TransferParser transferParser =
        new TransferParser(eipTransactionParser, oneStepTransactionParser);
    TestObserver<TransactionBuilder> test = transferParser.parse("ethereum:"
        + contractAddress
        + "@3"
        + "/buy?address=0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be&uint256"
        + "=1000000000000000000&data=0x636f6d2e63656e61732e70726f64756374&iabContractAddress"
        + "=0xb015D9bBabc472BBfC990ED6A0C961a90a482C57")
        .test();

    test.assertValue(transactionBuilder -> transactionBuilder.amount()
        .equals(new BigDecimal(1).setScale(18)));
    test.assertValue(transactionBuilder -> transactionBuilder.toAddress()
        .equals("0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be"));
    test.assertValue(transactionBuilder -> transactionBuilder.contractAddress()
        .equals("0xab949343e6c369c6b17c7ae302c1debd4b7b61c3"));
    test.assertValue(TransactionBuilder::shouldSendToken);
    test.assertValue(transactionBuilder -> transactionBuilder.getIabContract()
        .equals("0xb015D9bBabc472BBfC990ED6A0C961a90a482C57"));
  }

  @Test public void parseTransferToken() {
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    DefaultTokenProvider defaultTokenProvider = mock(DefaultTokenProvider.class);
    EIPTransactionParser eipTransactionParser = new EIPTransactionParser(defaultTokenProvider);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    TokenInfo tokenInfo = new TokenInfo(contractAddress, "AppCoins", "APPC", 18);
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));

    TransferParser transferParser =
        new TransferParser(eipTransactionParser, oneStepTransactionParser);
    String toAddress = "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be";
    TestObserver<TransactionBuilder> test = transferParser.parse("ethereum:"
        + contractAddress
        + "@3"
        + "/transfer?address="
        + toAddress
        + "&uint256=1000000000000000000")
        .test();

    test.assertValue(transactionBuilder -> transactionBuilder.amount()
        .equals(new BigDecimal(1).setScale(18)));
    test.assertValue(transactionBuilder -> transactionBuilder.toAddress()
        .equals(toAddress));
    test.assertValue(transactionBuilder -> transactionBuilder.contractAddress()
        .equals(contractAddress));
    test.assertValue(TransactionBuilder::shouldSendToken);
  }

  @Test public void parseEthTransaction() {
    DefaultTokenProvider defaultTokenProvider = mock(DefaultTokenProvider.class);
    EIPTransactionParser eipTransactionParser = new EIPTransactionParser(defaultTokenProvider);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String toAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";

    TransferParser transferParser =
        new TransferParser(eipTransactionParser, oneStepTransactionParser);
    TestObserver<TransactionBuilder> test =
        transferParser.parse("ethereum:" + toAddress + "@3?value=1e18")
            .test();

    test.assertValue(transactionBuilder -> transactionBuilder.amount()
        .equals(new BigDecimal(1).setScale(18)));
    test.assertValue(transactionBuilder -> transactionBuilder.toAddress()
        .equals(toAddress));
    test.assertValue(transactionBuilder -> !transactionBuilder.shouldSendToken());
  }
}