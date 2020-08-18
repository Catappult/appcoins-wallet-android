package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository;
import com.google.gson.Gson;
import io.reactivex.Single;
import io.reactivex.schedulers.TestScheduler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class TransactionsMapperTest {

  @Mock DefaultTokenProvider defaultTokenProvider;
  @Mock AppCoinsOperationRepository appcoinsOperationsDataSaver;

  private TestScheduler scheduler;

  @Before public void before() {
    Mockito.when(defaultTokenProvider.getDefaultToken())
        .thenReturn(Single.just(
            new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "AppCoins", "APPC", 18)));
    scheduler = new TestScheduler();
  }

  @Test public void dateFormatTest() throws ParseException {
    DateFormat receiveDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    Date date = receiveDateFormat.parse("2019-09-17 11:34:21.563408+0000");
    System.out.println(receiveDateFormat.format(date));
  }

  private ApiClientResponse getData(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, ApiClientResponse.class);
  }

  private final static class ApiClientResponse {
    List<RawTransaction> docs;
  }
}