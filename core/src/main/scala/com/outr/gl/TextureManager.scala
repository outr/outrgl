package com.outr.gl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.{Texture, Pixmap, Color}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TextureManager {
  lazy val Pixel = new Texture(TextureManager.createPixelMap(Color.WHITE, 1, 1))

  protected def classpath(path: String, useMipMaps: Boolean = true) = {
    load(Gdx.files.classpath(path))
  }

  protected def internal(path: String, useMipMaps: Boolean = true) = {
    load(Gdx.files.internal(path), useMipMaps)
  }

  protected def load(file: FileHandle, useMipMaps: Boolean = true) = {
    val t = new Texture(file, useMipMaps)
    t.setFilter(if (useMipMaps) TextureFilter.MipMapLinearLinear else TextureFilter.Linear, TextureFilter.Linear)
    t
  }
}

object TextureManager {
  lazy val BlankCursor = createPixelMap(Color.CLEAR, 32, 32)

  private def createPixelMap(color: Color, width: Int, height: Int) = {
    val pixmap = new Pixmap(width, height, Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fillRectangle(0, 0, width, height)
    pixmap
  }
}