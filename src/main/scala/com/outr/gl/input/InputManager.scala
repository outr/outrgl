package com.outr.gl.input

import com.badlogic.gdx.scenes.scene2d.{Event, EventListener, InputEvent, Stage}
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.log.Logging
import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
object InputManager {
  val current = Property[InputManager](default = None)
  private var map = Map.empty[Stage, InputManager]

  def set(stage: Stage) = current := apply(stage)

  def apply(stage: Stage) = synchronized {
    map.get(stage) match {
      case Some(im) => im
      case None => {
        val im = new InputManager(stage)
        map += stage -> im
        im
      }
    }
  }
}

class InputManager private(stage: Stage) extends Listenable with Logging {
  val keyDown = new UnitProcessor[KeyEvent]("keyDown")
  val keyUp = new UnitProcessor[KeyEvent]("keyUp")
  val keyTyped = new UnitProcessor[KeyEvent]("keyTyped")

  def simulate(key: Key) = {
    val event = KeyEvent(key)
    keyDown.fire(event)
    keyTyped.fire(event)
    keyUp.fire(event)
  }

  stage.addListener(new EventListener {
    override def handle(event: Event) = event match {
      case evt: InputEvent => {
        try {
          Key.byCode(evt.getKeyCode).orElse(Key.byChar(evt.getCharacter)) match {
            case Some(key) => {
              if (evt.getCharacter.toInt == 65535 || evt.getCharacter.toInt == 0 || key.character.nonEmpty) {
                evt.getType match {
                  case InputEvent.Type.keyDown => keyDown.fire(KeyEvent(key, Some(evt)))
                  case InputEvent.Type.keyUp => keyUp.fire(KeyEvent(key, Some(evt)))
                  case InputEvent.Type.keyTyped => keyTyped.fire(KeyEvent(key, Some(evt)))
                  case _ => //println(s"Key: $key, Type: ${evt.getType.name()}")
                }
              }
            }
            case None if evt.getKeyCode != 0 => info(s"No key mapping found for: ${evt.getKeyCode} (${evt.getCharacter})")
            case None => // Ignore
          }
        } catch {
          case t: Throwable => error(s"Error while handling InputEvent: ${evt.getKeyCode} (${evt.getCharacter})", t)
        }
        true
      }
    }
  })
}

case class KeyEvent(key: Key, cause: Option[InputEvent] = None)