package com.outr.gl

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <matt@outr.com>
 */
sealed abstract class Vertical extends EnumEntry

object Vertical extends Enumerated[Vertical] {
  case object Top extends Vertical
  case object Middle extends Vertical
  case object Bottom extends Vertical
  case object Default extends Vertical

  val values = findValues.toVector
}