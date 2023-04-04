package com.appcoins.wallet.home

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val homeNavigationRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
  this.navigate(homeNavigationRoute, navOptions)
}

//fun NavGraphBuilder.forYouScreen() {
//  composable(route = homeNavigationRoute) {
//    HomeScreenRoute()
//  }
//}
