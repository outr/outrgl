package com.outr.gl

import java.util.concurrent.ConcurrentHashMap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.{Texture, Pixmap, Color}
import com.outr.gl.screen.AbstractBaseScreen
import com.outr.gl.task.TaskManager
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TextureManager extends Listenable {
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

  private var loading = Set.empty[String]
  private var _textures = Map.empty[String, Texture]
  val loaded = new UnitProcessor[Loaded]("loaded")
  def textures = _textures

  def loadRemote(screen: AbstractBaseScreen, taskManager: TaskManager, url: String)(f: Texture => Unit) = synchronized {
    if (loading.contains(url)) {              // Currently loading the resource
      loaded.on { evt =>
        screen.nextRender {
          f(evt.texture)
        }
      }
    } else if (textures.contains(url)) {      // Texture is already loaded
      screen.nextRender {
        f(textures(url))
      }
    } else {
      loading += url
      try {
        val filename = url.substring(url.indexOf('/', 7) + 1)
        val local = Gdx.files.local(filename)
        val future = taskManager.download(url, local)
        future.andThen { handle =>
          screen.nextRender {
            val useMipMaps = true
            val texture = new Texture(handle, useMipMaps)
            f(texture)
            TextureManager.this.synchronized {
              _textures += url -> texture
            }
            loaded.fire(Loaded(url, texture))
          }
        }
      } finally {
        loading -= url
      }
    }
  }
}

case class Loaded(url: String, texture: Texture)

object TextureManager {
  lazy val BlankCursor = createPixelMap(Color.CLEAR, 32, 32)

  def createPixelMap(color: Color, width: Int, height: Int) = {
    val pixmap = new Pixmap(width, height, Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fillRectangle(0, 0, width, height)
    pixmap
  }
}