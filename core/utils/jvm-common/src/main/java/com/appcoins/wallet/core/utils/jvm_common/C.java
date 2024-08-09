package com.appcoins.wallet.core.utils.jvm_common;

public abstract class C {

  public static final String ETHEREUM_NETWORK_NAME = "Ethereum";
  public static final String ROPSTEN_NETWORK_NAME = "Ropsten (Test)";

  public static final String ETH_SYMBOL = "ETH";

  public static final String GWEI_UNIT = "Gwei";

  public static final String EXTRA_GAS_PRICE = "GAS_PRICE";
  public static final String EXTRA_GAS_LIMIT = "GAS_LIMIT";
  public static final String EXTRA_TRANSACTION_BUILDER = "TRANSACTION_BUILDER";
  public static final String EXTRA_GAS_SETTINGS = "GAS_SETTINGS";

  public static final long GAS_LIMIT_MIN = 21000L;
  public static final long GAS_LIMIT_MAX = 300000L;
  public static final long GAS_PRICE_MIN = 1000000000L;
  public static final long NETWORK_FEE_MAX = 90000000000000000L;
  public static final int ETHER_DECIMALS = 18;

  public interface ErrorCode {

    int UNKNOWN = 1;
    int ALREADY_ADDED = 3;
  }

  public interface Key {
    String WALLET = "wallet";
    String TRANSACTION = "transaction";
    String GLOBAL_BALANCE_CURRENCY = "global_balance_currency";
  }
}
