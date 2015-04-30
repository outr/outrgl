package com.outr.gl.download

import com.badlogic.gdx.Gdx
import com.outr.gl._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait DownloadManager {
  private var _tasks = Set.empty[DownloadTask]
  def tasks = _tasks
  def isEmpty = _tasks.isEmpty

  protected def fonts(family: String, style: String, sizes: Int*) = {
    sizes.foreach {
      case size => font(family, style, fontSize(size))
    }
  }

  protected def font(family: String, style: String, size: Int) = {
    download("http://bitmapfonts.outr.com/font", s"$family.$style.$size.fnt", "fonts")
    download("http://bitmapfonts.outr.com/font", s"$family.$style.$size.png", "fonts")
  }

  protected def download(base: String, filename: String, local: String) = synchronized {
    val task = new DownloadTask(this, s"$base/$filename", Gdx.files.local(s"$local/$filename"))
    _tasks += task
  }

  private[download] def finished(task: DownloadTask) = synchronized {
    _tasks -= task
  }
}