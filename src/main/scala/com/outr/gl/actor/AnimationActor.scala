package com.outr.gl.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{Batch, Animation, TextureRegion}
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * AnimationActor represents an animation as an actor for scene2d management.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class AnimationActor(regions: Array[TextureRegion], frameDuration: Float = 0.025f) extends Actor {
  private val animation = new Animation(frameDuration, regions: _*)
  private var elapsed = 0.0f

  setWidth(regions(0).getRegionWidth)
  setHeight(regions(0).getRegionHeight)

  override def draw(batch: Batch, parentAlpha: Float) = {
    elapsed += Gdx.graphics.getDeltaTime
    val frame = animation.getKeyFrame(elapsed, true)
    batch.draw(frame, getX, getY)
  }
}