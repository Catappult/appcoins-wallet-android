package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.ui.iab.AppCoinsOperation;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.google.gson.Gson;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
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
  @Mock AppcoinsOperationsDataSaver appcoinsOperationsDataSaver;

  private TestScheduler scheduler;

  @Before public void before() {
    Mockito.when(defaultTokenProvider.getDefaultToken())
        .thenReturn(Single.just(
            new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "AppCoins", "APPC", 18,
                true, false)));
    scheduler = new TestScheduler();
  }

  @Test public void standardTransactions() {
    RawTransaction[] transactions = getData(JsonResources.STANDARD_JSON).docs;
    TransactionsMapper mapper =
        new TransactionsMapper(defaultTokenProvider, appcoinsOperationsDataSaver, scheduler);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    scheduler.triggerActions();
    List<Transaction> transactionList = new ArrayList<>();
    List<Operation> operations = new ArrayList<>();
    operations.add(
        new Operation("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", "0.0001155"));

    transactionList.add(
        new Transaction("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1",
            Transaction.TransactionType.STANDARD, null, 1524757754,
            Transaction.TransactionStatus.SUCCESS, "100000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", null, null, operations));

    operations = new ArrayList<>();
    operations.add(
        new Operation("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", "0.000261125"));

    transactionList.add(
        new Transaction("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98",
            Transaction.TransactionType.STANDARD, null, 1524757754,
            Transaction.TransactionStatus.SUCCESS, "75000000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3", null, "APPC", operations));
    Assert.assertEquals(transactionList, test.values()
        .get(0));
  }

  @Test public void standardAndAdsTransactions() {
    Mockito.when(appcoinsOperationsDataSaver.getSync(
        "0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a"))
        .thenReturn(new AppCoinsOperation(
            "0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a",
            "com.packagename.test", "Test App", "/img/path/icon", "test item"));

    RawTransaction[] transactions = getData(JsonResources.STANDARD_AND_ADS_JSON).docs;
    TransactionsMapper mapper =
        new TransactionsMapper(defaultTokenProvider, appcoinsOperationsDataSaver, scheduler);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    scheduler.triggerActions();

    List<Operation> operations = new ArrayList<>();
    operations.add(
        new Operation("0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49",
            "0xf2e45dc350fa2d0a210c691f30cd58394cee1aa3", "0.000050618"));
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a",
            Transaction.TransactionType.ADS, null, 1524759761,
            Transaction.TransactionStatus.SUCCESS, "0",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49",
            "0xf2e45dc350fa2d0a210c691f30cd58394cee1aa3", new TransactionDetails("Test App",
            new TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, "/img/path/icon"), null),
            null, operations));

    operations = new ArrayList<>();
    operations.add(
        new Operation("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", "0.0001155"));
    transactionList.add(
        new Transaction("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1",
            Transaction.TransactionType.STANDARD, null, 1524757754,
            Transaction.TransactionStatus.SUCCESS, "100000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", null, null, operations));

    operations = new ArrayList<>();
    operations.add(
        new Operation("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x8367e6e522e5545466687bce1a508f4a32d14a49", "0.000261125"));
    transactionList.add(
        new Transaction("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98",
            Transaction.TransactionType.STANDARD, null, 1524757754,
            Transaction.TransactionStatus.SUCCESS, "75000000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3", null, "APPC", operations));
    Assert.assertEquals(transactionList, test.values()
        .get(0));
  }

  @Test public void standardAndIabTransactions() {
    Mockito.when(appcoinsOperationsDataSaver.getSync(
        "0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9"))
        .thenReturn(new AppCoinsOperation(
            "0xfddcbb2776d74ed5bc4c831ddb2210c624ecbb7af0864cf1dc8ab6ad5943307a",
            "com.packagename.test2", "Test App 2", "/img/path/icon2", "test item 2"));

    RawTransaction[] transactions = getData(JsonResources.STANDARD_AND_IAB_JSON).docs;
    TransactionsMapper mapper =
        new TransactionsMapper(defaultTokenProvider, appcoinsOperationsDataSaver, scheduler);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    scheduler.triggerActions();

    List<Operation> operations = new ArrayList<>();
    operations.add(
        new Operation("0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b",
            "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3", "0.00010921"));
    operations.add(
        new Operation("0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b",
            "0xb040e69bd4b1025ef6da958cac7464730933db71", "0.00022242"));
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9",
            Transaction.TransactionType.IAB,
            "0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb", 1524491228,
            Transaction.TransactionStatus.SUCCESS, "1000000000000000000",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b",
            "0xb040e69bd4b1025ef6da958cac7464730933db71", new TransactionDetails("Test App 2",
            new TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, "/img/path/icon2"),
            "test item 2"), "APPC", operations));

    operations = new ArrayList<>();
    operations.add(
        new Operation("0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b", "0.0000000000084"));
    transactionList.add(
        new Transaction("0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932",
            Transaction.TransactionType.STANDARD, null, 1524237519,
            Transaction.TransactionStatus.SUCCESS, "100000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b", null, null, operations));

    operations = new ArrayList<>();
    operations.add(
        new Operation("0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0x33a8c36a4812947e6f5d7cd37778ff1ad699839b", "0.00000000002089"));
    transactionList.add(
        new Transaction("0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533",
            Transaction.TransactionType.STANDARD, null, 1524237519,
            Transaction.TransactionStatus.SUCCESS, "75000000000000000000",
            "0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be",
            "0xab949343e6c369c6b17c7ae302c1debd4b7b61c3", null, "APPC", operations));
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