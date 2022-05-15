package com.github.Luc16.mygame

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.Luc16.mygame.screens.CustomScreen
import com.github.Luc16.mygame.screens.EnemyScreen
import com.github.Luc16.mygame.screens.UniverseScreen
import ktx.app.KtxGame

const val WIDTH = 450f
const val HEIGHT = 800f
//const val WIDTH = 1280f
//const val HEIGHT = 800f

class MyGame: KtxGame<CustomScreen>() {
    val renderer: ShapeRenderer by lazy { ShapeRenderer() }
    val font: BitmapFont by lazy { BitmapFont() }
    val batch: Batch by lazy { SpriteBatch() }
    val gameViewport = FitViewport(450f, 800f)

    override fun create() {
        font.data.scale(1f)
        addScreen(UniverseScreen(this))
        addScreen(EnemyScreen(this))
        setScreen<EnemyScreen>()
    }

    override fun dispose() {
        super.dispose()
        renderer.dispose()
        batch.dispose()
        font.dispose()
    }
}