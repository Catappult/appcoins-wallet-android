package com.appcoins.wallet.core.utils.jvm_common;

public abstract class C {

  public static final int IMPORT_REQUEST_CODE = 1001;
  public static final int EXPORT_REQUEST_CODE = 1002;
  public static final int SHARE_REQUEST_CODE = 1003;

  public static final String ETHEREUM_NETWORK_NAME = "Ethereum";
  public static final String ROPSTEN_NETWORK_NAME = "Ropsten (Test)";

  public static final String ETHEREUM_TIKER = "ethereum";

  public static final String USD_SYMBOL = "$";
  public static final String ETH_SYMBOL = "ETH";
  public static final String ETC_SYMBOL = "ETC";

  public static final String GWEI_UNIT = "Gwei";

  public static final String EXTRA_ADDRESS = "ADDRESS";
  public static final String EXTRA_CONTRACT_ADDRESS = "CONTRACT_ADDRESS";
  public static final String EXTRA_DECIMALS = "DECIMALS";
  public static final String EXTRA_SYMBOL = "SYMBOL";
  public static final String EXTRA_SENDING_TOKENS = "SENDING_TOKENS";
  public static final String EXTRA_ADDRESS_DATA = "address_data";
  public static final String EXTRA_TO_ADDRESS = "TO_ADDRESS";
  public static final String EXTRA_AMOUNT = "AMOUNT";
  public static final String EXTRA_GAS_PRICE = "GAS_PRICE";
  public static final String EXTRA_GAS_LIMIT = "GAS_LIMIT";
  public static final String EXTRA_TRANSACTION_BUILDER = "TRANSACTION_BUILDER";
  public static final String EXTRA_GAS_SETTINGS = "GAS_SETTINGS";

  public static final String COINBASE_WIDGET_CODE = "88d6141a-ff60-536c-841c-8f830adaacfd";
  public static final String SHAPESHIFT_KEY =
      "c4097b033e02163da6114fbbc1bf15155e759ddfd8352c88c55e7fef162e901a800e7eaecf836062a0c075b2b881054e0b9aa2324be7bc3694578493faf59af4";
  public static final String CHANGELLY_REF_ID = "968d4f0f0bf9";
  public static final String DONATION_ADDRESS = "0x9f8284ce2cf0c8ce10685f537b1fff418104a317";

  public static final long GAS_LIMIT_MIN = 21000L;
  public static final long GAS_LIMIT_MAX = 300000L;
  public static final long GAS_PRICE_MIN = 1000000000L;
  public static final long NETWORK_FEE_MAX = 90000000000000000L;
  public static final int ETHER_DECIMALS = 18;

  public interface ErrorCode {

    int UNKNOWN = 1;
    int CANT_GET_STORE_PASSWORD = 2;
    int ALREADY_ADDED = 3;
    int EMPTY_COLLECTION = 4;
  }

  public interface Key {
    String WALLET = "wallet";
    String TRANSACTION = "transaction";
    String GLOBAL_BALANCE_CURRENCY = "global_balance_currency";
    String SHOULD_SHOW_SECURITY_WARNING = "should_show_security_warning";
  }
}
