package com.outr.gl

import com.badlogic.gdx.Input.Orientation
import com.outr.gl.screen.MultiScreenApplication

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Platform[Config] {
  def platformId: String
  def application: MultiScreenApplication
  def orientation(orientation: Orientation): Unit
  def init(config: Config): Unit
}