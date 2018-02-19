package com.asf.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.interact.ChangeTokenEnableInteract;
import com.asf.wallet.interact.DeleteTokenInteract;
import com.asf.wallet.interact.FetchAllTokenInfoInteract;

import static com.asf.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TokenChangeCollectionViewModel extends BaseViewModel {
  private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
  private final MutableLiveData<Token[]> tokens = new MutableLiveData<>();

  private final FetchAllTokenInfoInteract fetchAllTokenInfoInteract;
  private final ChangeTokenEnableInteract changeTokenEnableInteract;
  private final DeleteTokenInteract tokenDeleteInteract;

  TokenChangeCollectionViewModel(FetchAllTokenInfoInteract fetchAllTokenInfoInteract,
      ChangeTokenEnableInteract changeTokenEnableInteract,
      DeleteTokenInteract tokenDeleteInteract) {
    this.fetchAllTokenInfoInteract = fetchAllTokenInfoInteract;
    this.changeTokenEnableInteract = changeTokenEnableInteract;
    this.tokenDeleteInteract = tokenDeleteInteract;
  }

  public void prepare() {
    progress.postValue(true);
    fetchTokens();
  }

  public MutableLiveData<Wallet> wallet() {
    return wallet;
  }

  public LiveData<Token[]> tokens() {
    return tokens;
  }

  public void fetchTokens() {
    progress.postValue(true);
    disposable = fetchAllTokenInfoInteract.fetch(wallet.getValue())
        .subscribe(this::onTokens, this::onError, this::onFetchTokensCompletable);
  }

  private void onFetchTokensCompletable() {
    progress.postValue(false);
    Token[] tokens = tokens().getValue();
    if (tokens == null || tokens.length == 0) {
      error.postValue(new ErrorEnvelope(EMPTY_COLLECTION, "tokens not found"));
    }
  }

  private void onTokens(Token[] tokens) {
    this.tokens.setValue(tokens);
    if (tokens != null && tokens.length > 0) {
      progress.postValue(true);
    }
  }

  public void setEnabled(Token token) {
    changeTokenEnableInteract.setEnable(wallet.getValue(), token)
        .subscribe(() -> {
        }, this::onError);
  }

  public void deleteToken(Token token) {
    tokenDeleteInteract.delete(wallet.getValue(), token)
        .subscribe(this::fetchTokens, t -> {

        });
  }
}
