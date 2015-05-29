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

  private var VirtualPortraitWide = 1920.0f
  private var VirtualPortraitTall = 1080.0f
  private var ActualPortraitWidthOverride: Option[Float] = None
  private var ActualPortraitHeightOverride: Option[Float] = None

  def virtualPortrait(wide: Float, tall: Float, actualWideOverride: Option[Float] = None, actualTallOverride: Option[Float] = None) = {
    VirtualPortraitWide = wide
    VirtualPortraitTall = tall
    ActualPortraitWidthOverride = actualWideOverride
    ActualPortraitHeightOverride = actualTallOverride
  }

  def virtualLandscape(wide: Float, tall: Float, actualWideOverride: Option[Float] = None, actualTallOverride: Option[Float] = None) = {
    VirtualPortraitWide = tall
    VirtualPortraitTall = wide
    ActualPortraitWidthOverride = actualTallOverride
    ActualPortraitHeightOverride = actualWideOverride
  }

  def VirtualWidth(implicit screen: AbstractBaseScreen) = oriented(screen, VirtualPortraitWide, VirtualPortraitTall)
  def VirtualHeight(implicit screen: AbstractBaseScreen) = oriented(screen, VirtualPortraitTall, VirtualPortraitWide)

  def ActualWidthOverride(implicit screen: AbstractBaseScreen) = oriented(screen, ActualPortraitWidthOverride, ActualPortraitHeightOverride)
  def ActualHeightOverride(implicit screen: AbstractBaseScreen) = oriented(screen, ActualPortraitHeightOverride, ActualPortraitWidthOverride)

  def ActualMax = math.max(Gdx.graphics.getWidth, Gdx.graphics.getHeight).toFloat
  def ActualMin = math.min(Gdx.graphics.getWidth, Gdx.graphics.getHeight).toFloat

  def ActualWidth(implicit screen: AbstractBaseScreen) = ActualWidthOverride(screen).getOrElse(oriented(screen, ActualMin, ActualMax))
  def ActualHeight(implicit screen: AbstractBaseScreen) = ActualHeightOverride(screen).getOrElse(oriented(screen, ActualMax, ActualMin))

  private def modifierWide(orientation: Orientation) = orientation match {
    case Orientation.Portrait => ActualPortraitWidthOverride.getOrElse(ActualMin) / VirtualPortraitWide
    case Orientation.Landscape => ActualPortraitHeightOverride.getOrElse(ActualMax) / VirtualPortraitTall
  }
  private def modifierTall(orientation: Orientation) = orientation match {
    case Orientation.Portrait => ActualPortraitHeightOverride.getOrElse(ActualMax) / VirtualPortraitTall
    case Orientation.Landscape => ActualPortraitWidthOverride.getOrElse(ActualMin) / VirtualPortraitWide
  }

  def fontSize(originalSize: Int, orientation: Orientation) = math.round(originalSize * modifierWide(orientation))

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

  def adjustedWide(orientation: Orientation, value: Float) = value * modifierWide(orientation)
  def adjustedTall(orientation: Orientation, value: Float) = value * modifierTall(orientation)

  implicit def actor2Enhanced[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen): EnhancedActor[A] = {
    Storage.getOrSet(actor, "adjustedActor", new EnhancedActor[A](actor))
  }
}