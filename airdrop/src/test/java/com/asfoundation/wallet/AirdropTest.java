package com.asfoundation.wallet;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class AirdropTest {
  public static final String APPC_TRANSACTION_HASH = "appc_transaction";
  public static final String ETH_TRANSACTION_HASH = "eth_transaction";
  public static final String DESCRIPTION = "description";
  public static final String WALLET_ADDRESS = "wallet_address";
  public static final String CAPTCHA = "captcha";
  @Mock TransactionService pendingTransaction;
  @Mock AirdropService airdropService;
  private Airdrop airdrop;

  @Before public void setUp() {
    airdrop = new Airdrop(pendingTransaction, BehaviorSubject.create(), airdropService);
    when(airdropService.requestCaptcha(WALLET_ADDRESS)).thenReturn(Single.just(CAPTCHA));
  }

  @Test public void getCaptcha() {
    TestObserver<String> test = airdrop.requestCaptcha(WALLET_ADDRESS)
        .test();
    test.assertValue(CAPTCHA);
  }
}