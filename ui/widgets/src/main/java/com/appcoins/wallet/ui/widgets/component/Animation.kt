package com.appcoins.wallet.ui.widgets.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun Animation(
  modifier: Modifier,
  animationRes: Int,
  iterations: Int = Int.MAX_VALUE,
  restartOnPlay: Boolean = true,
  onAnimationEnd: () -> Unit = {}
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
  val animationState = animateLottieCompositionAsState(
    composition = composition,
    iterations = iterations,
    restartOnPlay = restartOnPlay
  )

  LaunchedEffect(animationState.isAtEnd && !animationState.isPlaying) {
    if (animationState.isAtEnd && !animationState.isPlaying) {
      onAnimationEnd()
    }
  }

  LottieAnimation(
    modifier = modifier,
    composition = composition,
    progress = { animationState.progress }
  )
}


@Composable
fun Animation(modifier: Modifier = Modifier, animationRes: Int, iterations: Int = Int.MAX_VALUE, contentScale: ContentScale = ContentScale.Fit) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
  val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = iterations
  )
  LottieAnimation(
    modifier = modifier,
    composition = composition,
    progress = { progress },
    contentScale = contentScale
  )
}
