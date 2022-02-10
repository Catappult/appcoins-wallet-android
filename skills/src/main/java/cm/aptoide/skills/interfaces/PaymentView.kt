package cm.aptoide.skills.interfaces

interface PaymentView {
  fun showLoading()
  fun hideLoading()
  fun showError()
  fun showFraudError(isVerified: Boolean)
  fun showNoNetworkError()
  fun showFingerprintAuthentication()
}
