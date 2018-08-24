package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionContract;
import com.asfoundation.wallet.entity.TransactionOperation;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.entity.RealmTransaction;
import com.asfoundation.wallet.repository.entity.RealmTransactionContract;
import com.asfoundation.wallet.repository.entity.RealmTransactionOperation;
import com.asfoundation.wallet.service.RealmManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class TransactionsRealmCache implements TransactionLocalSource {

  private final RealmManager realmManager;

  public TransactionsRealmCache(RealmManager realmManager) {
    this.realmManager = realmManager;
  }

  @Override
  public Single<RawTransaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet) {
    return Single.fromCallable(() -> {
      Realm instance = null;
      try {
        instance = realmManager.getRealmInstance(networkInfo, wallet);
        RealmResults<RealmTransaction> list = instance.where(RealmTransaction.class)
            .sort("nonce", Sort.DESCENDING)
            .findAll();
        return convert(list);
      } finally {
        if (instance != null) {
          instance.close();
        }
      }
    });
  }

  @Override public Completable putTransactions(NetworkInfo networkInfo, Wallet wallet,
      RawTransaction[] transactions) {
    return Completable.fromAction(() -> {
      Realm instance = null;
      try {
        instance = realmManager.getRealmInstance(networkInfo, wallet);
        instance.beginTransaction();
        for (RawTransaction transaction : transactions) {
          RealmTransaction item = instance.where(RealmTransaction.class)
              .equalTo("hash", transaction.hash)
              .findFirst();
          if (item == null) {
            item = instance.createObject(RealmTransaction.class, transaction.hash);
          }
          fill(instance, item, transaction);
        }
        instance.commitTransaction();
      } catch (Exception ex) {
        if (instance != null) {
          instance.cancelTransaction();
        }
      } finally {
        if (instance != null) {
          instance.close();
        }
      }
    })
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<RawTransaction> findLast(NetworkInfo networkInfo, Wallet wallet) {
    return Single.fromCallable(() -> {
      Realm realm = null;
      try {
        realm = realmManager.getRealmInstance(networkInfo, wallet);
        return convert(realm.where(RealmTransaction.class)
            .sort("timeStamp", Sort.DESCENDING)
            .findAll()
            .first());
      } finally {
        if (realm != null) {
          realm.close();
        }
      }
    })
        .observeOn(Schedulers.io());
  }

  private void fill(Realm realm, RealmTransaction item, RawTransaction transaction) {
    item.setError(transaction.error);
    item.setBlockNumber(transaction.blockNumber);
    item.setTimeStamp(transaction.timeStamp);
    item.setNonce(transaction.nonce);
    item.setFrom(transaction.from);
    item.setTo(transaction.to);
    item.setValue(transaction.value);
    item.setGas(transaction.gas);
    item.setGasPrice(transaction.gasPrice);
    item.setInput(transaction.input);
    item.setGasUsed(transaction.gasUsed);

    for (TransactionOperation operation : transaction.operations) {
      if (!isAddedAlready(operation.transactionId, item)) {
        RealmTransactionOperation realmOperation =
            realm.createObject(RealmTransactionOperation.class);
        realmOperation.setTransactionId(operation.transactionId);
        realmOperation.setViewType(operation.viewType);
        realmOperation.setFrom(operation.from);
        realmOperation.setTo(operation.to);
        realmOperation.setValue(operation.value);

        RealmTransactionContract realmContract = realm.createObject(RealmTransactionContract.class);
        realmContract.setAddress(operation.contract.address);
        realmContract.setName(operation.contract.name);
        realmContract.setTotalSupply(operation.contract.totalSupply);
        realmContract.setDecimals(operation.contract.decimals);
        realmContract.setSymbol(operation.contract.symbol);

        realmOperation.setContract(realmContract);
        item.getOperations()
            .add(realmOperation);
      }
    }
  }

  private boolean isAddedAlready(String hash, RealmTransaction item) {
    for (RealmTransactionOperation operation : item.getOperations()) {
      if (operation.getTransactionId()
          .equalsIgnoreCase(hash)) {
        return true;
      }
    }
    return false;
  }

  private RawTransaction[] convert(RealmResults<RealmTransaction> items) {
    int len = items.size();
    RawTransaction[] result = new RawTransaction[len];
    for (int i = 0; i < len; i++) {
      result[i] = convert(items.get(i));
    }
    return result;
  }

  private RawTransaction convert(RealmTransaction rawItem) {
    int len = rawItem.getOperations()
        .size();
    TransactionOperation[] operations = new TransactionOperation[len];
    for (int i = 0; i < len; i++) {
      RealmTransactionOperation rawOperation = rawItem.getOperations()
          .get(i);
      if (rawOperation == null) {
        continue;
      }
      TransactionOperation operation = new TransactionOperation();
      operation.transactionId = rawOperation.getTransactionId();
      operation.viewType = rawOperation.getViewType();
      operation.from = rawOperation.getFrom();
      operation.to = rawOperation.getTo();
      operation.value = rawOperation.getValue();
      operation.contract = new TransactionContract();
      operation.contract.address = rawOperation.getContract()
          .getAddress();
      operation.contract.name = rawOperation.getContract()
          .getName();
      operation.contract.totalSupply = rawOperation.getContract()
          .getTotalSupply();
      operation.contract.decimals = rawOperation.getContract()
          .getDecimals();
      operation.contract.symbol = rawOperation.getContract()
          .getSymbol();
      operations[i] = operation;
    }
    return new RawTransaction(rawItem.getHash(), rawItem.getError(), rawItem.getBlockNumber(),
        rawItem.getTimeStamp(), rawItem.getNonce(), rawItem.getFrom(), rawItem.getTo(),
        rawItem.getValue(), rawItem.getGas(), rawItem.getGasPrice(), rawItem.getInput(),
        rawItem.getGasUsed(), operations);
  }
}
