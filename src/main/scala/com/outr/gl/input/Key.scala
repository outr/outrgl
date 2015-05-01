package com.outr.gl.input

import org.powerscala.enum.{EnumEntry, Enumerated}

/**
 * @author Matt Hicks <matt@outr.com>
 */
sealed abstract class Key private(val code: Int, val character: Option[Char] = None) extends EnumEntry {
  Key.synchronized {
    if (code >= 0) {
      Key.codes += code -> this
    }
    character match {
      case Some(c) => {
        Key.chars += c.toLower -> this
        Key.chars += c.toUpper -> this
      }
      case None => // No character
    }
  }
}

object Key extends Enumerated[Key] {
  private var codes = Map.empty[Int, Key]
  private var chars = Map.empty[Char, Key]

  case object T extends Key(48, Some('t'))
  case object Up extends Key(19)
  case object Left extends Key(21)
  case object Down extends Key(20)
  case object Right extends Key(22)
  case object PageUp extends Key(92)
  case object PageDown extends Key(93)
  case object W extends Key(51, Some('w'))
  case object A extends Key(29, Some('a'))
  case object S extends Key(47, Some('s'))
  case object D extends Key(32, Some('d'))
  case object Enter extends Key(66)
  case object Backspace extends Key(67)
  case object Escape extends Key(131)
  case object Backslash extends Key(73, Some('\\'))

  // Special keys
  case object Play extends Key(-1)
  case object Pause extends Key(-1)
  case object Next extends Key(-1)
  case object Previous extends Key(-1)
  case object Stop extends Key(-1)

  def byCode(code: Int) = codes.get(code)
  def byChar(char: Char) = chars.get(char)

  val values = findValues.toVector
}