package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.appcoins.wallet.gamification.repository.entity.*
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.VOUCHER_ID
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.CurrentLevelInfo
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.vouchers.MockedVouchersRepository
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class PromotionsInteractorTest {

  private companion object {
    private const val TEST_WALLET_ADDRESS = "0x00000"
    private const val TEST_START_DATE: Long = 1000
    private const val TEST_END_DATE: Long = 11111
  }

  @Mock
  lateinit var vouchersRepository: MockedVouchersRepository

  @Mock
  lateinit var referralInteractor: ReferralInteractorContract

  @Mock
  lateinit var gamificationInteractor: GamificationInteractor

  @Mock
  lateinit var promotionsRepository: PromotionsRepository

  @Mock
  lateinit var findDefaultWalletInteractor: FindDefaultWalletInteract

  @Mock
  lateinit var userLocalData: UserStatsLocalData

  @Mock
  lateinit var analyticsSetup: AnalyticsSetup

  @Mock
  lateinit var mapper: GamificationMapper

  private val testWalletOrigin = WalletOrigin.APTOIDE
  private lateinit var gamificationResponse: GamificationResponse
  private lateinit var genericProgressResponse: GenericResponse
  private lateinit var genericActiveResponse: GenericResponse
  private lateinit var linkResponse: GenericResponse
  private lateinit var promotions: List<PromotionsResponse>
  private lateinit var scheduler: TestScheduler
  private lateinit var interactor: PromotionsInteractor

  @Before
  fun setup() {
    gamificationResponse =
        GamificationResponse(GAMIFICATION_ID, 100, 20.0, BigDecimal.ONE, BigDecimal.ONE, 1,
            BigDecimal.TEN, PromotionsResponse.Status.ACTIVE, false)
    genericProgressResponse =
        GenericResponse("id1", 0, BigDecimal(50), "description", TEST_END_DATE, null, null,
            BigDecimal(100), TEST_START_DATE, "Progress", "PROGRESS", null)
    genericActiveResponse =
        GenericResponse("id2", 0, null, "description", TEST_END_DATE, null, null, null,
            TEST_START_DATE, "default", "DEFAULT", null)
    //If priority of the linkPerk is higher than the gamificationResponse the tests will fails since the perk will not show
    linkResponse =
        GenericResponse("id3", 32, null, "description", TEST_END_DATE, null, GAMIFICATION_ID, null,
            TEST_START_DATE, "link", "DEFAULT", "any")
    promotions =
        listOf(gamificationResponse, genericProgressResponse, genericActiveResponse, linkResponse)

    scheduler = TestScheduler()
    interactor =
        PromotionsInteractor(referralInteractor, gamificationInteractor, promotionsRepository,
            vouchersRepository, findDefaultWalletInteractor, userLocalData, analyticsSetup,
            mapper)
    Mockito.`when`(findDefaultWalletInteractor.find())
        .thenReturn(Single.just(Wallet(TEST_WALLET_ADDRESS)))
    Mockito.`when`(promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(UserStatusResponse(promotions, testWalletOrigin)))
  }

  @Test
  fun retrievePromotionsTest() {
    val maxLevel = Levels.Level(BigDecimal.ONE, 25.0, 0)
    Mockito.`when`(gamificationInteractor.getLevels())
        .thenReturn(Single.just(Levels(Levels.Status.OK, listOf(maxLevel), true)))
    Mockito.`when`(mapper.mapCurrentLevelInfo(1))
        .thenReturn(CurrentLevelInfo(null, 0, "title", "phrase"))
    Mockito.`when`(vouchersRepository.getMockedVouchers())
        .thenReturn(Single.just(VoucherListModel(
            listOf(Voucher("test", "Trivial Drive Sample", "icon", true)))))

    val linkItem = GamificationLinkItem("id3", "description", null, TEST_START_DATE, TEST_END_DATE)
    val gamificationItem =
        GamificationItem(GAMIFICATION_ID, null, 1, 0, "title", BigDecimal(9), 20.0, 25.0,
            listOf(linkItem).toMutableList())
    val voucherItem = VoucherItem(VOUCHER_ID, "test", "Trivial Drive Sample", "icon", true, 25.0)
    val progressItem =
        ProgressItem("id1", "description", null, TEST_START_DATE,
            TEST_END_DATE, BigDecimal(50), BigDecimal(100), "any")
    val defaultItem =
        DefaultItem("id2", "description", null, TEST_START_DATE, TEST_END_DATE,
            null)

    val promotionsModel = interactor.retrievePromotions()
        .blockingGet()
    val testPromotionsModel = PromotionsModel(listOf(gamificationItem), listOf(voucherItem),
        listOf(progressItem, defaultItem, linkItem),
        com.asfoundation.wallet.promotions.WalletOrigin.APTOIDE, null)

    Assert.assertEquals(promotionsModel.promotions.size, testPromotionsModel.promotions.size)
    promotionsModel.promotions.forEachIndexed { index, promotion ->
      if (promotion is GamificationItem) {
        compareGamificationItem(promotion, testPromotionsModel.promotions[index])
      }
    }
    promotionsModel.vouchers.forEachIndexed { index, voucher ->
      compareVoucherItem(voucher, testPromotionsModel.vouchers[index])
    }
    promotionsModel.perks.forEachIndexed { index, perkPromotion ->
      if (perkPromotion is DefaultItem) {
        compareDefaultItem(perkPromotion, testPromotionsModel.perks[index] as DefaultItem)
      } else if (perkPromotion is ProgressItem) {
        compareProgressItem(perkPromotion, testPromotionsModel.perks[index] as ProgressItem)
      }
    }
    Assert.assertEquals(promotionsModel.walletOrigin, testPromotionsModel.walletOrigin)
    Assert.assertEquals(promotionsModel.error, testPromotionsModel.error)
  }

  @Test
  fun hasAnyPromotionUpdateNegativeTest() {
    Mockito.`when`(
        referralInteractor.hasReferralUpdate(TEST_WALLET_ADDRESS, null, ReferralsScreen.PROMOTIONS))
        .thenReturn(Single.just(false))
    Mockito.`when`(gamificationInteractor.hasNewLevel(TEST_WALLET_ADDRESS, gamificationResponse,
        GamificationContext.SCREEN_PROMOTIONS))
        .thenReturn(Single.just(false))
    Mockito.`when`(promotionsRepository.getSeenGenericPromotion(Mockito.anyString(),
        Mockito.anyString()))
        .thenReturn(true)
    Mockito.`when`(userLocalData.getSeenWalletOrigin(TEST_WALLET_ADDRESS))
        .thenReturn(testWalletOrigin.name)
    val hasPromotionUpdate = interactor.hasAnyPromotionUpdate(PromotionUpdateScreen.PROMOTIONS)
        .blockingGet()
    Assert.assertFalse(hasPromotionUpdate)
  }

  @Test
  fun hasAnyPromotionUpdatePositiveTest() {
    Mockito.`when`(
        referralInteractor.hasReferralUpdate(TEST_WALLET_ADDRESS, null, ReferralsScreen.PROMOTIONS))
        .thenReturn(Single.just(true))
    Mockito.`when`(gamificationInteractor.hasNewLevel(TEST_WALLET_ADDRESS, gamificationResponse,
        GamificationContext.SCREEN_PROMOTIONS))
        .thenReturn(Single.just(true))
    Mockito.`when`(promotionsRepository.getSeenGenericPromotion(Mockito.anyString(),
        Mockito.anyString()))
        .thenReturn(false)
    Mockito.`when`(userLocalData.getSeenWalletOrigin(TEST_WALLET_ADDRESS))
        .thenReturn("PARTNER")
    val hasPromotionUpdate = interactor.hasAnyPromotionUpdate(PromotionUpdateScreen.PROMOTIONS)
        .blockingGet()
    Assert.assertTrue(hasPromotionUpdate)
  }

  @Test
  fun getUnwatchedPromotionNotificationWithDetailsTest() {
    Mockito.`when`(promotionsRepository.getSeenGenericPromotion(Mockito.anyString(),
        Mockito.anyString()))
        .thenReturn(false)
    val unwatchedNotification = interactor.getUnwatchedPromotionNotification()
        .blockingGet()
    Assert.assertTrue(unwatchedNotification is PromotionNotification)
    val promotionNotification = unwatchedNotification as PromotionNotification
    Assert.assertEquals(promotionNotification.noResTitle, linkResponse.title)
    Assert.assertEquals(promotionNotification.noResBody, linkResponse.description)
    Assert.assertEquals(promotionNotification.noResIcon, linkResponse.icon)
    Assert.assertEquals(promotionNotification.title, null)
    Assert.assertEquals(promotionNotification.body, null)
    Assert.assertEquals(promotionNotification.icon, null)
    Assert.assertEquals(promotionNotification.positiveAction, CardNotificationAction.DETAILS_URL)
    Assert.assertEquals(promotionNotification.positiveButtonText, null)
  }

  @Test
  fun getUnwatchedPromotionNotificationNoDetailsTest() {
    Mockito.`when`(
        promotionsRepository.getSeenGenericPromotion(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(false)
    val noDetailsProgressResponse =
        GenericResponse("id1", 2, BigDecimal(50), "description", 11111, null, null,
            BigDecimal(100), 1000, "Progress", "PROGRESS", null)
    Mockito.`when`(promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS))
        .thenReturn(
            Single.just(UserStatusResponse(listOf(noDetailsProgressResponse), testWalletOrigin)))

    val unwatchedNotification = interactor.getUnwatchedPromotionNotification()
        .blockingGet()
    Assert.assertTrue(unwatchedNotification is PromotionNotification)
    val promotionNotification = unwatchedNotification as PromotionNotification
    Assert.assertEquals(promotionNotification.noResTitle, noDetailsProgressResponse.title)
    Assert.assertEquals(promotionNotification.noResBody, noDetailsProgressResponse.description)
    Assert.assertEquals(promotionNotification.noResIcon, noDetailsProgressResponse.icon)
    Assert.assertEquals(promotionNotification.title, null)
    Assert.assertEquals(promotionNotification.body, null)
    Assert.assertEquals(unwatchedNotification.icon, null)
    Assert.assertEquals(unwatchedNotification.positiveAction, CardNotificationAction.NONE)
    Assert.assertEquals(unwatchedNotification.positiveButtonText, null)
  }

  @Test
  fun getUnwatchedPromotionNotificationFutureTest() {
    Mockito.`when`(
        promotionsRepository.getSeenGenericPromotion(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(false)
    val futureResponse =
        GenericResponse("id1", 2, BigDecimal(50), "description", 11111, null, null,
            BigDecimal(100), 10000000000000, "Progress", "PROGRESS", null)
    Mockito.`when`(promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS))
        .thenReturn(
            Single.just(UserStatusResponse(listOf(futureResponse), testWalletOrigin)))

    val unwatchedNotification = interactor.getUnwatchedPromotionNotification()
        .blockingGet()
    Assert.assertTrue(unwatchedNotification is EmptyNotification)
    Assert.assertEquals(unwatchedNotification.title, -1)
    Assert.assertEquals(unwatchedNotification.body, -1)
    Assert.assertEquals(unwatchedNotification.icon, -1)
    Assert.assertEquals(unwatchedNotification.positiveAction, CardNotificationAction.DISMISS)
    Assert.assertEquals(unwatchedNotification.positiveButtonText, -1)
  }

  @Test
  fun getUnwatchedPromotionNotificationEmptyListTest() {
    Mockito.`when`(promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS))
        .thenReturn(
            Single.just(UserStatusResponse(emptyList(), testWalletOrigin)))
    val unwatchedNotification = interactor.getUnwatchedPromotionNotification()
        .blockingGet()
    Assert.assertTrue(unwatchedNotification is EmptyNotification)
    Assert.assertEquals(unwatchedNotification.title, -1)
    Assert.assertEquals(unwatchedNotification.body, -1)
    Assert.assertEquals(unwatchedNotification.icon, -1)
    Assert.assertEquals(unwatchedNotification.positiveAction, CardNotificationAction.DISMISS)
    Assert.assertEquals(unwatchedNotification.positiveButtonText, -1)
  }

  @Test
  fun dismissNotificationTest() {
    val id = "id"
    interactor.dismissNotification(id)
        .blockingGet()
    Mockito.verify(
        promotionsRepository)
        .setSeenGenericPromotion(id, PromotionUpdateScreen.TRANSACTIONS.name)
  }

  @Test
  fun shouldShowGamificationDisclaimerTest() {
    interactor.shouldShowGamificationDisclaimer()
    Mockito.verify(userLocalData)
        .shouldShowGamificationDisclaimer()
  }

  @Test
  fun setGamificationDisclaimerShown() {
    interactor.setGamificationDisclaimerShown()
    Mockito.verify(userLocalData)
        .setGamificationDisclaimerShown()
  }

  private fun compareProgressItem(perkPromotion: ProgressItem, progressItem: ProgressItem) {
    Assert.assertEquals(perkPromotion.id, progressItem.id)
    Assert.assertEquals(perkPromotion.startDate, progressItem.startDate)
    Assert.assertEquals(perkPromotion.endDate, progressItem.endDate)
    Assert.assertEquals(perkPromotion.icon, progressItem.icon)
    Assert.assertEquals(perkPromotion.description, progressItem.description)
    Assert.assertEquals(perkPromotion.current, progressItem.current)
    Assert.assertEquals(perkPromotion.objective, progressItem.objective)
  }

  private fun compareDefaultItem(perkPromotion: DefaultItem, testDefault: DefaultItem) {
    Assert.assertEquals(perkPromotion.id, testDefault.id)
    Assert.assertEquals(perkPromotion.startDate, testDefault.startDate)
    Assert.assertEquals(perkPromotion.endDate, testDefault.endDate)
    Assert.assertEquals(perkPromotion.icon, testDefault.icon)
    Assert.assertEquals(perkPromotion.description, testDefault.description)
  }

  private fun compareVoucherItem(voucherItem: VoucherItem, testVoucherItem: VoucherItem) {
    Assert.assertEquals(voucherItem.id, testVoucherItem.id)
    Assert.assertEquals(voucherItem.maxBonus, testVoucherItem.maxBonus, 0.0)
    Assert.assertEquals(voucherItem.title, testVoucherItem.title)
    Assert.assertEquals(voucherItem.hasAppcoins, testVoucherItem.hasAppcoins)
    Assert.assertEquals(voucherItem.packageName, testVoucherItem.packageName)
  }

  private fun compareGamificationItem(promotion: GamificationItem, testPromotion: Promotion) {
    val testGamificationItem = testPromotion as GamificationItem
    Assert.assertEquals(promotion.id, testGamificationItem.id)
    Assert.assertEquals(promotion.bonus, testGamificationItem.bonus, 0.0)
    Assert.assertEquals(promotion.level, testGamificationItem.level)
    Assert.assertEquals(promotion.levelColor, testGamificationItem.levelColor)
    Assert.assertEquals(promotion.maxBonus, testGamificationItem.maxBonus, 0.0)
    Assert.assertEquals(promotion.planet, testGamificationItem.planet)
    Assert.assertEquals(promotion.title, testGamificationItem.title)
    Assert.assertEquals(promotion.toNextLevelAmount, testGamificationItem.toNextLevelAmount)
    Assert.assertEquals(promotion.links.size, testGamificationItem.links.size)
    promotion.links.forEachIndexed { linkIndex, gamificationLinkItem ->
      compareGamificationLinkItem(gamificationLinkItem, testPromotion.links[linkIndex])
    }
  }

  private fun compareGamificationLinkItem(gamificationLinkItem: GamificationLinkItem,
                                          testGamificationLinkItem: GamificationLinkItem) {
    Assert.assertEquals(gamificationLinkItem.id, testGamificationLinkItem.id)
    Assert.assertEquals(gamificationLinkItem.description, testGamificationLinkItem.description)
    Assert.assertEquals(gamificationLinkItem.icon, testGamificationLinkItem.icon)
    Assert.assertEquals(gamificationLinkItem.startDate, testGamificationLinkItem.startDate)
    Assert.assertEquals(gamificationLinkItem.endDate, testGamificationLinkItem.endDate)
    Assert.assertEquals(gamificationLinkItem.detailsLink, testGamificationLinkItem.detailsLink)
  }
}