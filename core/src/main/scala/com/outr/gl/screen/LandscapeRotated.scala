package com.outr.gl.screen

import com.badlogic.gdx.Gdx

trait LandscapeRotated extends BaseScreen {
  private lazy val isIOS = app.platform.platformId == "ios"

  override def render(delta: Float): Unit = {
    super.render(delta)

    if (isIOS) {
      stage.getRoot.setRotation(-90.0f)
      stage.getRoot.setPosition(0.0f, Gdx.graphics.getHeight)
    }
  }
}
