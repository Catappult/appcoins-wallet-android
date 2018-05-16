package com.asfoundation.wallet.ui.iab.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import java.util.List;

@Dao public interface InAppPurchaseDataDao {
  @Query("select * from InAppPurchaseData") Flowable<List<InAppPurchaseData>> getAllAsFlowable();

  @Query("select * from InAppPurchaseData") List<InAppPurchaseData> getAll();

  @Query("select * from InAppPurchaseData where transaction_id like :key limit 1")
  Flowable<InAppPurchaseData> getAsFlowable(String key);

  @Query("select * from InAppPurchaseData where transaction_id like :key limit 1")
  InAppPurchaseData get(String key);

  @Insert void insert(InAppPurchaseData data);

  @Delete void delete(InAppPurchaseData data);
}
