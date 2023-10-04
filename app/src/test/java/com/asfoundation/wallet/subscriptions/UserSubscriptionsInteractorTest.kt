package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserSubscriptionsInteractorTest {

  private companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
  }

  @Mock
  lateinit var userSubscriptionsRepository: UserSubscriptionRepository

  @Mock
  lateinit var remoteRepository: RemoteRepository

  @Mock
  lateinit var walletService: WalletService

  private lateinit var interactor: UserSubscriptionsInteractor

  @Before
  fun setup() {
    interactor =
        UserSubscriptionsInteractor(walletService, remoteRepository, userSubscriptionsRepository)
  }

  @Test
  fun loadSubscriptionsTest() {
    val expectedModel = SubscriptionModel(true, null)
    Mockito.`when`(walletService.getWalletAddress())
        .thenReturn(Single.just(TEST_WALLET_ADDRESS))
    Mockito.`when`(userSubscriptionsRepository.getUserSubscriptions(TEST_WALLET_ADDRESS, true))
        .thenReturn(Observable.just(expectedModel))
    val observer = TestObserver<SubscriptionModel>()

    interactor.loadSubscriptions(true)
        .subscribe(observer)

    observer.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun cancelSubscriptionTest() {
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_ADDRESS)))
    Mockito.`when`(remoteRepository.cancelSubscription("packageName", "uid", TEST_WALLET_ADDRESS,
        TEST_WALLET_ADDRESS))
        .thenReturn(Completable.complete())
    val observer = TestObserver<Boolean>()

    interactor.cancelSubscription("packageName", "uid")
        .subscribe(observer)

    observer.assertComplete()
  }
}