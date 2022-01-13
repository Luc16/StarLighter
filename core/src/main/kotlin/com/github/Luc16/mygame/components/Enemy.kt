package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2

abstract class Enemy(x: Float,
                     y: Float,
                     radius: Float,
                     maxSpeed: Float = 500f,
                     color: Color = Color.YELLOW,
                     initialDir: Vector2 = Vector2()):
    DynamicBall(x, y, radius, color, maxSpeed = maxSpeed, initialDir = initialDir) {
    var live = true

    abstract fun update(delta: Float, player: PlayerBall)
}