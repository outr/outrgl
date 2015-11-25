package com.outr.gl.screen

import java.util.concurrent.atomic.AtomicBoolean

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.{Action, Actor, Stage}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.outr.gl._
import com.outr.gl.input.{InputManager, Key}
import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait AbstractBaseScreen extends Screen {
  implicit def abstractBaseScreen: AbstractBaseScreen = this

  val cursor = Property[Pixmap](default = Some(null))

  def stage: Stage
  def app: MultiScreenApplication

  def orientation = app.defaultOrientation
  def portraitScreen: AbstractBaseScreen = this
  def landscapeScreen: AbstractBaseScreen = this

  lazy val input = InputManager(this)

  private val initialized = new AtomicBoolean

  private var updates = Map.empty[String, () => Any]

  def onResize(key: String, f: () => Any) = synchronized {
    if (AutoAdjust) {
      updates += key -> f
    }
    f()
  }

  def onTouch(actors: Actor*)(f: => Unit) = {
    actors.foreach {
      case a => a.onTouch(f)
    }
  }

  def onEnter(actors: Actor*)(f: => Unit) = {
    actors.foreach {
      case a => a.keyTyped.on {
        case evt => if (evt.key.is(Key.Enter)) f
      }
    }
  }

  def nextRender(f: => Unit) = Gdx.app.postRunnable(new Runnable {
    override def run(): Unit = try {
      f
    } catch {
      case t: Throwable => MultiScreenApplication.handleException(t)
    }
  })

  private var _transitioning = false
  def transitioning = _transitioning

  def init(): Unit

  private def initInternal() = {
    val application = MultiScreenApplication()
    application.orientation.change.on {
      case evt => if (application.screens.contains(this)) {
        val newScreen = evt.newValue match {
          case Orientation.Portrait => portraitScreen
          case Orientation.Landscape => landscapeScreen
        }
        if (newScreen != this) {
          application.removeScreen(this)
          application.addScreen(newScreen)
        }
      }
    }
    input.keyDown.on {
      case evt => if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
        if (evt.key == Key.P) {
          application.orientationOverride = Some(Orientation.Portrait)
        } else if (evt.key == Key.L) {
          application.orientationOverride = Some(Orientation.Landscape)
        }
      }
    }
  }

  override def show() = {
    if (initialized.compareAndSet(false, true)) {
      init()
      initInternal()
    }
    app.platform.orientation(orientation)
    Gdx.input.setInputProcessor(input.processor)
    InputManager.set(this)
//    Gdx.input.setCursorImage(cursor(), 0, 0)      // TODO: determine if this is needed
  }

  override def resize(width: Int, height: Int) = {
    val centerCamera = true
    stage.getViewport.update(width, height, centerCamera)
    stage.getRoot.setWidth(width.toFloat)
    stage.getRoot.setHeight(height.toFloat)

    updates.values.foreach {
      case f => f()
    }
  }

  override def render(delta: Float) = try {
    stage.act(delta)
    stage.getBatch.enableBlending()
    stage.getBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f)
    stage.draw()
  } catch {
    case t: Throwable => {
      MultiScreenApplication.handleException(t)
      MultiScreenApplication().dispose()
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

  def transitionCrossFade(screen: BaseScreen, time: Float = 0.25f, interpolation: Interpolation = Interpolation.linear) = {
    screen.stage.getRoot.getColor.a = 0.0f
    MultiScreenApplication().addScreen(screen)
    transitionToScreen(
      screen,
      fade(stage.getRoot, 0.0f, time, interpolation),
      fade(screen.stage.getRoot, 1.0f, time, interpolation)
    )
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