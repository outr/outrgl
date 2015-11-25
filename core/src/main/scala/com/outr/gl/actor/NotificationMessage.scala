package com.outr.gl.actor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.Align
import com.outr.gl._
import com.outr.gl.screen.AbstractBaseScreen

import scala.collection.mutable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NotificationMessage(style: LabelStyle, foreground: Color, background: Color, manager: TextureManager, showTime: Float = 5.0f)
                         (implicit screen: AbstractBaseScreen) extends RoundedGroup(background, 10, manager)(screen) {
  val label = new Label("", style).color(foreground)

  addActor(label)
  setVisible(false)

  var hiding = true

  private val backlog = mutable.Queue.empty[String]

  def show(message: String) = synchronized {
    if (hiding) {
      clearActions()
      showMessage(message)
      addAction(
        sequence(
          delay(showTime),
          function(nextMessage())
        )
      )
    } else {
      backlog += message
    }
  }

  private def showMessage(message: String) = {
    hiding = false
    getColor.a = 0.0f
    setVisible(true)
    val width = screen.stage.getWidth * 0.8f

    label.setWidth(width)
    label.setWrap(true)
    label.setAlignment(Align.center)

    updateMessage(message)

    setWidth(width)
    setX(screen.stage.getWidth * 0.1f)
    setY(screen.stage.getHeight * 0.25f)

    addAction(fade(this, 1.0f, 0.5f, Interpolation.linear))
  }

  private def updateMessage(message: String) = {
    label.setText(message)
    label.setY(label.getPrefHeight / 2.0f)
    setHeight(label.getPrefHeight)
  }

  private def hideMessage() = {
    hiding = true
    addAction(
      sequence(
        fade(this, 0.0f, 1.0f, Interpolation.linear),
        function {
          setVisible(false)
        }
      )
    )
  }

  protected def nextMessage(): Unit = synchronized {
    if (backlog.nonEmpty) {
      val message = backlog.dequeue()
      updateMessage(message)
      addAction(
        sequence(
          delay(showTime),
          function(nextMessage())
        )
      )
    } else {
      hideMessage()
    }
  }
}
