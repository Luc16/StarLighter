package com.github.Luc16.mygame.screens


import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.Luc16.mygame.MyGame
import ktx.app.KtxScreen

abstract class CustomScreen(
    val game: MyGame,
    val batch: Batch = game.batch,
    val renderer: ShapeRenderer = game.renderer,
    val font: BitmapFont = game.font,
    val viewport: FitViewport = game.gameViewport,
) : KtxScreen {
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}