package com.asfoundation.wallet.util

import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet

class SharedElementTransition : TransitionSet() {
  init {
    ordering = ORDERING_TOGETHER
    addTransition(ChangeBounds()).addTransition(ChangeTransform())
        .addTransition(ChangeImageTransform())
  }
}