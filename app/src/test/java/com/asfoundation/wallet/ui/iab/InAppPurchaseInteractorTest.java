package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class InAppPurchaseInteractorTest {

  @Mock InAppPurchaseService inAppPurchaseService;
  @Mock FindDefaultWalletInteract defaultWalletInteract;
  @Mock FetchGasSettingsInteract gasSettingsInteract;
  @Mock TransferParser parser;
  private InAppPurchaseInteractor inAppPurchaseInteractor;

  @Before public void setUp() throws Exception {
    inAppPurchaseInteractor =
        new InAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
            gasSettingsInteract, BigDecimal.ZERO, parser);
  }

  @Test public void getTopUpChannelSuggestionValues() {
    List<BigDecimal> topUpChannelSuggestionValues =
        inAppPurchaseInteractor.getTopUpChannelSuggestionValues(new BigDecimal("7.2"));
    List<BigDecimal> list = new ArrayList<>();
    list.add(new BigDecimal("7.2"));
    list.add(new BigDecimal("10.0"));
    list.add(new BigDecimal("15.0"));
    list.add(new BigDecimal("25.0"));
    list.add(new BigDecimal("35.0"));
    Assert.assertEquals(list, topUpChannelSuggestionValues);
  }
}