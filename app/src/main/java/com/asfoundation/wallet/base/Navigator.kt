package com.asfoundation.wallet.base

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator

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

/**
 * Same as [navigate] but supports extras for shared element transitions.
 */
fun Navigator.navigate(navController: NavController, destination: NavDirections , extras: FragmentNavigator.Extras) = with(
    navController) {
    currentDestination?.getAction(destination.actionId)
        ?.let { navigate(destination.actionId, destination.arguments , null ,extras) }
}