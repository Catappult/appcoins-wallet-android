package com.asfoundation.wallet.service

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
  private lateinit var scheduler: TestScheduler


  companion object {
    const val ELIGIBLE_ADDRESS = "0xAddress1"
    const val NOT_ELIGIBLE_ADDRESS = "0xAddress2"
    const val REQUIRES_VALIDATION_ADDRESS = "0xAddress3"
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
            GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.ELIGIBLE, CAMPAIGN_ID)))
    `when`(api.getCampaign(NOT_ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)).thenReturn(
        Observable.just(GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.NOT_ELIGIBLE,
            CAMPAIGN_ID)))
    `when`(api.getCampaign(REQUIRES_VALIDATION_ADDRESS, PACKAGE_WITH_CAMPAIGN,
        VERSION_CODE)).thenReturn(
        Observable.just(
            GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.REQUIRES_VALIDATION,
                CAMPAIGN_ID)))
    `when`(api.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITHOUT_CAMPAIGN, VERSION_CODE)).thenReturn(
        Observable.just(
            GetCampaignResponse(GetCampaignResponse.EligibleResponseStatus.ELIGIBLE, null)))

    scheduler = TestScheduler()
    campaignService = CampaignService(api)
  }


  @Test
  fun testUserEligible() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
        .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors().assertValue(CAMPAIGN_ID)

  }

  @Test
  fun testUserNotEligible() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(NOT_ELIGIBLE_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
        .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors().assertValue("")

  }

  @Test
  fun testUserRequiresValidation() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(REQUIRES_VALIDATION_ADDRESS, PACKAGE_WITH_CAMPAIGN, VERSION_CODE)
        .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors().assertValue(CAMPAIGN_ID)

  }

  @Test
  fun testNoCampaignAvailable() {
    val observer = TestObserver<String>()
    campaignService.getCampaign(ELIGIBLE_ADDRESS, PACKAGE_WITHOUT_CAMPAIGN, VERSION_CODE)
        .subscribe(observer)
    scheduler.triggerActions()
    observer.awaitTerminalEvent()

    observer.assertNoErrors().assertValue("")

  }

}