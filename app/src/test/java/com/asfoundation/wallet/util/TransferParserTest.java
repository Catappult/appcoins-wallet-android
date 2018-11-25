package com.asfoundation.wallet.util;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferParserTest {
  @Test public void parse() {
    TokenRepositoryType tokenRepositoryType = mock(TokenRepositoryType.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    EIPTransactionParser eipTransactionParser =
        new EIPTransactionParser(findDefaultWalletInteract, tokenRepositoryType);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    Token token = new Token(new TokenInfo(contractAddress, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;

    when(tokenRepositoryType.fetchAll(any())).thenReturn(Observable.just(tokens));

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
    TokenRepositoryType tokenRepositoryType = mock(TokenRepositoryType.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    EIPTransactionParser eipTransactionParser =
        new EIPTransactionParser(findDefaultWalletInteract, tokenRepositoryType);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    Token token = new Token(new TokenInfo(contractAddress, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;

    when(tokenRepositoryType.fetchAll(any())).thenReturn(Observable.just(tokens));

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
    test.assertValue(transactionBuilder -> transactionBuilder.shouldSendToken() == true);
    test.assertValue(transactionBuilder -> transactionBuilder.getIabContract()
        .equals("0xb015D9bBabc472BBfC990ED6A0C961a90a482C57"));
  }

  @Test public void parseTransferToken() {
    TokenRepositoryType tokenRepositoryType = mock(TokenRepositoryType.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    EIPTransactionParser eipTransactionParser =
        new EIPTransactionParser(findDefaultWalletInteract, tokenRepositoryType);
    OneStepTransactionParser oneStepTransactionParser = mock(OneStepTransactionParser.class);

    String contractAddress = "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3";
    when(findDefaultWalletInteract.find()).thenReturn(Single.just(new Wallet(contractAddress)));
    Token token = new Token(new TokenInfo(contractAddress, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;

    when(tokenRepositoryType.fetchAll(any())).thenReturn(Observable.just(tokens));

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
    test.assertValue(transactionBuilder -> transactionBuilder.shouldSendToken() == true);
  }

  @Test public void parseEthTransaction() {
    TokenRepositoryType tokenRepositoryType = mock(TokenRepositoryType.class);
    FindDefaultWalletInteract findDefaultWalletInteract = mock(FindDefaultWalletInteract.class);
    EIPTransactionParser eipTransactionParser =
        new EIPTransactionParser(findDefaultWalletInteract, tokenRepositoryType);
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
    test.assertValue(transactionBuilder -> transactionBuilder.shouldSendToken() == false);
  }
}