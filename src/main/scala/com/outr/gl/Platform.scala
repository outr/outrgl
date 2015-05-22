package com.outr.gl

import com.badlogic.gdx.Input.Orientation

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Platform {
  def orientation: Orientation
  def orientation(orientation: Orientation): Unit
}