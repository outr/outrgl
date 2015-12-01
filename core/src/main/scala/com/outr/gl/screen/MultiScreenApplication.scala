package com.outr.gl.screen

import java.util.concurrent.ConcurrentLinkedQueue

import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{ApplicationListener, Gdx, Screen}
import com.outr.gl.{Platform, _}
import org.powerscala.concurrent.Time
import org.powerscala.property.Property

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class MultiScreenApplication extends ApplicationListener {
  MultiScreenApplication.instance = this

  private var lastRender = 0L

  var orientationOverride: Option[Orientation] = None

  def defaultOrientation: Orientation
  def platform: Platform[_]

  private val _orientation = Property[Orientation](default = None)
  val orientation = _orientation.readOnlyView

  private val _activeScreens = ListBuffer.empty[Screen]
  def activeScreens = _activeScreens.toList

  private val _screens = mutable.HashMap.empty[String, Screen]

  def screen[S <: Screen](name: String, loader: => S): S = synchronized {
    _screens.get(name) match {
      case Some(s) => s.asInstanceOf[S]
      case None => {
        val s: S = loader
        _screens.put(name, s)
        s
      }
    }
  }

  def disposeAllScreens() = synchronized {
    _screens.values.foreach(s => s.dispose())
    _screens.clear()
  }

  private val workQueue = new ConcurrentLinkedQueue[() => Unit]()

  def invokeLater(f: => Unit) = workQueue.add(() => f)

  def invokeAndWait[T](f: => T) = {
    var result: Option[T] = None
    workQueue.add(() => {
      result = Some(f)
    })
    Time.waitFor(10.0, errorOnTimeout = true) {
      result.nonEmpty
    }
    result.get
  }

  def waitForRender() = {
    val previousRender = lastRender
    Time.waitFor(10.0, errorOnTimeout = true) {
      previousRender != lastRender
    }
  }

  def addScreen(screen: Screen) = synchronized {
    _activeScreens -= screen
    _activeScreens += screen
    screen.show()
    screen.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  }

  def insertScreen(index: Int, screen: Screen) = synchronized {
    _activeScreens -= screen
    _activeScreens.insert(index, screen)
    screen.show()
    screen.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  }

  def removeScreen(screen: Screen) = synchronized {
    screen.hide()
    _activeScreens -= screen
  }

  def setScreen(screen: Screen) = synchronized {
    activeScreens.foreach {
      case s => removeScreen(s)
    }
    addScreen(screen)
  }

  @tailrec
  final def withScreens(f: Screen => Unit, screens: List[Screen] = _activeScreens.toList): Unit = {
    if (screens.nonEmpty) {
      f(screens.head)
      withScreens(f, screens.tail)
    }
  }

  private val renderFunction = (s: Screen) => s.render(Gdx.graphics.getDeltaTime)
  private var orientationChangeStart: Option[Long] = None
  override def render() = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    orientationOverride match {
      case Some(o) => orientationChange(o)
      case None => {
        val accX = math.abs(math.round(Gdx.input.getAccelerometerX))
        val accY = math.abs(math.round(Gdx.input.getAccelerometerY))
        orientationChange(if (accY > accX || accX == accY) Orientation.Portrait else Orientation.Landscape)
      }
    }

    withScreens(renderFunction)
    processWork()
    lastRender = Gdx.graphics.getFrameId
  }

  private val orientationDelay = 500L
  private def orientationChange(orientation: Orientation): Unit = {
    val current = _orientation()
    if (current != orientation) {   // Different
      val time = System.currentTimeMillis()
      orientationChangeStart match {
        case Some(start) => if (time - start > orientationDelay) {
          _orientation := orientation
          orientationChangeStart = None
        }
        case None => orientationChangeStart = Some(time)
      }
    } else {
      orientationChangeStart = None
    }
  }

  @tailrec
  private def processWork(): Unit = workQueue.poll() match {
    case null => // Finished
    case f => {
      try {
        f()
      } catch {
        case t: Throwable => ErrorHandler(t)
      }
      processWork()
    }
  }

  private val resizeFunction = (s: Screen) => s.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  override def resize(width: Int, height: Int) = withScreens(resizeFunction)

  private val pauseFunction = (s: Screen) => s.pause()
  override def pause() = withScreens(pauseFunction)

  private val resumeFunction = (s: Screen) => s.resume()
  override def resume() = withScreens(resumeFunction)

  private val disposeFunction = (s: Screen) => s.dispose()
  override def dispose() = {
    withScreens(disposeFunction)
    Gdx.app.exit()
  }
}

object MultiScreenApplication {
  private var instance: MultiScreenApplication = _

  def apply() = instance

  def handleException(t: Throwable) = com.outr.gl.ErrorHandler(t)
}