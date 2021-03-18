package com.asfoundation.wallet.vouchers

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.util.Error
import io.reactivex.Single

class VouchersInteractor(private val vouchersRepository: VouchersRepository,
                         private val walletService: WalletService) {

  fun getVoucherData(transactionHash: String?): Single<VoucherTransactionModel> {
    if (transactionHash.isNullOrEmpty()) {
      return Single.just(VoucherTransactionModel(Error(hasError = true, isNoNetwork = false)))
    }
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          vouchersRepository.getVoucherTransactionData(transactionHash, walletAddressModel.address,
              walletAddressModel.signedAddress)
        }
        .onErrorReturn { VoucherTransactionModel(Error(hasError = true, isNoNetwork = false)) }
  }
}