package com.wallet.crypto.trustapp.interact;


import com.wallet.crypto.trustapp.entity.GasSettings;
import com.wallet.crypto.trustapp.repository.GasSettingsRepositoryType;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FetchGasSettingsInteract {
    private final GasSettingsRepositoryType repository;

    public FetchGasSettingsInteract(GasSettingsRepositoryType repository) {
        this.repository = repository;
    }

    public Single<GasSettings> fetch(boolean forTokenTransfer) {
        return repository
                .getGasSettings(forTokenTransfer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<GasSettings> fetchDefault(boolean tokenTransfer) {
        return null;
//        return Single.fromCallable(() -> {
//            BigInteger gasPrice = new BigInteger(C.DEFAULT_GAS_PRICE);
//            BigInteger gasLimit = new BigInteger(C.DEFAULT_GAS_LIMIT);
//            if (tokenTransfer) {
//                gasLimit = new BigInteger(C.DEFAULT_GAS_LIMIT_FOR_TOKENS);
//            }
//            return new GasSettings(gasPrice, gasLimit);
//        });
    }
}
