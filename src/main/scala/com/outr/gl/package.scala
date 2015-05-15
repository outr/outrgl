package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.Unique

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

  implicit class AdjustedActor[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen) {
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

    def onTouch(f: => Unit) = {
      actor.addListener(new EventListener {
        private var dragStartX = 0.0f
        private var dragStartY = 0.0f
        private var dragAdjust = 0.0f

        override def handle(event: Event) = event match {
          case e: InputEvent => {
            e.getType match {
              case InputEvent.Type.touchDown => {
                dragStartX = e.getStageX
                dragStartY = e.getStageY
                dragAdjust = 0.0f
              }
              case InputEvent.Type.touchDragged => {
                val adjustX = math.abs(dragStartX - e.getStageX)
                val adjustY = math.abs(dragStartY - e.getStageY)
                dragAdjust += adjustX
                dragAdjust += adjustY
              }
              case InputEvent.Type.touchUp if dragAdjust <= 12.0f => try {
                f
              } catch {
                case t: Throwable => ErrorHandler(t)
              }
              case _ => // Ignore everything else
            }
            true
          }
          case _ => false // Ignore other events
        }
      })
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