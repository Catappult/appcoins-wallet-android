package com.asfoundation.wallet.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.transfers.TransferConfirmationInteractor
import com.asfoundation.wallet.transfers.TransferConfirmationNavigator
import com.asfoundation.wallet.util.BalanceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject


class TransferConfirmationViewModel @Inject constructor(
  private val transferConfirmationInteractor: TransferConfirmationInteractor,
  private val gasSettingsRouter: GasSettingsRouter,
  private val logger: Logger,
  private val transactionConfirmationNavigator: TransferConfirmationNavigator
) : BaseViewModel() {

  private val transactionBuilder = MutableLiveData<TransactionBuilder>()
  private val transactionHash = MutableLiveData<PendingTransaction>()
  private var subscription: Disposable? = null

  companion object {
    private val TAG = TransferConfirmationViewModel::class.java.simpleName
  }

  fun init(transactionBuilder: TransactionBuilder) {
    subscription =
        transferConfirmationInteractor.fetchGasSettings(transactionBuilder.shouldSendToken())
            .doOnSuccess { gasSettings: GasSettings? ->
              transactionBuilder.gasSettings(gasSettings)
              this.transactionBuilder.postValue(transactionBuilder)
            }
            .subscribe({}, { throwable: Throwable -> onError(throwable) })
  }

  override fun onCleared() {
    subscription?.let {
      if (!it.isDisposed) {
        it.dispose()
      }
    }
    super.onCleared()
  }

  override fun onError(throwable: Throwable) {
    super.onError(throwable)
    logger.log(TAG, throwable.message, throwable)
  }

  fun transactionBuilder(): LiveData<TransactionBuilder> {
    return transactionBuilder
  }

  fun transactionHash(): LiveData<PendingTransaction> {
    return transactionHash
  }

  fun openGasSettings(context: Activity?) {
    val transactionBuilder = transactionBuilder.value
    transactionBuilder?.let {
      gasSettingsRouter.open(context, it.gasSettings())
    }
  }

  private fun onCreateTransaction(pendingTransaction: PendingTransaction) {
    transactionHash.postValue(pendingTransaction)
    transactionConfirmationNavigator.showEtherTransactionBottomSheet(pendingTransaction.hash)
  }

  fun progressFinished() {
    progress.postValue(false)
  }

  fun send() {
    progress.postValue(true)
    disposable.add(transferConfirmationInteractor.send(transactionBuilder.value)
        .map { hash: String? -> PendingTransaction(hash, false) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { pendingTransaction: PendingTransaction ->
              onCreateTransaction(pendingTransaction)
            }) { throwable: Throwable -> onError(throwable) })
  }

  fun setGasSettings(gasSettings: GasSettings?) {
    val transactionBuilder = transactionBuilder.value
    val gasPriceWei = BalanceUtils.gweiToWei(gasSettings?.gasPrice)
    val newGasSettings = GasSettings(gasPriceWei, gasSettings?.gasLimit)
    transactionBuilder?.let {
      it.gasSettings(newGasSettings)
      this.transactionBuilder.value = it // refresh view
    }
  }

  private fun getGasPreferences(): GasSettings = transferConfirmationInteractor.getGasPreferences()

  private fun getMaxGasPriceGwei(networkFeeMax: BigInteger, gasLimitMax: BigDecimal,
                                 gasPriceMin: BigDecimal): BigDecimal {
    return BalanceUtils.weiToGwei(BigDecimal(networkFeeMax.divide(gasLimitMax.toBigInteger())
        .subtract(gasPriceMin.toBigInteger())))
  }

  private fun getMinGasPriceGwei(gasPriceMin: BigDecimal): BigDecimal =
      BalanceUtils.weiToGwei(gasPriceMin)

  fun handleSavedGasSettings(gasPrice: BigDecimal, gasLimitMin: BigDecimal,
                             networkFeeMax: BigInteger,
                             gasPriceMinWei: BigDecimal, gasLimitMax: BigDecimal,
                             gasLimit: BigDecimal): GasSettings {
    val gasPriceMaxGwei = getMaxGasPriceGwei(networkFeeMax, gasLimitMax, gasPriceMinWei)
    val gasPriceMinGwei = getMinGasPriceGwei(gasPriceMinWei)
    val savedGasPreferences = getGasPreferences()
    val displayedGasPrice =
        if (isSavedLimitInRange(savedGasPreferences.gasPrice, gasPriceMinGwei, gasPriceMaxGwei)) {
          savedGasPreferences.gasPrice
        } else {
          BalanceUtils.weiToGwei(gasPrice)
        }
    val displayedGasLimit =
        if (isSavedLimitInRange(savedGasPreferences.gasLimit, gasLimitMin, gasLimitMax)) {
          savedGasPreferences.gasLimit
        } else {
          gasLimit
        }
    return GasSettings(displayedGasPrice, displayedGasLimit)
  }

  private fun isSavedLimitInRange(savedValue: BigDecimal?, minValue: BigDecimal,
                                  maxValue: BigDecimal): Boolean {
    return savedValue != null && savedValue > minValue && savedValue < maxValue
  }

}