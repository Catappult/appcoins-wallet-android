package cm.aptoide.skills.interfaces

interface PaymentView {
  fun showLoading()
  fun hideLoading()
  fun showError(errorCode : Int)
  fun showFraudError(isVerified: Boolean)
  fun showNoNetworkError()
  fun showRootError()
  fun showFingerprintAuthentication()
  fun showNeedsTopUpWarning()
}
