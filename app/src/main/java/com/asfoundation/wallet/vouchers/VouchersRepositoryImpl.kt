package com.asfoundation.wallet.vouchers

import android.util.Log
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.vouchers.api.VouchersApi
import io.reactivex.Single

class VouchersRepositoryImpl(private val api: VouchersApi,
                             private val mapper: VouchersResponseMapper,
                             private val remoteRepository: RemoteRepository) : VouchersRepository {

  companion object {
    private const val TAG = "VouchersRepository"
  }

  override fun getVoucherApps(): Single<VoucherListModel> {
    return api.getAppsWithAvailableVouchers()
        .map { response -> mapper.mapAppWithVouchers(response) }
        .onErrorReturn { throwable ->
          Log.e(TAG, throwable.message, throwable)
          mapper.mapAppWithVouchersError(throwable)
        }
  }

  override fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList> {
    return api.getVouchersForPackage(packageName)
        .map { response -> mapper.mapVoucherSkuList(response) }
        .onErrorReturn { throwable ->
          Log.e(TAG, throwable.message, throwable)
          mapper.mapVoucherSkuListError(throwable)
        }
  }

  override fun getVoucherTransactionData(transactionHash: String,
                                         walletAddress: String,
                                         signedAddress: String): Single<VoucherTransactionModel> {
    return remoteRepository.getAppCoinsTransactionByHash(transactionHash, walletAddress,
        signedAddress)
        .map { transaction -> mapper.mapVoucherTransactionData(transaction) }
        .onErrorReturn { throwable ->
          Log.e(TAG, throwable.message, throwable)
          mapper.mapVoucherTransactionDataError(throwable)
        }
  }
}