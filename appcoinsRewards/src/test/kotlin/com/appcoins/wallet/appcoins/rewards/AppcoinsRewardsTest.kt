package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.Api
import com.appcoins.wallet.appcoins.rewards.repository.BdsApi
import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class AppcoinsRewardsTest {
  companion object {
    private const val ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val BALANCE: Long = 2
  }

  lateinit var appcoinsRewards: AppcoinsRewards
  @Before
  fun setUp() {
    val api = object : Api {
      override fun getBalance(address: String): Single<Api.RewardBalanceResponse> {
        return Single.just(Api.RewardBalanceResponse(BALANCE))
      }
    }

    appcoinsRewards =
        AppcoinsRewards(BdsAppcoinsRewardsRepository(BdsApi(api)), object : WalletAddressProvider {
          override fun getWallet(): Single<String> {
            return Single.just(ADDRESS)
          }
        })
  }

  @Test
  fun getBalance() {
    val testObserverNoAddress = TestObserver<Long>()
    appcoinsRewards.getBalance().subscribe(testObserverNoAddress)
    testObserverNoAddress.assertNoErrors().assertValue(BALANCE).assertComplete()

    val testObserverWithAddress = TestObserver<Long>()
    appcoinsRewards.getBalance(ADDRESS).subscribe(testObserverWithAddress)
    testObserverWithAddress.assertNoErrors().assertValue(BALANCE).assertComplete()
  }
}