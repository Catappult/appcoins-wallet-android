package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.google.gson.Gson;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class TransactionsMapperTest {

  String iabJson = "{\"docs\":[{\"operations\":[{\"_id\":\"5adde367a1fa921d63765789\","
      + "\"transactionId\":\"0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9-0"
      + "\",\"contract\":{\"verified\":false,\"_id\":\"5add0576a1fa921d632eb098\","
      + "\"address\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\","
      + "\"totalSupply\":\"1000000000000000000000000\",\"decimals\":18,\"symbol\":\"APPC\","
      + "\"name\":\"AppCoins\"},\"value\":\"850000000000000000\","
      + "\"to\":\"0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be\","
      + "\"from\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"type\":\"token_transfer\"},"
      + "{\"_id\":\"5adde367a1fa921d6376579a\","
      + "\"transactionId\":\"0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9-1"
      + "\",\"contract\":{\"verified\":false,\"_id\":\"5add0576a1fa921d632eb098\","
      + "\"address\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\","
      + "\"totalSupply\":\"1000000000000000000000000\",\"decimals\":18,\"symbol\":\"APPC\","
      + "\"name\":\"AppCoins\"},\"value\":\"100000000000000000\","
      + "\"to\":\"0xc41b4160b63d1f9488937f7b66640d2babdbf8ad\","
      + "\"from\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"type\":\"token_transfer\"},"
      + "{\"_id\":\"5adde367a1fa921d637657a2\","
      + "\"transactionId\":\"0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9-2"
      + "\",\"contract\":{\"verified\":false,\"_id\":\"5add0576a1fa921d632eb098\","
      + "\"address\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\","
      + "\"totalSupply\":\"1000000000000000000000000\",\"decimals\":18,\"symbol\":\"APPC\","
      + "\"name\":\"AppCoins\"},\"value\":\"50000000000000000\","
      + "\"to\":\"0x0965b2a3e664690315ad20b9e5b0336c19cf172e\","
      + "\"from\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"type\":\"token_transfer\"}],"
      + "\"contract\":null,"
      + "\"_id\":\"0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9\","
      + "\"blockNumber\":3093683,\"timeStamp\":\"1524491228\",\"nonce\":1,"
      + "\"from\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\","
      + "\"to\":\"0xb040e69bd4b1025ef6da958cac7464730933db71\",\"value\":\"0\","
      + "\"gas\":\"120000\",\"gasPrice\":\"2500000000\",\"gasUsed\":\"88968\","
      +
      "\"input\":\"0xdc9564d50000000000000000000000000000000000000000000000000de0b6b3a764000000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000ab949343e6c369c6b17c7ae302c1debd4b7b61c30000000000000000000000002c30194bd2e7b6b8ff1467c5af1650f53cd231be000000000000000000000000c41b4160b63d1f9488937f7b66640d2babdbf8ad0000000000000000000000000965b2a3e664690315ad20b9e5b0336c19cf172e00000000000000000000000000000000000000000000000000000000000000036761730000000000000000000000000000000000000000000000000000000000\",\"error\":\"\",\"id\":\"0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9\"},{\"operations\":[],\"contract\":null,\"_id\":\"0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb\",\"blockNumber\":3093680,\"timeStamp\":\"1524491183\",\"nonce\":0,\"from\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"to\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\",\"value\":\"0\",\"gas\":\"120000\",\"gasPrice\":\"2500000000\",\"gasUsed\":\"43684\",\"input\":\"0x095ea7b3000000000000000000000000b040e69bd4b1025ef6da958cac7464730933db710000000000000000000000000000000000000000000000000de0b6b3a7640000\",\"error\":\"\",\"id\":\"0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb\"},{\"operations\":[],\"contract\":null,\"_id\":\"0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932\",\"blockNumber\":3074231,\"timeStamp\":\"1524237519\",\"nonce\":1898,\"from\":\"0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be\",\"to\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"value\":\"100000000000000000\",\"gas\":\"250000\",\"gasPrice\":\"400\",\"gasUsed\":\"21000\",\"input\":\"0x\",\"error\":\"\",\"id\":\"0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932\"},{\"operations\":[{\"_id\":\"5add0c94a1fa921d632f470e\",\"transactionId\":\"0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533-0\",\"contract\":{\"verified\":false,\"_id\":\"5add0576a1fa921d632eb098\",\"address\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\",\"totalSupply\":\"1000000000000000000000000\",\"decimals\":18,\"symbol\":\"APPC\",\"name\":\"AppCoins\"},\"value\":\"75000000000000000000\",\"to\":\"0x33a8c36a4812947e6f5d7cd37778ff1ad699839b\",\"from\":\"0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be\",\"type\":\"token_transfer\"}],\"contract\":null,\"_id\":\"0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533\",\"blockNumber\":3074231,\"timeStamp\":\"1524237519\",\"nonce\":1899,\"from\":\"0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be\",\"to\":\"0xab949343e6c369c6b17c7ae302c1debd4b7b61c3\",\"value\":\"0\",\"gas\":\"250000\",\"gasPrice\":\"400\",\"gasUsed\":\"52225\",\"input\":\"0xa9059cbb00000000000000000000000033a8c36a4812947e6f5d7cd37778ff1ad699839b00000000000000000000000000000000000000000000000410d586a20a4c0000\",\"error\":\"\",\"id\":\"0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533\"}],\"total\":4,\"limit\":20,\"page\":1,\"pages\":1}";
  @Mock DefaultTokenProvider defaultTokenProvider;

  @Before public void before() {
    Mockito.when(defaultTokenProvider.getDefaultToken())
        .thenReturn(Single.just(
            new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "AppCoins", "APPC", 18,
                true, false)));
  }

  @Test public void getNormalAndIabTransactions() {
    RawTransaction[] transactions = getData().docs;
    TransactionsMapper mapper = new TransactionsMapper(defaultTokenProvider);
    TestObserver<List<Transaction>> test = mapper.map(transactions)
        .test();
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(
        new Transaction("0x04efa141853e05a749b5e9dcdf4e474db24955bc411f7adca314dace3037c533"));
    transactionList.add(
        new Transaction("0x7d15f9c11a2f718ede84facca02080f6c9cf8a78da3c545347c1979235299932"));
    transactionList.add(
        new Transaction("0x8506e0e07e4fbcd89684689257dd5f5649474f5cb3d1f0c703460a31bac110bb",
            "0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9"));
    test.assertNoErrors()
        .assertComplete()
        .assertValues(transactionList);
  }

  private ApiClientResponse getData() {
    Gson gson = new Gson();
    return gson.fromJson(iabJson, ApiClientResponse.class);
  }

  private final static class ApiClientResponse {
    RawTransaction[] docs;
    int pages;
  }
}