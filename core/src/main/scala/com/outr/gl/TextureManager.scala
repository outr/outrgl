package com.outr.gl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{Batch, Sprite}
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
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

  def createGradient(width: Int, height: Int, topLeft: Color, topRight: Color, bottomLeft: Color, bottomRight: Color) = {
    val drawable = new GradientSpriteDrawable(this)
    drawable.topLeft = topLeft
    drawable.topRight = topRight
    drawable.bottomLeft = bottomLeft
    drawable.bottomRight = bottomRight
    drawable.setMinWidth(width)
    drawable.setMinHeight(height)
    val image = new Image(drawable)
    image.setWidth(width)
    image.setHeight(height)
    image
  }

  private var circleTextures = Map.empty[Int, Texture]

  def createCircle(radius: Int) = synchronized {
    val texture = circleTextures.get(radius) match {
      case Some(t) => t
      case None => {
        val size = radius * 2
        val pixmap = new Pixmap(size, size, Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(radius, radius, radius)
        val t = new Texture(pixmap)
        circleTextures += radius -> t
        t
      }
    }
    new Image(texture)
  }
}

class RoundedGroup(background: Color, radius: Int, manager: TextureManager)(implicit screen: AbstractBaseScreen) extends Group {
  private val circleTopLeft = manager.createCircle(radius).color(background)
  private val circleTopRight = manager.createCircle(radius).color(background)
  private val circleBottomLeft = manager.createCircle(radius).color(background)
  private val circleBottomRight = manager.createCircle(radius).color(background)
  private val backgroundImage1 = new Image(manager.Pixel).color(background)
  private val backgroundImage2 = new Image(manager.Pixel).color(background)

  addActor(backgroundImage1)
  addActor(backgroundImage2)
  addActor(circleTopLeft)
  addActor(circleTopRight)
  addActor(circleBottomLeft)
  addActor(circleBottomRight)

  override def setX(x: Float): Unit = {
    super.setX(x)

    updateBackground()
  }

  override def setY(y: Float): Unit = {
    super.setY(y)

    updateBackground()
  }

  override def setWidth(width: Float): Unit = {
    super.setWidth(width)

    updateBackground()
  }

  override def setHeight(height: Float): Unit = {
    super.setHeight(height)

    updateBackground()
  }

  def updateBackground() = {
    circleTopLeft.setPosition(-radius, getHeight - radius)
    circleTopRight.setPosition(getWidth - radius, getHeight - radius)
    circleBottomLeft.setPosition(-radius, -radius)
    circleBottomRight.setPosition(getWidth - radius, -radius)
    backgroundImage1.setPosition(-radius, 0)
    backgroundImage1.setSize(getWidth + (radius * 2), getHeight)
    backgroundImage2.setPosition(0, -radius)
    backgroundImage2.setSize(getWidth, getHeight + (radius * 2))
  }
}

case class Loaded(url: String, texture: Texture)

class GradientSpriteDrawable(manager: TextureManager) extends SpriteDrawable(new Sprite(manager.Pixel)) {
  val C1 = 2
  val C2 = 7
  val C3 = 12
  val C4 = 17

  var topLeft: Color = _
  var topRight: Color = _
  var bottomLeft: Color = _
  var bottomRight: Color = _

  private val tmpColor = new Color()

  override def draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float): Unit = {
    val sprite = getSprite
    sprite.setOrigin(originX, originY)
    sprite.setRotation(rotation)
    sprite.setScale(scaleX, scaleY)
    sprite.setBounds(x, y, width, height)

    val vertices = sprite.getVertices
    val batchColor = batch.getColor
    vertices(C1) = tmpColor.set(bottomLeft).mul(batchColor).toFloatBits
    vertices(C2) = tmpColor.set(topLeft).mul(batchColor).toFloatBits
    vertices(C3) = tmpColor.set(topRight).mul(batchColor).toFloatBits
    vertices(C4) = tmpColor.set(bottomRight).mul(batchColor).toFloatBits

    sprite.draw(batch)
  }
}

object TextureManager {
  lazy val BlankCursor = createPixelMap(Color.CLEAR, 32, 32)

  def createPixelMap(color: Color, width: Int, height: Int) = {
    val pixmap = new Pixmap(width, height, Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fillRectangle(0, 0, width, height)
    pixmap
  }
}