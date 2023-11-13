package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.ui.widgets.GameData
import com.asfoundation.wallet.repository.GamesRepositoryType
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.jupiter.api.Test

class GetGamesListingUseCaseTest {

  // Create a mock of the TransactionRepositoryType interface
  private val mockGamesRepository: GamesRepositoryType = mockk()
  private val getGamesListingUseCaseUseCase = GetGamesListingUseCase(mockGamesRepository)

  private val gamesData = listOf(
    GameData(
      title = "Mobile Legends",
      gameIcon = "https://cdn6.aptoide.com/imgs/b/3/e/b3e336be6c4874605cbc597d811d1822_icon.png?w=128",
      gameBackground = "https://cdn6.aptoide.com/imgs/e/e/0/ee0469bf46c9a4423baf41fe8dd59b43_screen.jpg",
      gamePackage = "com.mobile.legends",
      actionUrl = "www.aptoide.com",
    ),
    GameData(
      title = "Lords Mobile",
      gameIcon = "https://cdn6.aptoide.com/imgs/0/7/e/07eb83a511499243706f0c791b0b8969_icon.png?w=128",
      gameBackground = "https://cdn6.aptoide.com/imgs/4/d/a/4dafe1624f6f5d626e8761dbe903e9a0_screen.jpg",
      gamePackage = "com.igg.android.lordsmobile",
      actionUrl = "www.aptoide.com",
    )
  )

  @Test
  fun `when invoke is called, it should fetch the games list from repository`() {
    // Define the input parameters

    // Define the expected output
    val expectedGamesList = gamesData

    // Mock the behavior of the transaction repository
    every { mockGamesRepository.getGamesListing() } returns Single.just(
      expectedGamesList
    )

    // Call the method being tested
    val testObserver = TestObserver<List<GameData>>()
    getGamesListingUseCaseUseCase.invoke().subscribe(testObserver)
    val result = testObserver.values()[0]

    // Verify that the method returns the expected output
    assert(result == expectedGamesList)
  }

}
