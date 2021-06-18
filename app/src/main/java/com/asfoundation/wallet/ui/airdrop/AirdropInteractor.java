package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.Airdrop;
import com.asfoundation.wallet.AirdropData;
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AirdropInteractor {
  private final Airdrop airdrop;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final AirdropChainIdMapper airdropChainIdMapper;

  public AirdropInteractor(Airdrop airdrop, FindDefaultWalletInteract findDefaultWalletInteract,
      AirdropChainIdMapper airdropChainIdMapper) {
    this.airdrop = airdrop;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.airdropChainIdMapper = airdropChainIdMapper;
  }

  Single<String> requestCaptcha() {
    return findDefaultWalletInteract.find()
        .observeOn(Schedulers.io())
        .flatMap(wallet -> airdrop.requestCaptcha(wallet.address));
  }

  Completable requestAirdrop(String captchaAnswer) {
    return findDefaultWalletInteract.find()
        .flatMap(wallet -> airdropChainIdMapper.getAirdropChainId()
            .doOnSuccess(chainId -> airdrop.request(wallet.address, chainId, captchaAnswer)))
        .ignoreElement();
  }

  public Observable<AirdropData> getStatus() {
    return airdrop.getStatus();
  }

  void terminateStateConsumed() {
    airdrop.resetState();
  }
}
