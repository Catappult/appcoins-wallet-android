package com.asfoundation.wallet.util.tuples

import java.io.Serializable

data class Sextuple<A, B, C, D, E, F>(
  var first: A,
  var second: B,
  var third: C,
  var fourth: D,
  var fifth: E,
  var sixth: F
) :
  Serializable {
  override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}
