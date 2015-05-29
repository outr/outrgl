package com.outr.gl.jglfw

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.backends.jglfw.{JglfwApplication, JglfwApplicationConfiguration}
import com.outr.gl.Platform

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait JGLFWPlatform extends App with Platform[JglfwApplicationConfiguration] {
  def title = getClass.getSimpleName

  val config = new JglfwApplicationConfiguration

  init(config)

  val jglfw = new JglfwApplication(application, config)

  def init(config: JglfwApplicationConfiguration): Unit

  override def orientation(orientation: Orientation) = {
    val max = math.max(config.width, config.height)
    val min = math.min(config.width, config.height)
    orientation match {
      case Orientation.Portrait => jglfw.getGraphics.setDisplayMode(min, max, false)
      case Orientation.Landscape => jglfw.getGraphics.setDisplayMode(max, min, false)
    }
    ()
  }
}