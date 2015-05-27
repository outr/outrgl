package com.outr.gl.desktop

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import com.outr.gl.Platform

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait DesktopPlatform extends App with Platform[LwjglApplicationConfiguration] {
  def title = getClass.getSimpleName

  val config = new LwjglApplicationConfiguration

  init(config)

  val lwjgl = new LwjglApplication(application, config)

  def init(config: LwjglApplicationConfiguration): Unit

  override def orientation(orientation: Orientation) = {
    val max = math.max(config.width, config.height)
    val min = math.min(config.width, config.height)
    orientation match {
      case Orientation.Portrait => lwjgl.getGraphics.setDisplayMode(min, max, false)
      case Orientation.Landscape => lwjgl.getGraphics.setDisplayMode(max, min, false)
    }
    ()
  }
}