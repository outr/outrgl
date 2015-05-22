package com.outr.gl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Orientation
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{TextureRegion, BitmapFont}
import com.outr.gl.screen.AbstractBaseScreen

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FontManager {
  protected def load(orientation: Orientation, family: String, style: String, size: Int, adjust: Boolean = true) = {
    val s = if (adjust) fontSize(size, orientation) else size
    val fnt = Gdx.files.local(s"fonts/$family.$style.$s.fnt")
    val png = Gdx.files.local(s"fonts/$family.$style.$s.png")
    if (!fnt.exists()) {
      throw new RuntimeException(s"Font file doesn't exist: $fnt.")
    }
    if (!png.exists()) {
      throw new RuntimeException(s"Font texture doesn't exist: $png.")
    }
    val texture = new Texture(png, true)
    texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear)
    new BitmapFont(fnt, new TextureRegion(texture), false)
  }
}
