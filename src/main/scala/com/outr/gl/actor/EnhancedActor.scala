package com.outr.gl.actor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d._
import com.outr.gl.input._
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.Unique
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class EnhancedActor[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen) extends Listenable {
  lazy val keyDown = new UnitProcessor[KeyEvent]("keyDown")
  lazy val keyUp = new UnitProcessor[KeyEvent]("keyUp")
  lazy val keyTyped = new UnitProcessor[KeyEvent]("keyTyped")
  lazy val mouseMoved = new UnitProcessor[MouseEvent]("mouseMoved")
  lazy val touchDown = new UnitProcessor[MouseEvent]("touchDown")
  lazy val touchDragged = new UnitProcessor[MouseEvent]("touchDragged")
  lazy val touchUp = new UnitProcessor[MouseEvent]("touchUp")
  lazy val scrolled = new UnitProcessor[ScrollEvent]("scrolled")
  lazy val tapped = new UnitProcessor[MouseEvent]("tapped")
  lazy val longPressed = new UnitProcessor[MouseEvent]("longPressed")
  lazy val flung = new UnitProcessor[FlingEvent]("flung")
  lazy val panned = new UnitProcessor[PanEvent]("panned")
  lazy val panStopped = new UnitProcessor[MouseEvent]("panStopped")
  lazy val zoomed = new UnitProcessor[ZoomEvent]("zoomed")
  lazy val pinched = new UnitProcessor[PinchEvent]("pinched")

  // Default to only passing events down
  actor.setTouchable(Touchable.childrenOnly)

  if (actor.getName == null) {
    actor.setName(Unique())
  }

  def id = actor.getName

  def positioned(x: Float, y: Float, offsetFromHeight: Boolean = true) = this.x(x).y(y, offsetFromHeight)

  def x(x: Float) = {
    screen.onResize(s"$id:x", () => actor.setX(adjustedWide(x)))
    actor
  }

  def y(y: Float, offsetFromHeight: Boolean = true) = {
    screen.onResize(s"$id:y", () => {
      if (offsetFromHeight) {
        actor.setY(ActualHeight - adjustedTall(y) - actor.getHeight)
      } else {
        actor.setY(adjustedTall(y))
      }
    })
    actor
  }

  def right(x: Float) = {
    screen.onResize(s"$id:x", () => actor.setX(adjustedWide(x) - actor.getWidth))
    actor
  }

  def alpha(a: Float) = {
    actor.getColor.a = a
    actor
  }

  def color(color: Color) = {
    actor.setColor(color)
    actor
  }

  def visible = {
    actor.setVisible(true)
    actor
  }

  def invisible = {
    actor.setVisible(false)
    actor
  }

  def sized(width: Float, height: Float) = {
    this.width(width)
    this.height(height)
    actor
  }

  def width(width: Float = VirtualWidth) = {
    screen.onResize(s"$id:width", () => actor.setWidth(adjustedWide(width)))
    actor
  }

  def height(height: Float = VirtualHeight) = {
    screen.onResize(s"$id:height", () => actor.setHeight(adjustedTall(height)))
    actor
  }

  def centerX() = {
    screen.onResize(s"$id:x", () => actor.setX((ActualWidth / 2.0f) - (actor.getWidth / 2.0f)))
    actor
  }

  def centerY() = {
    screen.onResize(s"$id:y", () => actor.setY((ActualHeight / 2.0f) - (actor.getHeight / 2.0f)))
    actor
  }

  def center() = {
    this.centerX()
    this.centerY()
    actor
  }

  def wrapper(padX: Float = 0.0f, padY: Float = 0.0f) = new ActorWrapper[A](actor, padX, padY)

  def onTouch(f: => Unit) = {
    actor.setTouchable(Touchable.enabled)

    tapped.on {
      case evt => try {
        f
      } catch {
        case t: Throwable => ErrorHandler(t)
      }
    }

    actor
  }

  def onSwipeRight(f: => Unit) = {
    actor.setTouchable(Touchable.enabled)

    flung.on {
      case evt => if (evt.velocityX > 50.0f && evt.velocityX > math.abs(evt.velocityY)) {
        try {
          f
        } catch {
          case t: Throwable => ErrorHandler(t)
        }
      }
    }

    actor
  }

  def onSwipeLeft(f: => Unit) = {
    actor.setTouchable(Touchable.enabled)

    flung.on {
      case evt => if (evt.velocityX < -50.0f && math.abs(evt.velocityX) > math.abs(evt.velocityY)) {
        try {
          f
        } catch {
          case t: Throwable => ErrorHandler(t)
        }
      }
    }

    actor
  }
}