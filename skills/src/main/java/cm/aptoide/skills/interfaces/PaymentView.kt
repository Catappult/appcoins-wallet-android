package cm.aptoide.skills.interfaces

interface PaymentView {
  fun showLoading()
  fun hideLoading()
  fun showError()
  fun showFraudError()
  fun showNoNetworkError()
  fun showFingerprintAuthentication()
}
