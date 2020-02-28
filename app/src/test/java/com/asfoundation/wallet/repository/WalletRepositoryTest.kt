package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.WalletBalanceService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WalletRepositoryTest {

  @Mock
  lateinit var preferencesRepositoryType: PreferencesRepositoryType
  @Mock
  lateinit var accountKeystoreService: AccountKeystoreService
  @Mock
  lateinit var walletBalanceService: WalletBalanceService

  private lateinit var walletAddress: String

  private lateinit var walletRepository: WalletRepository

  @Before
  fun setUp() {
    walletAddress = "0x648234234"

    walletRepository =
        WalletRepository(preferencesRepositoryType, accountKeystoreService, walletBalanceService)
  }

  @Test
  fun test() {

  }


}