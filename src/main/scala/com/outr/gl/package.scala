package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}
import com.outr.gl.actor.EnhancedActor
import com.outr.gl.screen.{MultiScreenApplication, AbstractBaseScreen}
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

  var VirtualPortraitWide = 1920.0f
  var VirtualPortraitTall = 1080.0f

  def VirtualWidth(implicit screen: AbstractBaseScreen) = oriented(screen, VirtualPortraitWide, VirtualPortraitTall)
  def VirtualHeight(implicit screen: AbstractBaseScreen) = oriented(screen, VirtualPortraitTall, VirtualPortraitWide)

  var ActualPortraitWidthOverride: Option[Float] = None
  var ActualPortraitHeightOverride: Option[Float] = None

  def ActualWidthOverride(implicit screen: AbstractBaseScreen) = oriented(screen, ActualPortraitWidthOverride, ActualPortraitHeightOverride)
  def ActualHeightOverride(implicit screen: AbstractBaseScreen) = oriented(screen, ActualPortraitHeightOverride, ActualPortraitWidthOverride)

  def ActualMax = math.max(Gdx.graphics.getWidth, Gdx.graphics.getHeight).toFloat
  def ActualMin = math.min(Gdx.graphics.getWidth, Gdx.graphics.getHeight).toFloat

  def ActualWidth(implicit screen: AbstractBaseScreen) = ActualWidthOverride(screen).getOrElse(oriented(screen, ActualMin, ActualMax))
  def ActualHeight(implicit screen: AbstractBaseScreen) = ActualHeightOverride(screen).getOrElse(oriented(screen, ActualMax, ActualMin))

  private def modifier(orientation: Orientation) = orientation match {
    case Orientation.Portrait => ActualMin / VirtualPortraitWide
    case Orientation.Landscape => ActualMax / VirtualPortraitTall
  }

  def fontSize(originalSize: Int, orientation: Orientation) = math.round(originalSize * modifier(orientation))

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

  def oriented[T](screen: AbstractBaseScreen, portrait: T, landscape: T, orientation: Option[Orientation] = None) = orientation.getOrElse(screen.orientation) match {
    case Orientation.Portrait => portrait
    case Orientation.Landscape => landscape
  }

  def adjustedWide(screen: AbstractBaseScreen, value: Float) = oriented(screen, value * (ActualWidth(screen) / VirtualWidth(screen)), value * (ActualHeight(screen) / VirtualHeight(screen)))
  def adjustedTall(screen: AbstractBaseScreen, value: Float) = oriented(screen, value * (ActualHeight(screen) / VirtualHeight(screen)), value * (ActualWidth(screen) / VirtualWidth(screen)))

  implicit def actor2Enhanced[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen): EnhancedActor[A] = {
    Storage.getOrSet(actor, "adjustedActor", new EnhancedActor[A](actor))
  }
}