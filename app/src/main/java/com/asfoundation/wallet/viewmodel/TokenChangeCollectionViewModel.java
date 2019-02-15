package com.asfoundation.wallet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.ChangeTokenEnableInteract;
import com.asfoundation.wallet.interact.DeleteTokenInteract;
import com.asfoundation.wallet.interact.FetchAllTokenInfoInteract;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

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
