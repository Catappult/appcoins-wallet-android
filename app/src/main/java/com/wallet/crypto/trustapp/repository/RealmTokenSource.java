package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.Wallet;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class RealmTokenSource implements LocalTokenSource {

    @Override
    public Completable put(Wallet wallet, TokenInfo tokenInfo) {
        return Completable.fromAction(() -> putInNeed(wallet, tokenInfo));
    }

    @Override
    public Single<TokenInfo[]> fetch(Wallet wallet) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = getRealmInstance(wallet);
                RealmResults<RealmTokenInfo> realmItems = realm.where(RealmTokenInfo.class).findAll();
                int len = realmItems.size();
                TokenInfo[] result = new TokenInfo[len];
                for (int i = 0; i < len; i++) {
                    RealmTokenInfo realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        result[i] = new TokenInfo(
                                realmItem.getAddress(),
                                realmItem.getName(),
                                realmItem.getSymbol(),
                                realmItem.getDecimals());
                    }
                }
                return result;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }

    private Realm getRealmInstance(Wallet wallet) {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(wallet.address + ".realm")
                .schemaVersion(1)
                .build();
        return Realm.getInstance(config);
    }

    private void putInNeed(Wallet wallet, TokenInfo tokenInfo) {
        Realm realm = null;
        try {
            realm = getRealmInstance(wallet);
            RealmTokenInfo realmTokenInfo = realm.where(RealmTokenInfo.class)
                    .equalTo("address", wallet.address).findFirst();
            if (realmTokenInfo == null) {
                realm.executeTransaction(r -> {
                    RealmTokenInfo obj = r.createObject(RealmTokenInfo.class);
                    obj.setAddress(tokenInfo.address);
                    obj.setName(tokenInfo.name);
                    obj.setSymbol(tokenInfo.symbol);
                    obj.setDecimals(tokenInfo.decimals);
                });
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private static class RealmTokenInfo extends RealmObject {
        @PrimaryKey
        private String address;
        private String name;
        private String symbol;
        private int decimals;

        int getDecimals() {
            return decimals;
        }

        void setDecimals(int decimals) {
            this.decimals = decimals;
        }

        String getSymbol() {
            return symbol;
        }

        void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getAddress() {
            return address;
        }

        void setAddress(String address) {
            this.address = address;
        }
    }
}
