package com.outr.gl

import com.badlogic.gdx.Input.Orientation
import com.outr.gl.task.TaskManager

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FontManager {
  def taskManager: TaskManager

  class OrientedFonts(orientation: Orientation) {
    class FontFamily(name: String) {
      class FontStyle(style: String) {
        def font(size: Int) = taskManager.font(name, style, size, Some(orientation))
      }
    }
  }
}
