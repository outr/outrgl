package com.outr.gl.task

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.Net.{HttpResponse, HttpResponseListener, HttpMethods}
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{TextureRegion, BitmapFont}
import com.badlogic.gdx.net.HttpRequestBuilder
import com.badlogic.gdx.utils.async.{AsyncTask, AsyncExecutor}
import com.outr.gl.screen.MultiScreenApplication
import org.powerscala.concurrent.{AtomicInt, Time}

import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TaskManager(application: MultiScreenApplication,
                  maxConcurrent: Int = 4,
                  autoStart: Boolean = false,
                  allowTasksAfterStart: Boolean = true) {
  private val executor = new AsyncExecutor(maxConcurrent)

  private var tasks = List.empty[Task]
  private var started = false
  private[task] val _queued = new AtomicInt(0)
  private[task] val _running = new AtomicInt(0)

  def queued = _queued()
  def running = _running()

  def isEmpty = queued == 0 && running == 0 && tasks.isEmpty

  def add(task: Task) = synchronized {
    if (started) {
      if (allowTasksAfterStart) {
        executor.submit(task)
      } else {
        throw new RuntimeException("Tasks not allowed to be added after manager started.")
      }
    } else {
      tasks = task :: tasks
    }
  }

  def start() = synchronized {
    if (!started) {
      tasks.reverse.foreach {
        case task => {
          executor.submit(task)
          _queued += 1
        }
      }
      tasks = Nil
      started = true
    }
  }

  def apply(f: => Unit) = {
    add(new FunctionalTask(this, () => f))
  }

  def futureTask[T](f: () => T, autoAdd: Boolean = true) = {
    val future = new FutureObject[T](this, f)
    if (autoAdd) add(future)
    future
  }

  def future[T](f: => T) = futureTask(() => f)

  def download(url: String, local: FileHandle, autoAdd: Boolean = true): FutureObject[FileHandle] = futureTask(() => {
    if (!local.exists()) {
      val request = new HttpRequestBuilder().newRequest().method(HttpMethods.GET).url(url).build()
      var finished = false
      Gdx.net.sendHttpRequest(request, new HttpResponseListener {
        override def handleHttpResponse(httpResponse: HttpResponse) = {
          local.writeBytes(httpResponse.getResult, false)
          finished = true
        }

        override def cancelled() = {}

        override def failed(t: Throwable) = {
          t.printStackTrace()
        }
      })
      Time.waitFor(Double.MaxValue) {
        finished
      }
    }
    local
  }, autoAdd)

  def downloadLocal(base: String, filename: String, local: String, autoAdd: Boolean = true): FutureObject[FileHandle] = {
    download(s"$base/$filename", Gdx.files.local(s"$local/$filename"), autoAdd)
  }

  def font(family: String, style: String, size: Int, scaleForOrientation: Option[Orientation] = None) = future {
    val adjustedSize = scaleForOrientation match {
      case Some(orientation) => fontSize(size, orientation)
      case None => size
    }
    val fnt = downloadLocal("http://bitmapfonts.outr.com/font", s"$family.$style.$adjustedSize.fnt", "fonts", autoAdd = false)
    val png = downloadLocal("http://bitmapfonts.outr.com/font", s"$family.$style.$adjustedSize.png", "fonts", autoAdd = false)
    fnt.invoke()
    png.invoke()
    if (!fnt().exists()) {
      throw new RuntimeException(s"Font file doesn't exist: ${fnt()}.")
    }
    if (!png().exists()) {
      throw new RuntimeException(s"Font texture doesn't exist: ${png()}.")
    }
    application.invokeAndWait {
      val texture = new Texture(png(), true)
      texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear)
      new BitmapFont(fnt(), new TextureRegion(texture), false)
    }
  }
}

trait Task extends AsyncTask[Unit] {
  def manager: TaskManager

  override final def call() = {
    manager._running += 1
    try {
      invoke()
    } finally {
      manager._running -= 1
      manager._queued -= 1
    }
  }

  def invoke(): Unit
}

class FunctionalTask(val manager: TaskManager, f: () => Unit) extends Task {
  override def invoke() = f()
}

class FutureObject[T](val manager: TaskManager, f: () => T) extends Task {
  private var result: Option[T] = None

  def apply(maxWait: Double = 60.0) = {
    Time.waitFor(maxWait, errorOnTimeout = true) {
      result.nonEmpty
    }
    result.get
  }

  def get() = result

  override def invoke() = {
    result = Some(f())
  }
}