package com.asfoundation.wallet.firebase_messaging

import com.asfoundation.wallet.firebase_messaging.domain.RegisterFirebaseTokenUseCaseImpl
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.IOException

class RegisterFirebaseTokenUseCaseImplTest {

  private val walletRepository = mockk<WalletRepositoryType> {
    every { fetchWallets() } returns Single.error(IOException("offline"))
  }
  private val useCase =
    RegisterFirebaseTokenUseCaseImpl(walletRepository, mockk(), mockk(), mockk())

  @Test
  fun `network failure retries maxRetries times then gives up without throwing`() = runBlocking {
    useCase.registerFirebaseTokenForAllWallets("token", retry = 0, maxRetries = 3)

    verify(exactly = 4) { walletRepository.fetchWallets() }
  }
}
