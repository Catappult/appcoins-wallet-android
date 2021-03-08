package com.asfoundation.wallet.vouchers

import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import io.reactivex.Single

interface VouchersRepository {

  fun getAppsWithVouchers(): Single<VoucherListModel>

  fun getVoucherSkuList(packageName: String): Single<VoucherSkuModelList>

  fun getVoucherTransactionData(transactionHash: String, walletAddress: String,
                                signedAddress: String): Single<VoucherTransactionModel>
}
