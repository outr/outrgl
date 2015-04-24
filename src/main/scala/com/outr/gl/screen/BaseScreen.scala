package com.outr.gl.screen

import java.util.concurrent.atomic.AtomicBoolean

import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Interpolation
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

  private val initialized = new AtomicBoolean

  private var _transitioning = false
  def transitioning = _transitioning

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
    stage.act(delta)
    stage.getBatch.enableBlending()
    stage.getBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f)
    stage.draw()
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

  def transitionPushLeft(screen: BaseScreen, time: Float = 0.25f, interpolation: Interpolation = Interpolation.linear) = {
    screen.stage.getRoot.setX(Gdx.graphics.getWidth)
    MultiScreenApplication().addScreen(screen)
    transitionToScreen(
      screen,
      move(stage.getRoot, -Gdx.graphics.getWidth, 0.0f, time, interpolation),
      move(screen.stage.getRoot, 0.0f, 0.0f, time, interpolation)
    )
  }

  def transitionPushRight(screen: BaseScreen, time: Float = 0.25f, interpolation: Interpolation = Interpolation.linear) = {
    screen.stage.getRoot.setX(-Gdx.graphics.getWidth)
    MultiScreenApplication().addScreen(screen)
    transitionToScreen(
      screen,
      move(stage.getRoot, Gdx.graphics.getWidth, 0.0f, time, interpolation),
      move(screen.stage.getRoot, 0.0f, 0.0f, time, interpolation)
    )
  }

  def transitionSlideOverLeft(screen: BaseScreen, time: Float = 0.25f, interpolation: Interpolation = Interpolation.linear) = {
    screen.stage.getRoot.setX(Gdx.graphics.getWidth)
    MultiScreenApplication().addScreen(screen)
    transitionToScreen(
      screen,
      move(screen.stage.getRoot, 0.0f, 0.0f, time, interpolation)
    )
  }

  def transitionSlideOutRight(screen: BaseScreen, time: Float = 0.25f, interpolation: Interpolation = Interpolation.linear) = {
    screen.stage.getRoot.setX(0.0f)
    MultiScreenApplication().insertScreen(0, screen)
    transitionToScreen(
      screen,
      move(stage.getRoot, Gdx.graphics.getWidth, 0.0f, time, interpolation)
    )
  }

  def transitionToScreen(screen: BaseScreen, actions: Action*) = if (!transitioning) {
    _transitioning = true
    addAction(sequence(
      parallel(actions: _*),
      function {
        MultiScreenApplication().removeScreen(this)
        _transitioning = false
      }
    ))
  }
}