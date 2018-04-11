package com.outr.gl.actor

import com.badlogic.gdx.scenes.scene2d.ui.TextField.{TextFieldListener, TextFieldStyle}
import com.badlogic.gdx.scenes.scene2d.ui.{Label, TextField}
import com.outr.gl.actor.addon.FocusSupport

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TextInput(placeholder: Label, style: TextFieldStyle) extends TextField("", style) with FocusSupport {
  setTextFieldListener(new TextFieldListener {
    override def keyTyped(textField: TextField, c: Char) = updatePlaceholder()
  })

  private def updatePlaceholder() = if (placeholder != null) placeholder.setVisible(getText.isEmpty)

  override def focus() = {
    super.focus()

    getOnscreenKeyboard.show(true)
    true
  }

  override def setText(str: String): Unit = {
    super.setText(str)
    updatePlaceholder()
  }
}