package com.outr.gl.input

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d._
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.log.Logging
import org.powerscala.property.Property

import scala.annotation.tailrec

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

class InputManager private(val stage: Stage) extends Listenable with Logging {
  private[input] val vector = new Vector2
  private[input] val keyEvent = new KeyEventImpl(this)
  private[input] val mouseEvent = new MouseEventImpl(this)
  private[input] val scrollEvent = new ScrollEventImpl(this)
  private[input] val flingEvent = new FlingEventImpl(this)
  private[input] val panEvent = new PanEventImpl(this)
  private[input] val zoomEvent = new ZoomEventImpl(this)
  private[input] val pinchEvent = new PinchEventImpl(this)
  private[input] var _screenX: Int = _
  private[input] var _screenY: Int = _
  private[input] var _stageX: Float = _
  private[input] var _stageY: Float = _
  private[input] var _localX: Float = _
  private[input] var _localY: Float = _

  def screenX = _screenX
  def screenY = _screenY
  def stageX = _stageX
  def stageY = _stageY
  def localX = _localX
  def localY = _localY

  val focused = Property[Actor](default = None)
  var atCursor: Actor = _

  val keyDown = new UnitProcessor[KeyEvent]("keyDown")
  val keyUp = new UnitProcessor[KeyEvent]("keyUp")
  val keyTyped = new UnitProcessor[KeyEvent]("keyTyped")
  val mouseMoved = new UnitProcessor[MouseEvent]("mouseMoved")
  val touchDown = new UnitProcessor[MouseEvent]("touchDown")
  val touchDragged = new UnitProcessor[MouseEvent]("touchDragged")
  val touchUp = new UnitProcessor[MouseEvent]("touchUp")
  val scrolled = new UnitProcessor[ScrollEvent]("scrolled")
  val tapped = new UnitProcessor[MouseEvent]("tapped")
  val longPressed = new UnitProcessor[MouseEvent]("longPressed")
  val flung = new UnitProcessor[FlingEvent]("flung")
  val panned = new UnitProcessor[PanEvent]("panned")
  val panStopped = new UnitProcessor[MouseEvent]("panStopped")
  val zoomed = new UnitProcessor[ZoomEvent]("zoomed")
  val pinched = new UnitProcessor[PinchEvent]("pinched")

  val processor = new ScreenInputProcessor(this)

  def simulate(key: Key) = {
    keyEvent(key)

    keyDown.fire(keyEvent)
    keyTyped.fire(keyEvent)
    keyUp.fire(keyEvent)
  }

  @tailrec
  final def findTouchable(actor: Actor): Option[Actor] = {
    if (actor.isTouchable) {
      Some(actor)
    } else if (actor.getParent == null) {
      None
    } else {
      findTouchable(actor.getParent)
    }
  }
}

private class ScreenInputProcessor(manager: InputManager) extends InputProcessor with GestureListener {
  private val gestures = new GestureDetector(this)

  // TODO: fire events on EnhancedActor as well

  override def keyDown(keyCode: Int) = {
    val key = Key.byCode(keyCode).getOrElse(throw new RuntimeException(s"Unable to find by keyCode: $keyCode."))
    manager.keyEvent(key)

    manager.keyDown.fire(manager.keyEvent)

    gestures.keyDown(keyCode)

    true
  }

  override def keyUp(keyCode: Int) = {
    val key = Key.byCode(keyCode).getOrElse(throw new RuntimeException(s"Unable to find by keyCode: $keyCode."))
    manager.keyEvent(key)

    manager.keyUp.fire(manager.keyEvent)

    gestures.keyUp(keyCode)

    true
  }

  override def keyTyped(character: Char) = {
    val key = Key.byChar(character).getOrElse(throw new RuntimeException(s"Unable to find by character: $character."))
    manager.keyEvent(key)

    manager.keyTyped.fire(manager.keyEvent)

    gestures.keyTyped(character)

    true
  }

  private def updateCoordinates(screenX: Int, screenY: Int) = {
    manager._screenX = screenX
    manager._screenY = screenY
    manager.vector.set(screenX, screenY)
    manager.stage.screenToStageCoordinates(manager.vector)
    manager._stageX = manager.vector.x
    manager._stageY = manager.vector.y

    val actor = manager.stage.hit(manager.stageX, manager.stageY, true)
    manager.findTouchable(actor) match {
      case Some(a) => manager.atCursor = a
      case None => manager.atCursor = manager.stage.getRoot
    }

    manager.vector.set(screenX, screenY)
    manager.atCursor.screenToLocalCoordinates(manager.vector)
    manager._localX = manager.vector.x
    manager._localY = manager.vector.y
  }

  override def mouseMoved(screenX: Int, screenY: Int) = {
    updateCoordinates(screenX, screenY)
    manager.mouseEvent()
    manager.mouseMoved.fire(manager.mouseEvent)
    gestures.mouseMoved(screenX, screenY)
    true
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = {
    updateCoordinates(screenX, screenY)
    manager.mouseEvent(pointer, button)
    manager.touchDown.fire(manager.mouseEvent)
    gestures.touchDown(screenX, screenY, pointer, button)
    true
  }

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int) = {
    updateCoordinates(screenX, screenY)
    manager.mouseEvent(pointer)
    manager.touchDragged.fire(manager.mouseEvent)
    gestures.touchDragged(screenX, screenY, pointer)
    true
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = {
    updateCoordinates(screenX, screenY)
    manager.mouseEvent(pointer, button)
    manager.touchUp.fire(manager.mouseEvent)
    gestures.touchUp(screenX, screenY, pointer, button)
    true
  }

  override def scrolled(amount: Int) = {
    manager.scrollEvent(amount)
    manager.scrolled.fire(manager.scrollEvent)
    gestures.scrolled(amount)
    true
  }

  override def touchDown(x: Float, y: Float, pointer: Int, button: Int) = {
    true
  }

  override def longPress(x: Float, y: Float) = {
    manager.longPressed.fire(manager.mouseEvent)
    true
  }

  override def zoom(initialDistance: Float, distance: Float) = {
    manager.zoomed.fire(manager.zoomEvent(initialDistance, distance))
    true
  }

  override def pan(x: Float, y: Float, deltaX: Float, deltaY: Float) = {
    manager.panned.fire(manager.panEvent(deltaX, deltaY))
    true
  }

  override def tap(x: Float, y: Float, count: Int, button: Int) = {
    manager.tapped.fire(manager.mouseEvent)
    true
  }

  override def fling(velocityX: Float, velocityY: Float, button: Int) = {
    manager.flung.fire(manager.flingEvent(velocityX, velocityY, button))
    true
  }

  override def panStop(x: Float, y: Float, pointer: Int, button: Int) = {
    manager.panStopped.fire(manager.mouseEvent)
    true
  }

  override def pinch(initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2) = {
    manager.pinched.fire(manager.pinchEvent(initialPointer1, initialPointer2, pointer1, pointer2))
    true
  }
}

trait KeyEvent {
  def key: Key
  def focused: Option[Actor]
  def atCursor: Actor
}

class KeyEventImpl(manager: InputManager, var key: Key = null) extends KeyEvent {
  def focused = manager.focused.get
  def atCursor = manager.atCursor

  def apply(key: Key) = {
    this.key = key
    this
  }
}

trait PointerEvent {
  def manager: InputManager
  def screenX = manager.screenX
  def screenY = manager.screenY
  def stageX = manager.stageX
  def stageY = manager.stageY
  def localX = manager.localX
  def localY = manager.localY
  def actor = manager.atCursor
}

trait MouseEvent extends PointerEvent {
  def pointer: Int
  def button: Int
}

class MouseEventImpl(val manager: InputManager) extends MouseEvent {
  var pointer = -1
  var button = -1

  def apply(pointer: Int = 1, button: Int = -1) = {
    this.pointer = pointer
    this.button = button

    this
  }
}

trait ScrollEvent extends PointerEvent {
  def amount: Int
}

class ScrollEventImpl(val manager: InputManager) extends ScrollEvent {
  var amount = -1

  def apply(amount: Int) = {
    this.amount = amount

    this
  }
}

trait FlingEvent extends PointerEvent {
  def velocityX: Float
  def velocityY: Float
  def button: Int
}

class FlingEventImpl(val manager: InputManager) extends FlingEvent {
  var velocityX = 0.0f
  var velocityY = 0.0f
  var button = -1

  def apply(velocityX: Float, velocityY: Float, button: Int) = {
    this.velocityX = velocityX
    this.velocityY = velocityY
    this.button = button
    this
  }
}

trait PanEvent extends PointerEvent {
  def deltaX: Float
  def deltaY: Float
}

class PanEventImpl(val manager: InputManager) extends PanEvent {
  var deltaX = 0.0f
  var deltaY = 0.0f

  def apply(deltaX: Float, deltaY: Float) = {
    this.deltaX = deltaX
    this.deltaY = deltaY
    this
  }
}

trait ZoomEvent extends PointerEvent {
  def originalDistance: Float
  def currentDistance: Float
}

class ZoomEventImpl(val manager: InputManager) extends ZoomEvent {
  var originalDistance = 0.0f
  var currentDistance = 0.0f

  def apply(originalDistance: Float, currentDistance: Float) = {
    this.originalDistance = originalDistance
    this.currentDistance = currentDistance
    this
  }
}

trait PinchEvent extends PointerEvent {
  def initialFirstPointer: Vector2
  def initialSecondPointer: Vector2
  def firstPointer: Vector2
  def secondPointer: Vector2
}

class PinchEventImpl(val manager: InputManager) extends PinchEvent {
  var initialFirstPointer: Vector2 = _
  var initialSecondPointer: Vector2 = _
  var firstPointer: Vector2 = _
  var secondPointer: Vector2 = _

  def apply(initialFirstPointer: Vector2, initialSecondPointer: Vector2, firstPointer: Vector2, secondPointer: Vector2) = {
    this.initialFirstPointer = initialFirstPointer
    this.initialSecondPointer = initialSecondPointer
    this.firstPointer = firstPointer
    this.secondPointer = secondPointer
    this
  }
}