package com.outr.gl.actor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.{Event, EventListener, InputEvent, Actor}
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.Unique
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class EnhancedActor[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen) extends Listenable {
  val touchDown = new UnitProcessor[InputEvent]("touchDown")
  val touchDragged = new UnitProcessor[InputEvent]("touchDragged")
  val touchUp = new UnitProcessor[InputEvent]("touchUp")

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

  private lazy val receiveEvents = {
    actor.addListener(new EventListener {
      override def handle(event: Event) = event match {
        case e: InputEvent => {
          e.getType match {
            case InputEvent.Type.touchDown => touchDown.fire(e)
            case InputEvent.Type.touchDragged => touchDragged.fire(e)
            case InputEvent.Type.touchUp => touchUp.fire(e)
            case _ => // Ignore other types
          }
          true
        }
        case _ => false // Ignore non-input events
      }
    })
  }

  def onTouch(f: => Unit) = {
    receiveEvents               // Initialize events on this

    var dragStartX = 0.0f
    var dragStartY = 0.0f
    var dragAdjust = 0.0f

    touchDown.on {
      case evt => {
        dragStartX = evt.getStageX
        dragStartY = evt.getStageY
        dragAdjust = 0.0f
      }
    }
    touchDragged.on {
      case evt => {
        val adjustX = math.abs(dragStartX - evt.getStageX)
        val adjustY = math.abs(dragStartY - evt.getStageY)
        dragAdjust += adjustX
        dragAdjust += adjustY
      }
    }
    touchUp.on {
      case evt => if (dragAdjust <= 12.0f) try {
        f
      } catch {
        case t: Throwable => ErrorHandler(t)
      }
    }
    actor
  }

  def onSwipeRight(f: => Unit) = {
    receiveEvents               // Initialize events on this

    var dragStartX = 0.0f
    var dragAdjust = 0.0f

    touchDown.on {
      case evt => {
        dragStartX = evt.getStageX
        dragAdjust = 0.0f
      }
    }
    touchDragged.on {
      case evt => {
        val adjustX = math.abs(dragStartX - evt.getStageX)
        dragAdjust += adjustX
      }
    }
    touchUp.on {
      case evt => if (dragAdjust >= 20.0f) try {
        f
      } catch {
        case t: Throwable => ErrorHandler(t)
      }
    }
    actor
  }

  def onSwipeLeft(f: => Unit) = {
    receiveEvents               // Initialize events on this

    var dragStartX = 0.0f
    var dragAdjust = 0.0f

    touchDown.on {
      case evt => {
        dragStartX = evt.getStageX
        dragAdjust = 0.0f
      }
    }
    touchDragged.on {
      case evt => {
        val adjustX = math.abs(dragStartX - evt.getStageX)
        dragAdjust += adjustX
      }
    }
    touchUp.on {
      case evt => if (dragAdjust <= -20.0f) try {
        f
      } catch {
        case t: Throwable => ErrorHandler(t)
      }
    }
    actor
  }
}