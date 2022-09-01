package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.VipReferralResponse
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.Voucher
import com.asfoundation.wallet.promotions.model.VoucherListModel
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetPromotionsUseCase @Inject constructor(
    private val getCurrentWallet: GetCurrentWalletUseCase,
    private val observeLevels: ObserveLevelsUseCase,
    private val promotionsMapper: PromotionsMapper,
    private val promotionsRepository: PromotionsRepository,
    private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  operator fun invoke(): Observable<PromotionsModel> {
    return getCurrentPromoCodeUseCase()
        .flatMapObservable { promoCode ->
          getCurrentWallet()
              .flatMapObservable {
                Observable.zip(observeLevels(),
                    promotionsRepository.getUserStats(it.address, promoCode.code),
                    promotionsRepository.getVipReferral(it.address),
                    { levels: Levels, userStatsResponse: UserStats, vipReferralResponse: VipReferralResponse ->
                      promotionsMapper.mapToPromotionsModel(userStatsResponse, levels, it,
                          getMockedVouchers(), vipReferralResponse)
                    })
              }
        }
  }

  //TODO Temporary place for mocked vouchers
  private fun getMockedVouchers(): VoucherListModel {
    return VoucherListModel(listOf(
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            true)))
  }
}