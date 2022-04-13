package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.util.FakeSchedulers
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CampaignServiceTest {

  @Mock
  lateinit var api: CampaignService.CampaignApi
  private lateinit var campaignService: CampaignService
  private val fakeSchedulers: RxSchedulers = FakeSchedulers()
  private val scheduler: TestScheduler = fakeSchedulers.main as TestScheduler

  companion object {
    const val ELIGIBLE_ADDRESS = "0xAddress1"
    const val NOT_ELIGIBLE_ADDRESS = "0xAddress2"
    const val REQUIRES_VALIDATION_ADDRESS = "0xAddress3"
    const val POA_LIMIT_REACHED_ADDRESS = "0xAddress4"
    const val PACKAGE_WITH_CAMPAIGN = "application_with_campaign"
    const val PACKAGE_WITHOUT_CAMPAIGN = "application_without_campaign"
    const val VERSION_CODE = 1
    const val CAMPAIGN_ID = "1"
  }


  @Before
  @Throws(Exception::class)
  fun setUp() {
    `when`(api.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)).thenReturn(
      Observable.just(
        GetCampaignResponse(
          GetCampaignResponse.EligibleResponseStatus.ELIGIBLE, CAMPAIGN_ID, 0,
          0
        )
      )
    )
    `when`(api.getCampaign(NOT_ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)).thenReturn(
      Observable.just(
        GetCampaignResponse(
          GetCampaignResponse.EligibleResponseStatus.NOT_ELIGIBLE,
          CAMPAIGN_ID, 0, 0
        )
      )
    )
    `when`(
      api.getCampaign(
        REQUIRES_VALIDATION_ADDRESS, PACKAGE_WITH_CAMPAIGN,
        VERSION_CODE
      )
    ).thenReturn(
      Observable.just(
        GetCampaignResponse(
          GetCampaignResponse.EligibleResponseStatus.REQUIRES_VALIDATION,
          CAMPAIGN_ID, 0, 0
        )
      )
    )
    `when`(api.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITHOUT_CAMPAIGN, VERSION_CODE)).thenReturn(
      Observable.just(
        GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.ELIGIBLE, null, 0, 0)
      )
    )
    `when`(
      api.getCampaign(
        POA_LIMIT_REACHED_ADDRESS, PACKAGE_WITH_CAMPAIGN,
        VERSION_CODE
      )
    ).thenReturn(
      Observable.just(
        GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.NOT_ELIGIBLE, null, 1, 5)
      )
    )
    `when`(
      api.getPoaInformation(POA_LIMIT_REACHED_ADDRESS)
    ).thenReturn(
      Observable.just(PoaInformationResponse(1, 0, 5))
    )
    `when`(
      api.getPoaInformation(ELIGIBLE_ADDRESS)
    ).thenReturn(
      Observable.just(PoaInformationResponse(0, 3, 0))
    )
    campaignService = CampaignService(api, BuildConfig.VERSION_CODE, fakeSchedulers)
  }


  @Test
  fun testUserEligible() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
      .map { it.campaignId }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(CAMPAIGN_ID)

  }

  @Test
  fun testUserNotEligible() {
    val observer = TestObserver<CampaignStatus>()
    campaignService.getCampaign(NOT_ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
      .map { it.campaignStatus }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(CampaignStatus.NOT_ELIGIBLE)

  }

  @Test
  fun testUserRequiresValidation() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(REQUIRES_VALIDATION_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
      .map { it.campaignId }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(CAMPAIGN_ID)

  }

  @Test
  fun testNoCampaignAvailable() {
    val observer = TestObserver<CampaignStatus>()
    campaignService.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITHOUT_CAMPAIGN, VERSION_CODE)
      .map { it.campaignStatus }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(CampaignStatus.NOT_ELIGIBLE)

  }

  @Test
  fun testPoaLimitReached() {
    val observer = TestObserver<Pair<Int, Int>>()
    campaignService.getCampaign(POA_LIMIT_REACHED_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
      .map { Pair(it.hoursRemaining, it.minutesRemaining) }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(Pair(1, 5))
  }

  @Test
  fun testPoaRetriveInformationLimitReached() {
    val observer = TestObserver<Triple<Int, Int, Int>>()
    campaignService.retrievePoaInformation(POA_LIMIT_REACHED_ADDRESS)
      .map { Triple(it.remainingHours, it.remainingMinutes, it.remainingPoa) }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(Triple(1, 5, 0))
  }

  @Test
  fun testPoaRetrieveInformationNoLimitReached() {
    val observer = TestObserver<Triple<Int, Int, Int>>()
    campaignService.retrievePoaInformation(ELIGIBLE_ADDRESS)
      .map { Triple(it.remainingHours, it.remainingMinutes, it.remainingPoa) }
      .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors()
      .assertValue(Triple(0, 0, 3))
  }
}