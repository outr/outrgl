package com.outr.gl.actor

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack

/**
 * ClippingSupport can be mixed into any Actor to enable scissor clipping to the rectangular constraints of the actor.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait ClippingSupport extends Actor {
  override def draw(batch: Batch, parentAlpha: Float): Unit = if (getHeight > 1.0f && getWidth > 1.0f) {
    val scissors = new Rectangle
    val clipBounds = new Rectangle(getX, getY, getWidth, getHeight)
    ScissorStack.calculateScissors(getStage.getCamera, batch.getTransformMatrix, clipBounds, scissors)
    batch.flush()
    ScissorStack.pushScissors(scissors)
    try {
      super.draw(batch, parentAlpha)
    } finally {
      ScissorStack.popScissors()
      ()
    }
  } else {
    // Don't draw
  }
}
