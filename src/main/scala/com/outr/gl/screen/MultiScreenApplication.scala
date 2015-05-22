package com.outr.gl.screen

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{ApplicationListener, Gdx, Screen}
import com.outr.gl.Platform
import org.powerscala.property.Property

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class MultiScreenApplication extends ApplicationListener {
  MultiScreenApplication.instance = this

  def platform: Platform

  private val _orientation = Property[Orientation](default = None)
  val orientation = _orientation.readOnlyView

  private val _screens = ListBuffer.empty[Screen]
  def screens = _screens.toList

  def addScreen(screen: Screen) = synchronized {
    _screens -= screen
    _screens += screen
    screen.show()
    screen.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  }

  def insertScreen(index: Int, screen: Screen) = synchronized {
    _screens -= screen
    _screens.insert(index, screen)
    screen.show()
    screen.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  }

  def removeScreen(screen: Screen) = synchronized {
    screen.hide()
    _screens -= screen
  }

  def setScreen(screen: Screen) = synchronized {
    screens.foreach {
      case s => removeScreen(s)
    }
    addScreen(screen)
  }

  @tailrec
  final def withScreens(f: Screen => Unit, screens: List[Screen] = _screens.toList): Unit = {
    if (screens.nonEmpty) {
      f(screens.head)
      withScreens(f, screens.tail)
    }
  }

  private val renderFunction = (s: Screen) => s.render(Gdx.graphics.getDeltaTime)
  override def render() = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val accX = math.abs(math.round(Gdx.input.getAccelerometerX))
    val accY = math.abs(math.round(Gdx.input.getAccelerometerY))
    _orientation := (if (accY > accX || accX == accY) Orientation.Portrait else Orientation.Landscape)

    withScreens(renderFunction)
  }

  private val resizeFunction = (s: Screen) => s.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  override def resize(width: Int, height: Int) = withScreens(resizeFunction)

  private val pauseFunction = (s: Screen) => s.pause()
  override def pause() = withScreens(pauseFunction)

  private val resumeFunction = (s: Screen) => s.resume()
  override def resume() = withScreens(resumeFunction)

  private val disposeFunction = (s: Screen) => s.dispose()
  override def dispose() = withScreens(disposeFunction)
}

object MultiScreenApplication {
  private var instance: MultiScreenApplication = _

  def apply() = instance
}