package com.outr.gl

import com.badlogic.gdx.Input.Orientation

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Platform {
  var orientationOverride: Option[Orientation] = None
  def orientation(orientation: Orientation): Unit
}