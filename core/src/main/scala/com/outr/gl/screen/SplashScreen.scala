package com.outr.gl.screen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.outr.gl._
import com.outr.gl.actor.AnimationActor
import com.outr.gl.task.TaskManager

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class SplashScreen(taskManager: TaskManager,
                            textureManager: TextureManager,
                            loadingTextures: Array[TextureRegion],
                            nextScreen: => BaseScreen,
                            backgroundColor: Color = Color.WHITE,
                            maxConcurrent: Int = 4,
                            minimumDisplayTime: Long = 2000) extends BaseScreen {
  lazy val background = new Image(textureManager.Pixel).color(backgroundColor).width().height()

  lazy val loading = new AnimationActor(loadingTextures).center()

  private val started = System.currentTimeMillis()
  private var finished = false

  override def init() = {
    stage.addActor(background)
    stage.addActor(loading)

    taskManager.start()
  }

  override def render(delta: Float) = {
    loading.setOrigin(Align.center)
    loading.rotateBy(delta * -150.0f)

    super.render(delta)

    if (!transitioning && !finished && taskManager.isEmpty && System.currentTimeMillis() - started > minimumDisplayTime) {
      finished = true
      transitionToNext()
    }
  }

  protected def transitionToNext() = {
    transitionCrossFade(nextScreen, 0.5f)
  }
}
