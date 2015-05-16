package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.event.Listenable
import org.powerscala.{Storage, Unique}
import org.powerscala.event.processor.UnitProcessor

import scala.language.implicitConversions

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object gl {
  var ErrorHandler: Throwable => Unit = (t: Throwable) => t.printStackTrace()
  var AutoAdjust = false

  var VirtualWidth = 1920.0f
  var VirtualHeight = 1080.0f

  var WidthOverride: Option[Float] = None
  var HeightOverride: Option[Float] = None

  def ActualWidth = WidthOverride.getOrElse(Gdx.graphics.getWidth.toFloat)
  def ActualHeight = HeightOverride.getOrElse(Gdx.graphics.getHeight.toFloat)

  private def modifier = ActualWidth / VirtualWidth

  def fontSize(originalSize: Int) = math.round(originalSize * modifier)

  def function(f: => Unit) = {
    val runnable = new Runnable {
      override def run() = f
    }
    val action = new RunnableAction
    action.setRunnable(runnable)
    action
  }

  def move(actor: Actor, x: Float, y: Float, duration: Float, interpolation: Interpolation) = {
    val action = new MoveToAction
    action.setX(x)
    action.setY(y)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def fade(actor: Actor, to: Float, duration: Float, interpolation: Interpolation) = {
    val action = new AlphaAction
    action.setAlpha(to)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def adjustedWide(value: Float) = value * (ActualWidth / VirtualWidth)
  def adjustedTall(value: Float) = value * (ActualHeight / VirtualHeight)

  implicit def actor2Adjusted[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen): AdjustedActor[A] = Storage.getOrSet(actor, "adjustedActor", new AdjustedActor[A](actor))

  class AdjustedActor[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen) extends Listenable {
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
      this.width(width).height(height)
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

    def center() = centerX().centerY()

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
}

class ActorWrapper[A <: Actor](val actor: A, padX: Float = 0.0f, padY: Float = 0.0f) extends Group {
  setX(actor.getX - padX)
  setY(actor.getY - padY)
  setWidth(actor.getWidth + (padX * 2.0f))
  setHeight(actor.getHeight + (padY * 2.0f))
  actor.setX(padX)
  actor.setY(padY)

  addActor(actor)
}