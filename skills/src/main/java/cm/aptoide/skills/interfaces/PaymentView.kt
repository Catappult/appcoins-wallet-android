package cm.aptoide.skills.interfaces

interface PaymentView {
  fun showLoading()
  fun hideLoading()
  fun showError(errorCode: Int)
  fun showFraudError(isVerified: Boolean)
  fun showNoNetworkError()
  fun showRootError()
  fun showWalletVersionNotSupportedError()
  fun showFingerprintAuthentication()
  fun showNeedsTopUpWarning()
  fun showNoFundsWarning()
  fun showPaymentMethodNotSupported()
}
