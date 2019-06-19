package com.asfoundation.wallet.ui.balance.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsEntity.TABLE_NAME
import io.reactivex.Observable


@Dao
interface BalanceDetailsDao {

  @Query(
      "select * from $TABLE_NAME where `wallet_address` like :wallet_address limit 1")
  fun getSyncBalance(wallet_address: String): BalanceDetailsEntity?

  @Query(
      "select * from $TABLE_NAME where `wallet_address` like :wallet_address limit 1")
  fun getBalance(wallet_address: String): Observable<BalanceDetailsEntity?>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insert(data: BalanceDetailsEntity): Long

  @Query(
      "UPDATE $TABLE_NAME SET eth_token_amount = :amount, eth_token_conversion = :conversion, fiat_currency = :currency, fiat_symbol = :symbol WHERE wallet_address =:address")
  fun updateEthBalance(address: String, amount: String, conversion: String, currency: String,
                       symbol: String): Int

  @Query(
      "UPDATE $TABLE_NAME SET appc_token_amount = :amount, appc_token_conversion = :conversion, fiat_currency = :currency, fiat_symbol = :symbol WHERE wallet_address =:address")
  fun updateAppcBalance(address: String, amount: String, conversion: String, currency: String,
                        symbol: String): Int

  @Query(
      "UPDATE $TABLE_NAME SET credits_token_amount = :amount, credits_token_conversion = :conversion, fiat_currency = :currency, fiat_symbol = :symbol WHERE wallet_address =:address")
  fun updateCreditsBalance(address: String, amount: String, conversion: String, currency: String,
                           symbol: String): Int

}