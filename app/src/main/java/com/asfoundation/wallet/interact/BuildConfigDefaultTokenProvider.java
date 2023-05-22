package com.asfoundation.wallet.interact;

import com.appcoins.wallet.core.utils.properties.MiscProperties;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Created by trinkes on 07/02/2018.
 */
@BoundTo(supertype = DefaultTokenProvider.class) public class BuildConfigDefaultTokenProvider
    implements DefaultTokenProvider {
  private final FindDefaultWalletInteract findDefaultWalletInteract;

  public @Inject BuildConfigDefaultTokenProvider(
      FindDefaultWalletInteract findDefaultWalletInteract) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
  }

  @NotNull @Override public Single<TokenInfo> getDefaultToken() {
    return findDefaultWalletInteract.find()
        .map(wallet -> getDefaultTokenInfo());
  }

  @NotNull private TokenInfo getDefaultTokenInfo() {
    return new TokenInfo(MiscProperties.INSTANCE.getDEFAULT_TOKEN_ADDRESS(),
        MiscProperties.INSTANCE.getDEFAULT_TOKEN_NAME(),
        MiscProperties.INSTANCE.getDEFAULT_TOKEN_SYMBOL()
            .toLowerCase(), MiscProperties.INSTANCE.getDEFAULT_TOKEN_DECIMALS());
  }
}
