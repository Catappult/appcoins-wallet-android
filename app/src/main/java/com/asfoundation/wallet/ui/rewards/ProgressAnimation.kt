package com.asfoundation.wallet.ui.rewards

import android.widget.ProgressBar
import android.view.animation.Animation
import android.view.animation.Transformation


class ProgressAnimation(private val progressBar: ProgressBar?, private val from: Float,
                           private val to: Float) : Animation() {

  override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
    super.applyTransformation(interpolatedTime, t)
    val value = from + (to - from) * interpolatedTime
    progressBar?.progress = value.toInt()
  }

}