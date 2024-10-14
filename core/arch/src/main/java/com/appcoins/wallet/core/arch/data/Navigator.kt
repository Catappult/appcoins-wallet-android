package com.appcoins.wallet.core.arch.data

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

/**
 * Helper functions for navigating.
 */
interface Navigator

/**
 * Safe navigate function to help avoid IllegalArgumentExceptions on concurrent navigation events.
 */
fun navigate(navController: NavController, destination: NavDirections) = with(
  navController
) {
  currentDestination?.getAction(destination.actionId)
    ?.let { navigate(destination) }
}

/**
 * Same as [navigate] but uses the action id to navigate and supports [Bundle] and [NavOptions].
 */
fun navigate(
  navController: NavController,
  resId: Int,
  args: Bundle? = null,
  navOptions: NavOptions? = null
) = with(
  navController
) {
  currentDestination?.getAction(resId)
    ?.let { navigate(resId, args, navOptions) }
}

/**
 * Same as [navigate] but supports extras for shared element transitions.
 */
fun navigate(
  navController: NavController,
  destination: NavDirections,
  extras: androidx.navigation.Navigator.Extras? = null,
  navOptions: NavOptions? = null
) = with(
  navController
) {
  currentDestination?.getAction(destination.actionId)
    ?.let { navigate(destination.actionId, destination.arguments, navOptions, extras) }
}