package com.asfoundation.wallet.ui

interface UpdateRequiredView {
  fun navigateToStoreAppView(deepLink: String)
  fun showError()
}
