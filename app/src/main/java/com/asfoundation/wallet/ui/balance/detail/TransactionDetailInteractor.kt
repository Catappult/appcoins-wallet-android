package com.asfoundation.wallet.ui.balance.detail

import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.vouchers.VouchersRepository
import io.reactivex.Single

class TransactionDetailInteractor(private val vouchersRepository: VouchersRepository) {

  fun getVoucherTransactionModel(transactionHash: String, walletAddress: String,
                                 signedAddress: String): Single<VoucherTransactionModel> {
    return vouchersRepository.getVoucherTransactionData(transactionHash, walletAddress,
        signedAddress)
  }
}