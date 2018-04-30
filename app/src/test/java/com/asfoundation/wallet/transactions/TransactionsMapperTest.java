package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.google.gson.Gson;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class TransactionsMapperTest {

  @Mock DefaultTokenProvider defaultTokenProvider;

  @Before public void before() {
    Mockito.when(defaultTokenProvider.getDefaultToken())
        .thenReturn(Single.just(
            new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "AppCoins", "APPC", 18,
                true, false)));
  }

  @Test public void standardTransactions() {
    RawTransaction[] transactions = getData(JsonResources.STANDARD_JSON).docs;
    TransactionsMapper mapper = new TransactionsMapper(defaultTokenProvider);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1"));
    transactionList.add(
        new Transaction("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98"));
    Assert.assertEquals(transactionList, test.values()
        .get(0));
  }

  @Test public void standardAndAdsTransactions() {
    RawTransaction[] transactions = getData(JsonResources.STANDARD_AND_ADS_JSON).docs;
    TransactionsMapper mapper = new TransactionsMapper(defaultTokenProvider);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a",
            Transaction.TransactionType.ADS, null));
    transactionList.add(
        new Transaction("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1"));
    transactionList.add(
        new Transaction("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98"));
    Assert.assertEquals(transactionList, test.values()
        .get(0));
  }

  @Test public void standardAndIabTransactions() {
    RawTransaction[] transactions = getData(JsonResources.STANDARD_AND_IAB_JSON).docs;
    TransactionsMapper mapper = new TransactionsMapper(defaultTokenProvider);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb",
            "0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9"));
    transactionList.add(
        new Transaction("0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932"));
    transactionList.add(
        new Transaction("0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533"));
    test.assertNoErrors()
        .assertComplete();
    Assert.assertEquals(transactionList, test.values()
        .get(0));
  }

  private ApiClientResponse getData(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, ApiClientResponse.class);
  }

  private final static class ApiClientResponse {
    RawTransaction[] docs;
    int pages;
  }
}