package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}
import com.outr.gl.actor.EnhancedActor
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

  implicit def actor2Enhanced[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen): EnhancedActor[A] = {
    Storage.getOrSet(actor, "adjustedActor", new EnhancedActor[A](actor))
  }
}