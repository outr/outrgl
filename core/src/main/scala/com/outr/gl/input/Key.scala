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

  case object A extends Key(29, Some('a'))
  case object B extends Key(30, Some('b'))
  case object C extends Key(31, Some('c'))
  case object D extends Key(32, Some('d'))
  case object E extends Key(33, Some('e'))
  case object F extends Key(34, Some('f'))
  case object G extends Key(35, Some('g'))
  case object H extends Key(36, Some('h'))
  case object I extends Key(37, Some('i'))
  case object J extends Key(38, Some('j'))
  case object K extends Key(39, Some('k'))
  case object L extends Key(40, Some('l'))
  case object M extends Key(41, Some('m'))
  case object N extends Key(42, Some('n'))
  case object O extends Key(43, Some('o'))
  case object P extends Key(44, Some('p'))
  case object Q extends Key(45, Some('q'))
  case object R extends Key(46, Some('r'))
  case object S extends Key(47, Some('s'))
  case object T extends Key(48, Some('t'))
  case object U extends Key(49, Some('u'))
  case object V extends Key(50, Some('v'))
  case object W extends Key(51, Some('w'))
  case object X extends Key(52, Some('x'))
  case object Y extends Key(53, Some('y'))
  case object Z extends Key(54, Some('z'))
  case object Up extends Key(19)
  case object Left extends Key(21)
  case object Down extends Key(20)
  case object Right extends Key(22)
  case object PageUp extends Key(92)
  case object PageDown extends Key(93)
  case object Enter extends Key(66)
  case object Backspace extends Key(67)
  case object Escape extends Key(131)
  case object Backslash extends Key(73, Some('\\'))
  case object Space extends Key(62, Some(' '))

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