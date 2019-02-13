package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FetchTokensInteract;
import com.asfoundation.wallet.router.AddTokenRouter;
import com.asfoundation.wallet.router.ChangeTokenCollectionRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import java.math.BigDecimal;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TokensViewModel extends BaseViewModel {
  private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
  private final MutableLiveData<Token[]> tokens = new MutableLiveData<>();
  private final MutableLiveData<BigDecimal> total = new MutableLiveData<>();

  private final FetchTokensInteract fetchTokensInteract;
  private final AddTokenRouter addTokenRouter;
  private final SendRouter sendRouter;
  private final TransactionsRouter transactionsRouter;
  private final ChangeTokenCollectionRouter changeTokenCollectionRouter;

  TokensViewModel(FetchTokensInteract fetchTokensInteract, AddTokenRouter addTokenRouter,
      SendRouter sendRouter, TransactionsRouter transactionsRouter,
      ChangeTokenCollectionRouter changeTokenCollectionRouter) {
    this.fetchTokensInteract = fetchTokensInteract;
    this.addTokenRouter = addTokenRouter;
    this.sendRouter = sendRouter;
    this.transactionsRouter = transactionsRouter;
    this.changeTokenCollectionRouter = changeTokenCollectionRouter;
  }

  public MutableLiveData<Wallet> wallet() {
    return wallet;
  }

  public LiveData<Token[]> tokens() {
    return tokens;
  }

  public LiveData<BigDecimal> total() {
    return total;
  }

  public void fetchTokens() {
    progress.postValue(true);
    fetchTokensInteract.fetch(wallet.getValue())
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
      showTotalBalance(tokens);
    }
  }

  private void showTotalBalance(Token[] tokens) {
    BigDecimal total = new BigDecimal("0");
    for (Token token : tokens) {
      if (token.balance != null
          && token.ticker != null
          && !TextUtils.isEmpty(token.ticker.price)
          && token.balance.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal decimalDivisor = new BigDecimal(Math.pow(10, token.tokenInfo.decimals));
        BigDecimal ethBalance =
            token.tokenInfo.decimals > 0 ? token.balance.divide(decimalDivisor) : token.balance;
        total = total.add(ethBalance.multiply(new BigDecimal(token.ticker.price)));
      }
    }
    total = total.setScale(2, BigDecimal.ROUND_HALF_UP)
        .stripTrailingZeros();
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      total = null;
    }
    this.total.postValue(total);
  }

  public void showAddToken(Context context) {
    addTokenRouter.open(context);
  }

  public void showSendToken(Context context, String address, String symbol, int decimals) {
    sendRouter.open(context, new TokenInfo(address, "", symbol, decimals, true, false));
  }

  public void showTransactions(Context context) {
    transactionsRouter.open(context, true);
  }

  public void showEditTokens(Context context) {
    changeTokenCollectionRouter.open(context, wallet.getValue());
  }
}