package com.appcoins.wallet.ui.widgets.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun Animation(modifier: Modifier, animationRes: Int, iterations: Int = Int.MAX_VALUE, restartOnPlay: Boolean = true) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
  val progress by animateLottieCompositionAsState(composition, iterations = iterations, restartOnPlay = restartOnPlay)
  LottieAnimation(modifier = modifier, composition = composition, progress = { progress })
}
