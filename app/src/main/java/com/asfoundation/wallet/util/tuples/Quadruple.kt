package com.asfoundation.wallet.util.tuples

import java.io.Serializable

data class Quadruple<A, B, C, D>(var first: A, var second: B, var third: C, var fourth: D) :
  Serializable {
  override fun toString(): String = "($first, $second, $third, $fourth)"
}
