package com

import java.math.BigInteger
import java.util.UUID

/**
 * Functions for UUIDs.
 */
package object example {

  val base16 = 16
  val base36 = 36
  val base16Length = 32
  val base36Length = 25

  // scalastyle:off magic.number
  val uuidDashPositions = List(8, 12, 16, 20)
  // scalastyle:on magic.number

  implicit class StringOps(val x: String) extends AnyVal {

    def uuidFromBase36String: UUID = {
      val number = new BigInteger(x, base36)
      val base16Unleaded = number.toString(base16)
      val base16Number = base16Unleaded.withLeadingZeroes(base16Length)
      UUID.fromString(base16Number.withUUIDDashes)
    }

    def withLeadingZeroes(length: Int): String = {
      "0" * (length - x.length) + x
    }

    def withUUIDDashes: String = {

      def insertDash(position: Int, value: String): String = {
        val (s1, s2) = value.splitAt(position)
        s1 + "-" + s2
      }

      uuidDashPositions.foldRight(x)(insertDash)
    }

  }

  implicit class UUIDOps(val x: UUID) extends AnyVal {

    /**
     * Converts the UUID to a base 36 number.
     * Adds leading zeroes, so that the returned
     * string is exactly 25 characters wide.
     *
     * A UUID will always fit into 25 "digits"
     * in base 36 since:
     * - UUID is a 128 bit number
     * - log36 (2 ^^ 128 - 1) ~= 24.758
     *
     * @return
     */
    def toBase36String: String = {
      val withoutDashes = x.toString.replace("-", "")
      val number = new BigInteger(withoutDashes, base16)
      val base36Unleaded = number.toString(base36)
      base36Unleaded.withLeadingZeroes(base36Length)
    }

  }

}
