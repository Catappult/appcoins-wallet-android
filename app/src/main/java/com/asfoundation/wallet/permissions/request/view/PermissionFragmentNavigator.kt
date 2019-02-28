package com.asfoundation.wallet.permissions.request.view

interface PermissionFragmentNavigator {
  fun closeSuccess(walletAddress: String)
  fun closeCancel()
}
