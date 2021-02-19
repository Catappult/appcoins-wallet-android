package com.asfoundation.wallet.vouchers

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.asfoundation.wallet.promotions.Voucher
import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.util.Error
import com.asfoundation.wallet.vouchers.api.VoucherAppListResponse

class VouchersResponseMapper {

  fun mapAppWithVouchers(response: VoucherAppListResponse): VoucherListModel {
    val list = response.items
        .map { responseVoucher ->
          Voucher(responseVoucher.name, responseVoucher.title, responseVoucher.icon,
              responseVoucher.appc)
        }
    return VoucherListModel(list)
  }

  fun mapAppWithVouchersError(throwable: Throwable): VoucherListModel {
    return VoucherListModel(Error(true, throwable.isNoNetworkException()))
  }
}