package com.asfoundation.wallet;

import com.appcoins.wallet.core.network.airdrop.AirdropService;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class AirdropTest {
  public static final String CAPTCHA_ANSWER = "captcha_answer";
  private static final String APPC_TRANSACTION_HASH = "appc_transaction";
  private static final String ETH_TRANSACTION_HASH = "eth_transaction";
  private static final String DESCRIPTION = "description";
  private static final String WALLET_ADDRESS = "wallet_address";
  private static final String CAPTCHA = "captcha";
  private static final int CHAIN_ID = 1;
  private static final String ERROR_MESSAGE = "error";
  @Mock TransactionService transactionService;
  @Mock AirdropService airdropService;
  private Airdrop airdrop;

  @Before public void setUp() {
    when(airdropService.requestCaptcha(WALLET_ADDRESS)).thenReturn(Single.just(CAPTCHA));
    when(transactionService.waitForTransactionToComplete(anyString())).thenReturn(
        Completable.complete());
    airdrop = new Airdrop(transactionService, BehaviorSubject.create(), airdropService);
  }

  @Test public void requestCaptcha() {
    TestObserver<String> test = airdrop.requestCaptcha(WALLET_ADDRESS)
        .test();
    test.assertValue(CAPTCHA);
  }

  @Test public void requestSuccess() {
    when(airdropService.requestAirdrop(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER)).thenReturn(
        Single.just(
            new AirdropService.AirDropResponse(AirdropService.Status.OK, APPC_TRANSACTION_HASH,
                ETH_TRANSACTION_HASH, CHAIN_ID, DESCRIPTION)));
    TestObserver<AirdropData> test = airdrop.getStatus()
        .test();
    airdrop.request(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER);
    test.assertValues(new AirdropData(AirdropData.AirdropStatus.PENDING, null),
        new AirdropData(AirdropData.AirdropStatus.SUCCESS, DESCRIPTION, CHAIN_ID));
  }

  @Test public void requestApiFail() {
    when(airdropService.requestAirdrop(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER)).thenReturn(
        Single.just(new AirdropService.AirDropResponse(AirdropService.Status.FAIL, null, null, -1,
            ERROR_MESSAGE)));
    TestObserver<AirdropData> test = airdrop.getStatus()
        .test();
    airdrop.request(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER);
    test.assertValues(new AirdropData(AirdropData.AirdropStatus.PENDING, null),
        new AirdropData(AirdropData.AirdropStatus.API_ERROR, ERROR_MESSAGE));
  }

  @Test public void requestFail() {
    when(airdropService.requestAirdrop(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER)).thenReturn(
        Single.error(new RuntimeException()));
    TestObserver<AirdropData> test = airdrop.getStatus()
        .test();
    airdrop.request(WALLET_ADDRESS, CHAIN_ID, CAPTCHA_ANSWER);
    test.assertValues(new AirdropData(AirdropData.AirdropStatus.PENDING, null),
        new AirdropData(AirdropData.AirdropStatus.ERROR));
  }

  @Test public void stop() {
    airdrop.stop();
  }
}