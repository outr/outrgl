package com.outr.gl.screen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.async.AsyncExecutor
import com.outr.gl._
import com.outr.gl.actor.AnimationActor
import com.outr.gl.download.DownloadManager

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SplashScreen(downloadManager: DownloadManager,
                   loadingTextures: Array[TextureRegion],
                   nextScreen: BaseScreen,
                   backgroundColor: Color = Color.WHITE,
                   maxConcurrent: Int = 4,
                   minimumDisplayTime: Long = 2000) extends BaseScreen {
  lazy val background = new Image(TextureManager.Pixel).color(backgroundColor).width().height()

  lazy val loading = new AnimationActor(loadingTextures).center()

  val executor = new AsyncExecutor(4)

  private val started = System.currentTimeMillis()
  private var finished = false

  override def init() = {
    stage.addActor(background)
    stage.addActor(loading)

    downloadManager.tasks.foreach {
      case task => executor.submit(task)
    }
  }

  override def render(delta: Float) = {
    loading.setOrigin(Align.center)
    loading.rotateBy(delta * -150.0f)

    super.render(delta)

    if (!transitioning && !finished && downloadManager.isEmpty && System.currentTimeMillis() - started > minimumDisplayTime) {
      finished = true
      transitionToNext()
    }
  }

  protected def transitionToNext() = {
    transitionCrossFade(nextScreen, 0.5f)
  }

  override def hide() = {
    super.hide()

    executor.dispose()
  }
}
