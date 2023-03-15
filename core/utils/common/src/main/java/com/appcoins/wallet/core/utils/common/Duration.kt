package com.appcoins.wallet.core.utils.common

import com.appcoins.wallet.core.utils.common.Duration.DurationUnits.addExact
import com.appcoins.wallet.core.utils.common.Duration.DurationUnits.floorDiv
import com.appcoins.wallet.core.utils.common.Duration.DurationUnits.floorMod
import com.appcoins.wallet.core.utils.common.Duration.DurationUnits.multiplyExact
import java.io.IOException
import java.io.InvalidObjectException
import java.io.ObjectInputStream
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

class Duration private constructor(val seconds: Long, val nano: Int) : Comparable<Duration>,
    Serializable {

  object DurationUnits {

    fun addExact(x: Long, y: Long): Long {
      val r = x + y
      if (x xor r and (y xor r) < 0) {
        throw java.lang.ArithmeticException("long overflow")
      }
      return r
    }

    fun floorDiv(x: Long, y: Long): Long {
      var r = x / y
      if (x xor y < 0 && r * y != x) {
        r--
      }
      return r
    }

    fun floorMod(x: Long, y: Long): Long {
      return x - floorDiv(x, y) * y
    }

    fun multiplyExact(x: Long, y: Long): Long {
      val r = x * y
      val ax = abs(x)
      val ay = abs(y)
      if (ax or ay ushr 31 != 0L) {
        if (y != 0L && r / y != x ||
            x == Long.MIN_VALUE && y == -1L) {
          throw java.lang.ArithmeticException("long overflow")
        }
      }
      return r
    }

  }

  operator fun plus(duration: Duration): Duration {
    return plus(duration.seconds, duration.nano.toLong())
  }

  private fun plus(secondsToAdd: Long, nanosToAdd: Long): Duration {
    var nanosToAdd = nanosToAdd
    if (secondsToAdd or nanosToAdd == 0L) {
      return this
    }
    var epochSec = addExact(seconds, secondsToAdd)
    epochSec = addExact(epochSec, nanosToAdd / NANOS_PER_SECOND)
    nanosToAdd %= NANOS_PER_SECOND
    val nanoAdjustment = nano + nanosToAdd // safe int+NANOS_PER_SECOND
    return ofSeconds(epochSec, nanoAdjustment)
  }

  operator fun minus(duration: Duration): Duration {
    val secsToSubtract = duration.seconds
    val nanosToSubtract = duration.nano
    return if (secsToSubtract == Long.MIN_VALUE) {
      plus(Long.MAX_VALUE, -nanosToSubtract.toLong()).plus(1, 0)
    } else plus(-secsToSubtract, -nanosToSubtract.toLong())
  }

  private fun multipliedBy(multiplicand: Long): Duration {
    if (multiplicand == 0L) {
      return ZERO
    }
    return if (multiplicand == 1L) {
      this
    } else create(
        toSeconds().multiply(BigDecimal.valueOf(multiplicand)))
  }

  private fun toSeconds(): BigDecimal {
    return BigDecimal.valueOf(seconds)
        .add(BigDecimal.valueOf(nano.toLong(), 9))
  }

  fun negated(): Duration {
    return multipliedBy(-1)
  }

  fun toDays(): Long {
    return seconds / SECONDS_PER_DAY
  }


  fun toHours(): Long {
    return seconds / SECONDS_PER_HOUR
  }

  override fun compareTo(otherDuration: Duration): Int {
    val cmp = seconds.compareTo(otherDuration.seconds)
    return if (cmp != 0) {
      cmp
    } else nano - otherDuration.nano
  }

  override fun equals(otherDuration: Any?): Boolean {
    if (this === otherDuration) {
      return true
    }
    if (otherDuration is Duration) {
      val other = otherDuration
      return seconds == other.seconds &&
          nano == other.nano
    }
    return false
  }

  override fun hashCode(): Int {
    return (seconds xor (seconds ushr 32)).toInt() + 51 * nano
  }

  override fun toString(): String {
    if (this === ZERO) {
      return "PT0S"
    }
    val hours: Long = seconds / SECONDS_PER_HOUR
    val minutes = (seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE) as Int
    val secs = (seconds % SECONDS_PER_MINUTE) as Int
    val buf = StringBuilder(24)
    buf.append("PT")
    if (hours != 0L) {
      buf.append(hours)
          .append('H')
    }
    if (minutes != 0) {
      buf.append(minutes)
          .append('M')
    }
    if (secs == 0 && nano == 0 && buf.length > 2) {
      return buf.toString()
    }
    if (secs < 0 && nano > 0) {
      if (secs == -1) {
        buf.append("-0")
      } else {
        buf.append(secs + 1)
      }
    } else {
      buf.append(secs)
    }
    if (nano > 0) {
      val pos = buf.length
      if (secs < 0) {
        buf.append(2 * NANOS_PER_SECOND - nano)
      } else {
        buf.append(nano + NANOS_PER_SECOND)
      }
      while (buf[buf.length - 1] == '0') {
        buf.setLength(buf.length - 1)
      }
      buf.setCharAt(pos, '.')
    }
    buf.append('S')
    return buf.toString()
  }

  private fun readObject(s: ObjectInputStream) {
    throw InvalidObjectException("Deserialization via serialization delegate")
  }

  companion object {
    val ZERO = Duration(0, 0)
    private const val serialVersionUID = 3078945930695997490L
    private val BI_NANOS_PER_SECOND = BigInteger.valueOf(1000000000L)

    private const val SECONDS_PER_DAY = 86400
    private const val SECONDS_PER_HOUR = 3600
    private const val SECONDS_PER_MINUTE = 60
    private const val NANOS_PER_SECOND = 1000000000L

    private val PATTERN = Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?" +
        "(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?",
        Pattern.CASE_INSENSITIVE)

    fun ofSeconds(seconds: Long, nanoAdjustment: Long): Duration {
      val secs = addExact(seconds, floorDiv(nanoAdjustment, NANOS_PER_SECOND))
      val nos = floorMod(nanoAdjustment, NANOS_PER_SECOND)
          .toInt()
      return create(secs, nos)
    }

    fun parse(text: CharSequence): Duration {
      Objects.requireNonNull(text, "text")
      val matcher = PATTERN.matcher(text)
      if (matcher.matches()) {
        // check for letter T but no time sections
        if ("T" != matcher.group(3)) {
          val negate = "-" == matcher.group(1)
          val dayMatch = matcher.group(2)
          val hourMatch = matcher.group(4)
          val minuteMatch = matcher.group(5)
          val secondMatch = matcher.group(6)
          val fractionMatch = matcher.group(7)
          if (dayMatch != null || hourMatch != null || minuteMatch != null || secondMatch != null) {
            val daysAsSecs = parseNumber(dayMatch, SECONDS_PER_DAY)
            val hoursAsSecs = parseNumber(hourMatch, SECONDS_PER_HOUR)
            val minsAsSecs = parseNumber(minuteMatch, SECONDS_PER_MINUTE)
            val seconds = parseNumber(secondMatch, 1)
            val nanos = parseFraction(fractionMatch, if (seconds < 0) -1 else 1)
            return try {
              create(negate, daysAsSecs, hoursAsSecs, minsAsSecs, seconds, nanos)
            } catch (ex: ArithmeticException) {
              throw IOException()
            }
          }
        }
      }
      throw IOException()
    }

    private fun parseNumber(parsed: String?, multiplier: Int): Long {
      return if (parsed == null) {
        0
      } else try {
        val `val` = parsed.toLong()
        multiplyExact(`val`, multiplier.toLong())
      } catch (ex: NumberFormatException) {
        throw IOException()
      } catch (ex: ArithmeticException) {
        throw IOException()
      }
    }

    private fun parseFraction(parsed: String?, negate: Int): Int {
      // regex limits to [0-9]{0,9}
      var temp = parsed
      return if (temp == null || temp.isEmpty()) {
        0
      } else try {
        temp = (parsed + "000000000").substring(0, 9)
        temp.toInt() * negate
      } catch (ex: NumberFormatException) {
        throw IOException()
      } catch (ex: ArithmeticException) {
        throw IOException()
      }
    }

    private fun create(negate: Boolean, daysAsSecs: Long, hoursAsSecs: Long, minsAsSecs: Long,
                       secs: Long, nanos: Int): Duration {
      val seconds = addExact(daysAsSecs, addExact(hoursAsSecs, addExact(minsAsSecs, secs)))
      return if (negate) {
        ofSeconds(seconds, nanos.toLong()).negated()
      } else ofSeconds(seconds, nanos.toLong())
    }

    private fun create(seconds: Long, nanoAdjustment: Int): Duration {
      return if (seconds or nanoAdjustment.toLong() == 0L) {
        ZERO
      } else Duration(seconds, nanoAdjustment)
    }

    private fun create(seconds: BigDecimal): Duration {
      val nanos = seconds.movePointRight(9)
          .toBigIntegerExact()
      val divRem = nanos.divideAndRemainder(BI_NANOS_PER_SECOND)
      if (divRem[0].bitLength() > 63) {
        throw ArithmeticException("Exceeds capacity of Duration: $nanos")
      }
      return ofSeconds(divRem[0].toLong(), divRem[1].toInt()
          .toLong())
    }

  }

}