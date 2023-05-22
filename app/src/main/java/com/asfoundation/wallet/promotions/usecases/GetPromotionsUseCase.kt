package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.Voucher
import com.asfoundation.wallet.promotions.model.VoucherListModel
import com.appcoins.wallet.feature.walletInfo.data.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetPromotionsUseCase @Inject constructor(
    private val getCurrentWallet: com.appcoins.wallet.feature.walletInfo.data.usecases.GetCurrentWalletUseCase,
    private val observeLevels: ObserveLevelsUseCase,
    private val promotionsMapper: PromotionsMapper,
    private val promotionsRepository: PromotionsRepository,
    private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase,
    private val checkAndCancelVipPollingUseCase: CheckAndCancelVipPollingUseCase,
) {

  operator fun invoke(): Observable<PromotionsModel> {
    return getCurrentPromoCodeUseCase()
      .flatMapObservable { promoCode ->
        getCurrentWallet()
          .flatMapObservable {
            Observable.zip(
              observeLevels(),
              promotionsRepository.getUserStats(it.address, promoCode.code),
              checkAndCancelVipPollingUseCase(it).toObservable()
            ) { levels: Levels, userStatsResponse: UserStats, vipReferralResponse: VipReferralResponse ->
              promotionsMapper.mapToPromotionsModel(
                userStats = userStatsResponse,
                levels = levels,
                wallet = it,
                vouchersListModel = getMockedVouchers(),
                vipReferralResponse = vipReferralResponse
              )
            }
          }
      }
  }

  //TODO Temporary place for mocked vouchers
  private fun getMockedVouchers(): VoucherListModel {
    return VoucherListModel(
      listOf(
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = true
        ),
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = false
        ),
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = true
        ),
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = false
        ),
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = true
        ),
        Voucher(
          packageName = "com.appcoins.trivialdrivesample.test",
          title = "Trivial Drive Sample",
          icon = "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
          hasAppcoins = true
        )
      )
    )
  }
}