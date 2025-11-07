package com.asfoundation.wallet.ui.login.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.FailedEntryRecover.AlreadyAdded
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.RecoverEntryPrivateKeyUseCase
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.AlreadyLoggedIn
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.ErrorAddingWallet
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.NewWalletSaved
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.WalletSwitched
import com.asfoundation.wallet.ui.login.webview_login.repository.LoginRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class FetchUserKeyUseCase @Inject constructor(
  private val loginRepository: LoginRepository,
  private val recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase,
  private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val setActiveWalletUseCase: SetActiveWalletUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val getAddressFromPrivateKeyUseCase: GetAddressFromPrivateKeyUseCase
) {

  operator fun invoke(
    authToken: String,
    email: String?
  ): Single<FetchUserKeyResult> {
    return Single.zip(
      loginRepository.fetchUserKey(authToken),
      getWalletInfoUseCase(null, true)
    ) { key, currentWalletInfo ->
      key to currentWalletInfo
    }
      .flatMap { (response, wallet) ->
        Single.zip(
          getAddressFromPrivateKeyUseCase(response.userKey),
          recoverEntryPrivateKeyUseCase(WalletKeyStore(email, response.userKey))
        ) { address, recoverResult ->
          Triple(address, recoverResult, wallet)
        }
      }
      .flatMap { (address, result, wallet) ->
        setDefaultWallet(
          result,
          email,
          wallet,
          address
        )
      }
  }

  private fun setDefaultWallet(
    recoverResult: RecoverEntryResult,
    email: String?,
    currentWalletInfo: WalletInfo,
    loginWalletAddress: String
  ): Single<FetchUserKeyResult> =
    when (recoverResult) {
      is AlreadyAdded -> {
        if (currentWalletInfo.wallet == loginWalletAddress) {
          if (currentWalletInfo.email == email) {
            Single.just(AlreadyLoggedIn)
          } else {
            loginWalletAddress
              .updateWalletInfo(email)
              .andThen(updateWalletNameUseCase(loginWalletAddress, email))
              .toSingleDefault(AlreadyLoggedIn)
          }
        } else {
          loginWalletAddress
            .updateWalletInfo(email)
            .andThen(updateWalletNameUseCase(loginWalletAddress, email))
            .andThen(setActiveWalletUseCase(loginWalletAddress))
            .toSingleDefault(
              WalletSwitched(
                email ?: loginWalletAddress
              )
            )
        }
      }

      is FailedEntryRecover -> Single.just(ErrorAddingWallet("Failed to recover wallet: $recoverResult"))

      is SuccessfulEntryRecover -> {
        setActiveWalletUseCase(recoverResult.address)
          .mergeWith(recoverResult.address.updateWalletInfo(email))
          .andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
          .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
          .toSingle {
            if (currentWalletInfo.wallet == loginWalletAddress && currentWalletInfo.email == null) {
              NewWalletSaved(
                email ?: loginWalletAddress
              )
            } else {
              WalletSwitched(
                email ?: loginWalletAddress
              )
            }
          }
      }
    }

  private fun String.updateWalletInfo(
    email: String?
  ): Completable = (email?.let { updateWalletInfoUseCase(this, it) }
    ?: updateWalletInfoUseCase(this))

  /**
   * The possible results of fetching a user key.
   *
   * Used to map the Fetch user key result to a snack bar message.
   */
  sealed class FetchUserKeyResult(open val message: String, val code: Int) {
    data class NewWalletSaved(override val message: String) :
      FetchUserKeyResult(message, NEW_WALLET_SAVED_CODE)

    data object AlreadyLoggedIn : FetchUserKeyResult("", ALREADY_LOGGED_IN_CODE)
    data class WalletSwitched(override val message: String) :
      FetchUserKeyResult(message, WALLET_SWITCHED_CODE)

    data class ErrorAddingWallet(override val message: String) :
      FetchUserKeyResult(message, ERROR_ADDING_WALLET_CODE)

    companion object {
      const val NEW_WALLET_SAVED_CODE = 100
      const val ALREADY_LOGGED_IN_CODE = 101
      const val WALLET_SWITCHED_CODE = 102
      const val ERROR_ADDING_WALLET_CODE = 103
    }
  }
}
