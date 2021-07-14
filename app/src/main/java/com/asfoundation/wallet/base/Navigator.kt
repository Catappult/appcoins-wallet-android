package com.asfoundation.wallet.base

import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * Helper functions for navigating.
 */
interface Navigator

/**
 * Safe navigate function to help avoid IllegalArgumentExceptions on concurrent navigation events.
 */
fun Navigator.navigate(navController: NavController, destination: NavDirections) = with(
    navController) {
  currentDestination?.getAction(destination.actionId)
      ?.let { navigate(destination) }
}