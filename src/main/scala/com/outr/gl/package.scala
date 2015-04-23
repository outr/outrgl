package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object gl {
  var VirtualWidth = 1920.0f
  var VirtualHeight = 1080.0f

  private def modifier = Gdx.graphics.getWidth / VirtualWidth

  def fontSize(originalSize: Int) = math.round(originalSize * modifier)

  def function(f: => Unit) = {
    val runnable = new Runnable {
      override def run() = f
    }
    val action = new RunnableAction
    action.setRunnable(runnable)
    action
  }

  def moveTo(actor: Actor, x: Float, y: Float, duration: Float, interpolation: Interpolation) = {
    val action = new MoveToAction
    action.setX(x)
    action.setY(y)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def fadeTo(to: Float, actor: Actor, duration: Float, interpolation: Interpolation) = {
    val action = new AlphaAction
    action.setAlpha(to)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def adjustedWide(value: Float) = value * (Gdx.graphics.getWidth.toFloat / VirtualWidth)
  def adjustedTall(value: Float) = value * (Gdx.graphics.getHeight.toFloat / VirtualHeight)

  implicit class AdjustedActor[A <: Actor](actor: A) {
    def positioned(x: Float, y: Float, offsetFromHeight: Boolean = true) = this.x(x).y(y, offsetFromHeight)

    def x(x: Float) = {
      actor.setX(adjustedWide(x))
      actor
    }

    def y(y: Float, offsetFromHeight: Boolean = true) = {
      if (offsetFromHeight) {
        actor.setY(Gdx.graphics.getHeight - adjustedTall(y) - actor.getHeight)
      } else {
        actor.setY(adjustedTall(y))
      }
      actor
    }

    def right(x: Float) = {
      actor.setX(adjustedWide(x) - actor.getWidth)
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

    def sized(width: Float, height: Float) = {
      actor.setSize(adjustedWide(width), adjustedTall(height))
      actor
    }

    def width(width: Float = VirtualWidth) = {
      actor.setWidth(adjustedWide(width))
      actor
    }

    def height(height: Float = VirtualHeight) = {
      actor.setHeight(adjustedTall(height))
      actor
    }

    def centerX() = {
      actor.setX((Gdx.graphics.getWidth.toFloat / 2.0f) - (actor.getWidth / 2.0f))
      actor
    }

    def centerY() = {
      actor.setY((Gdx.graphics.getHeight.toFloat / 2.0f) - (actor.getHeight / 2.0f))
      actor
    }

    def center() = centerX().centerY()
  }
}
