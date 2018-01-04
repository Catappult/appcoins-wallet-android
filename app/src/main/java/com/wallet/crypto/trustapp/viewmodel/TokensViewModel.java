package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.interact.FetchTokensInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.trustapp.router.AddTokenRouter;

public class TokensViewModel extends BaseViewModel {
    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    private final MutableLiveData<Token[]> tokens = new MutableLiveData<>();

    private final FindDefaultNetworkInteract findDefaultNetworkInteract;
    private final FetchTokensInteract fetchTokensInteract;
    private final AddTokenRouter addTokenRouter;

    TokensViewModel(
            FindDefaultNetworkInteract findDefaultNetworkInteract,
            FetchTokensInteract fetchTokensInteract,
            AddTokenRouter addTokenRouter) {
        this.findDefaultNetworkInteract = findDefaultNetworkInteract;
        this.fetchTokensInteract = fetchTokensInteract;
        this.addTokenRouter = addTokenRouter;
    }

    public void prepare() {
        progress.postValue(true);
        findDefaultNetwork();
    }

    private void findDefaultNetwork() {
        disposable = findDefaultNetworkInteract
                .find()
                .subscribe(this::onDefaultNetwork, this::onError);
    }

    private void onDefaultNetwork(NetworkInfo networkInfo) {
        defaultNetwork.postValue(networkInfo);
        fetchTokens();
    }

    public LiveData<NetworkInfo> defaultNetwork() {
        return defaultNetwork;
    }

    public MutableLiveData<Wallet> wallet() {
        return wallet;
    }

    public LiveData<Token[]> tokens() {
        return tokens;
    }

    public void fetchTokens() {
        progress.postValue(true);
        if (defaultNetwork.getValue() == null) {
            findDefaultNetwork();
        }
        disposable = fetchTokensInteract
                .fetch(wallet.getValue())
                .subscribe(this::onTokens, this::onError);
    }

    private void onTokens(Token[] tokens) {
        progress.postValue(false);
        this.tokens.postValue(tokens);
    }

    public void showAddToken(Context context) {
        addTokenRouter.open(context);
    }
}
