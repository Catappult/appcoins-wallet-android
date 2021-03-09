package com.asfoundation.wallet.vouchers

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.vouchers.api.VouchersApi
import io.reactivex.Single

class VouchersRepositoryImpl(private val api: VouchersApi,
                             private val mapper: VouchersResponseMapper,
                             private val remoteRepository: RemoteRepository) : VouchersRepository {

  override fun getAppsWithVouchers(): Single<VoucherListModel> {
    return api.getAppsWithAvailableVouchers()
        .map { response -> mapper.mapAppWithVouchers(response) }
        .onErrorReturn { throwable -> mapper.mapAppWithVouchersError(throwable) }
  }

  override fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList> {
    return api.getVouchersForPackage(packageName)
        .map { response -> mapper.mapVoucherSkuList(response) }
        .onErrorReturn { throwable -> mapper.mapVoucherSkuListError(throwable) }
  }

  override fun getVoucherTransactionData(transactionHash: String,
                                         walletAddress: String,
                                         signedAddress: String): Single<VoucherTransactionModel> {
    return remoteRepository.getAppCoinsTransactionByHash(transactionHash, walletAddress,
        signedAddress)
        .map { transaction -> mapper.mapVoucherTransactionData(transaction) }
        .onErrorReturn { throwable -> mapper.mapVoucherTransactionDataError(throwable) }
  }
}