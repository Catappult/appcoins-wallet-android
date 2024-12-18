package com.asfoundation.wallet.util.tuples

import java.io.Serializable

data class Quintuple<A, B, C, D, E>(
  var first: A,
  var second: B,
  var third: C,
  var fourth: D,
  var fifth: E
) :
  Serializable {
  override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}
