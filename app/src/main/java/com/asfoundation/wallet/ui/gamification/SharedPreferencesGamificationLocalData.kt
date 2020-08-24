package com.asfoundation.wallet.ui.gamification

import android.content.SharedPreferences
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.GamificationLocalData
import com.appcoins.wallet.gamification.repository.PromotionDao
import com.appcoins.wallet.gamification.repository.entity.*
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.REFERRAL_ID
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesGamificationLocalData(private val preferences: SharedPreferences,
                                             private val promotionDao: PromotionDao) :
    GamificationLocalData {

  companion object {
    private const val SHOWN_LEVEL = "shown_level"
    private const val SCREEN = "screen_"
    private const val GAMIFICATION_LEVEL = "gamification_level"
  }

  override fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
    return Single.fromCallable { preferences.getInt(getKey(wallet, screen), -1) }
  }

  override fun saveShownLevel(wallet: String, level: Int, screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putInt(getKey(wallet, screen), level)
          .apply()
    }
  }

  override fun setGamificationLevel(gamificationLevel: Int): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putInt(GAMIFICATION_LEVEL, gamificationLevel)
          .apply()
    }
  }


  private fun getKey(wallet: String, screen: String): String {
    return if (screen == GamificationScreen.MY_LEVEL.toString()) {
      SHOWN_LEVEL + wallet
    } else {
      SHOWN_LEVEL + wallet + SCREEN + screen
    }
  }

  override fun getPromotions(): Single<List<PromotionsResponse>> {
    return promotionDao.getAll()
        .map {
          mapToPromotionResponse(it)
        }
  }

  private fun mapToPromotionResponse(promotions: List<PromotionEntity>): List<PromotionsResponse> {
    return promotions.map {
      when (it.id) {
        GAMIFICATION_ID ->
          GamificationResponse(it.id, it.priority, it.bonus!!, it.totalSpend!!, it.totalEarned!!,
              it.level!!, it.nextLevelAmount, it.status!!, it.bundle!!)
        REFERRAL_ID -> ReferralResponse(it.id, it.priority, it.maxAmount!!, it.available!!,
            it.bundle!!, it.completed!!, it.currency!!, it.symbol!!, it.invited!!, it.link,
            it.pendingAmount!!, it.receivedAmount!!, it.userStatus, it.minAmount!!, it.status!!,
            it.amount!!)
        else ->
          GenericResponse(it.id, it.priority, it.currentProgress, it.description!!, it.endDate!!,
              it.icon,
              it.linkedPromotionId, it.objectiveProgress, it.startDate, it.title!!, it.viewType!!
          )
      }
    }
  }

  override fun deletePromotions() = promotionDao.deleteAll()

  override fun insertPromotions(promotions: List<PromotionsResponse>): Completable {
    return Single.create<List<PromotionEntity>> { emitter ->
      val results: List<PromotionEntity> = promotions.map {
        when (it) {
          is GamificationResponse ->
            PromotionEntity(it.id, it.priority, bonus = it.bonus, totalSpend = it.totalSpend,
                totalEarned = it.totalEarned, level = it.level,
                nextLevelAmount = it.nextLevelAmount, status = it.status, bundle = it.bundle)
          is ReferralResponse -> {
            PromotionEntity(it.id, it.priority, maxAmount = it.maxAmount, available = it.available,
                bundle = it.bundle, completed = it.completed, currency = it.currency,
                symbol = it.symbol, invited = it.invited, link = it.link,
                pendingAmount = it.pendingAmount, receivedAmount = it.receivedAmount,
                userStatus = it.userStatus, minAmount = it.minAmount, status = it.status,
                amount = it.amount)
          }
          else -> {
            val genericResponse = it as GenericResponse
            PromotionEntity(genericResponse.id, genericResponse.priority,
                currentProgress = genericResponse.currentProgress,
                description = genericResponse.description, endDate = genericResponse.endDate,
                icon = genericResponse.icon, linkedPromotionId = genericResponse.linkedPromotionId,
                objectiveProgress = genericResponse.objectiveProgress,
                startDate = genericResponse.startDate, title = genericResponse.title,
                viewType = genericResponse.viewType)
          }
        }
      }
      emitter.onSuccess(results)
    }
        .flatMapCompletable { promotionDao.insert(it) }

  }
}