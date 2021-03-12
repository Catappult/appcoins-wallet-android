package com.asfoundation.wallet.vouchers

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.util.isNoNetworkException
import com.asfoundation.wallet.promotions.Voucher
import com.asfoundation.wallet.promotions.VoucherListModel
import com.asfoundation.wallet.promotions.voucher.Price
import com.asfoundation.wallet.promotions.voucher.VoucherSkuItem
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.util.Error
import com.asfoundation.wallet.vouchers.api.VoucherAppListResponse
import com.asfoundation.wallet.vouchers.api.VoucherSkuListResponse

class VouchersResponseMapper {

  fun mapAppWithVouchers(response: VoucherAppListResponse): VoucherListModel {
    val list = response.items
        .map { responseVoucher ->
          Voucher(responseVoucher.name, responseVoucher.title, responseVoucher.graphic,
              responseVoucher.icon, responseVoucher.appc)
        }
    return VoucherListModel(list)
  }

  fun mapAppWithVouchersError(throwable: Throwable): VoucherListModel {
    return VoucherListModel(Error(true, throwable.isNoNetworkException()))
  }

  fun mapVoucherSkuList(response: VoucherSkuListResponse): VoucherSkuModelList {
    val list = response.items.map { skuResponse ->
      VoucherSkuItem(skuResponse.sku, skuResponse.title,
          Price(skuResponse.price.value.toDouble(), skuResponse.price.currency,
              skuResponse.price.symbol,
              skuResponse.price.appc.toDouble()))
    }
    return VoucherSkuModelList(list)
  }

  fun mapVoucherSkuListError(throwable: Throwable): VoucherSkuModelList {
    return VoucherSkuModelList(Error(true, throwable.isNoNetworkException()))
  }

  fun mapVoucherTransactionData(transaction: Transaction): VoucherTransactionModel {
    return transaction.metadata?.voucher?.let { voucher ->
      VoucherTransactionModel(voucher.code, voucher.redeem)
    } ?: VoucherTransactionModel(Error(hasError = true, isNoNetwork = false))
  }

  fun mapVoucherTransactionDataError(throwable: Throwable): VoucherTransactionModel {
    return VoucherTransactionModel(
        Error(hasError = true, isNoNetwork = throwable.isNoNetworkException()))
  }
}