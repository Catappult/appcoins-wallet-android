package com.asfoundation.wallet.util;

import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.token.Erc20Token;

/**
 * Created by neuro on 12-02-2018.
 */

public final class TokenInfoFactory {

  public static TokenInfo getTokenInfo(Erc20Token erc20Token) {
    return new TokenInfo(erc20Token.getAddress(), erc20Token.getSymbol(), erc20Token.getSymbol(),
        erc20Token.getDecimals(), true, false);
  }
}
