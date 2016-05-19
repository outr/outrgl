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

  def is(key: Key) = key.code == code || key.character == character
}

object Key extends Enumerated[Key] {
  private var codes = Map.empty[Int, Key]
  private var chars = Map.empty[Char, Key]

  import com.badlogic.gdx.Input.{Keys => k}
  
  case object Zero extends Key(k.NUM_0, Some('0'))
  case object One extends Key(k.NUM_1, Some('1'))
  case object Two extends Key(k.NUM_2, Some('2'))
  case object Three extends Key(k.NUM_3, Some('3'))
  case object Four extends Key(k.NUM_4, Some('4'))
  case object Five extends Key(k.NUM_5, Some('5'))
  case object Six extends Key(k.NUM_6, Some('6'))
  case object Seven extends Key(k.NUM_7, Some('7'))
  case object Eight extends Key(k.NUM_8, Some('8'))
  case object Nine extends Key(k.NUM_9, Some('9'))

  case object Up extends Key(k.UP)
  case object Left extends Key(k.LEFT)
  case object Down extends Key(k.DOWN)
  case object Right extends Key(k.RIGHT)
  case object Center extends Key(k.CENTER)

  case object A extends Key(k.A, Some('a'))
  case object B extends Key(k.B, Some('b'))
  case object C extends Key(k.C, Some('c'))
  case object D extends Key(k.D, Some('d'))
  case object E extends Key(k.E, Some('e'))
  case object F extends Key(k.F, Some('f'))
  case object G extends Key(k.G, Some('g'))
  case object H extends Key(k.H, Some('h'))
  case object I extends Key(k.I, Some('i'))
  case object J extends Key(k.J, Some('j'))
  case object K extends Key(k.K, Some('k'))
  case object L extends Key(k.L, Some('l'))
  case object M extends Key(k.M, Some('m'))
  case object N extends Key(k.N, Some('n'))
  case object O extends Key(k.O, Some('o'))
  case object P extends Key(k.P, Some('p'))
  case object Q extends Key(k.Q, Some('q'))
  case object R extends Key(k.R, Some('r'))
  case object S extends Key(k.S, Some('s'))
  case object T extends Key(k.T, Some('t'))
  case object U extends Key(k.U, Some('u'))
  case object V extends Key(k.V, Some('v'))
  case object W extends Key(k.W, Some('w'))
  case object X extends Key(k.X, Some('x'))
  case object Y extends Key(k.Y, Some('y'))
  case object Z extends Key(k.Z, Some('z'))

  case object AltLeft extends Key(k.ALT_LEFT)
  case object AltRight extends Key(k.ALT_RIGHT)
  case object AngleLeft extends Key(-1, Some('<'))
  case object AngleRight extends Key(-1, Some('>'))
  case object Apostrophe extends Key(k.APOSTROPHE, Some('\''))
  case object Asterisk extends Key(k.STAR, Some('*'))
  case object At extends Key(k.AT, Some('@'))
  case object Backslash extends Key(k.BACKSLASH, Some('\\'))
  case object BracketLeft extends Key(k.LEFT_BRACKET, Some('['))
  case object BracketRight extends Key(k.RIGHT_BRACKET, Some(']'))
  case object Colon extends Key(k.COLON, Some(':'))
  case object Comma extends Key(k.COMMA, Some(','))
  case object Equals extends Key(k.EQUALS, Some('='))
  case object ForwardSlash extends Key(k.SLASH, Some('/'))
  case object Grave extends Key(k.GRAVE, Some('`'))
  case object Minus extends Key(k.MINUS, Some('-'))
  case object Period extends Key(k.PERIOD, Some('.'))
  case object Pipe extends Key(-1, Some('|'))
  case object Plus extends Key(k.PLUS, Some('+'))
  case object Pound extends Key(k.POUND, Some('#'))
  case object Power extends Key(k.POWER, Some('^'))
  case object Question extends Key(-1, Some('?'))
  case object Quote extends Key(-1, Some('"'))
  case object SemiColon extends Key(k.SEMICOLON, Some(';'))
  case object Space extends Key(k.SPACE, Some(' '))
  case object Underscore extends Key(-1, Some('_'))

  case object Back extends Key(k.BACK)
  case object Backspace extends Key(k.BACKSPACE, Some(8.toChar))
  case object Call extends Key(k.CALL)
  case object Camera extends Key(k.CAMERA)
  case object Clear extends Key(k.CLEAR)
  case object Dash extends Key(-1, Some('-'))
  case object EndCall extends Key(k.ENDCALL)
  case object Enter extends Key(k.ENTER, Some('\r'))
  case object Envelope extends Key(k.ENVELOPE)
  case object Escape extends Key(k.ESCAPE)
  case object Explorer extends Key(k.EXPLORER)
  case object Focus extends Key(k.FOCUS)
  case object ForwardDel extends Key(k.FORWARD_DEL, Some(127.toChar))
  case object HeadsetHook extends Key(k.HEADSETHOOK)
  case object Home extends Key(k.HOME)
  case object MediaFastForward extends Key(k.MEDIA_FAST_FORWARD)
  case object MediaNext extends Key(k.MEDIA_NEXT)
  case object MediaPlayPause extends Key(k.MEDIA_PLAY_PAUSE)
  case object MediaPrevious extends Key(k.MEDIA_PREVIOUS)
  case object MediaRewind extends Key(k.MEDIA_REWIND)
  case object MediaStop extends Key(k.MEDIA_STOP)
  case object Menu extends Key(k.MENU)
  case object Mute extends Key(k.MUTE)
  case object Notification extends Key(k.NOTIFICATION)
  case object Num extends Key(k.NUM)
  case object PageUp extends Key(k.PAGE_UP)
  case object PageDown extends Key(k.PAGE_DOWN)
  case object Search extends Key(k.SEARCH)
  case object ShiftLeft extends Key(k.SHIFT_LEFT)
  case object ShiftRight extends Key(k.SHIFT_RIGHT)
  case object SoftLeft extends Key(k.SOFT_LEFT)
  case object SoftRight extends Key(k.SOFT_RIGHT)
  case object Sym extends Key(k.SYM)
  case object Tab extends Key(k.TAB, Some('\t'))
  case object VolumeDown extends Key(k.VOLUME_DOWN)
  case object VolumeUp extends Key(k.VOLUME_UP)
  case object ControlLeft extends Key(k.CONTROL_LEFT)
  case object ControlRight extends Key(k.CONTROL_RIGHT)
  case object End extends Key(k.END)
  case object Insert extends Key(k.INSERT)
  case object PictSymbols extends Key(k.PICTSYMBOLS)
  case object SwitchCharset extends Key(k.SWITCH_CHARSET)

  case object F1 extends Key(k.F1)
  case object F2 extends Key(k.F2)
  case object F3 extends Key(k.F3)
  case object F4 extends Key(k.F4)
  case object F5 extends Key(k.F5)
  case object F6 extends Key(k.F6)
  case object F7 extends Key(k.F7)
  case object F8 extends Key(k.F8)
  case object F9 extends Key(k.F9)
  case object F10 extends Key(k.F10)
  case object F11 extends Key(k.F11)
  case object F12 extends Key(k.F12)

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