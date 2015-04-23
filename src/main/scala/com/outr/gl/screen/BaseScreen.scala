package com.outr.gl.screen

import java.util.concurrent.atomic.AtomicBoolean

import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.actions.Actions._
import com.badlogic.gdx.{Gdx, Screen}
import com.badlogic.gdx.scenes.scene2d.{Action, Stage}
import com.badlogic.gdx.utils.viewport.ScreenViewport

import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class BaseScreen extends Screen {
  lazy val stage = new Stage(new ScreenViewport)
  var cached = false

  private val initialized = new AtomicBoolean
  private var cachedBuffer: FrameBuffer = _
  private var cachedSprite: Sprite = _

  def init(): Unit

  override def show() = {
    if (initialized.compareAndSet(false, true)) {
      init()
    }
    Gdx.input.setInputProcessor(stage)
  }

  override def resize(width: Int, height: Int) = {
    val centerCamera = true
    stage.getViewport.update(width, height, centerCamera)
    stage.getRoot.setWidth(width.toFloat)
    stage.getRoot.setHeight(height.toFloat)
  }

  override def render(delta: Float) = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    val createSprite = cached && cachedSprite == null
    if (cached && cachedSprite != null) {   // Cached sprite already exists, lets render it instead of drawing
      stage.getBatch.begin()
      cachedSprite.draw(stage.getBatch)
      stage.getBatch.end()
    } else if (createSprite) {              // We need to create the sprite in this render
    val hasDepth = false
      cachedBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth, Gdx.graphics.getHeight, hasDepth)
      cachedBuffer.begin()
    } else if (cachedSprite != null) {      // Not cached, so kill the sprite
      cachedSprite.getTexture.dispose()
      cachedSprite = null
    }
    if (!cached || createSprite) {          // Non-cached rendering
      stage.act(delta)
      stage.getBatch.enableBlending()
      stage.getBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f)
      stage.draw()
    }
    if (createSprite) {
      cachedBuffer.end()
      cachedSprite = new Sprite(cachedBuffer.getColorBufferTexture)
      cachedSprite.flip(false, true)
      cachedSprite.setPosition(0, 0)
    }
  }

  override def pause() = {}

  override def resume() = {}

  override def hide() = {}

  override def dispose() = {}

  def addAction(action: Action) = {
    stage.getRoot.addAction(action)
  }

  def removeAction(action: Action) = {
    stage.getRoot.removeAction(action)
  }

  def fadeToScreen(screen: Screen, time: Float = 2.0f) = addAction(sequence(fadeOut(time), function {
    MultiScreenApplication().setScreen(screen)
  }))
}