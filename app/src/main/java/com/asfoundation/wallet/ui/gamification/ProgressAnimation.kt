package com.asfoundation.wallet.ui.gamification

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar

class ProgressAnimation(private val progressBar: ProgressBar?, private val from: Float,
                        private val to: Float) : Animation() {

  override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
    super.applyTransformation(interpolatedTime, t)
    val value = from + (to - from) * interpolatedTime
    progressBar?.progress = value.toInt()
  }

}