package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2

const val DIRECTION_TIMER = 30

class Drone(pos: Vector2,
            radius: Float,
            initialDir: Vector2,
            lifeTime: Int,
            maxSpeed: Float = 650f,
            color: Color = Color.YELLOW):
    Bullet(pos, radius, initialDir, lifeTime, color = color, maxSpeed = maxSpeed) {

    init {
        timer = 1
    }

    override fun update(delta: Float, player: PlayerBall) {
        if (timer % DIRECTION_TIMER == 0) {
            direction.set(player.x - x, player.y - y).nor()
        }
        super.update(delta, player)
    }

    override fun draw(renderer: ShapeRenderer) {
        if (live) super.draw(renderer)
    }
}

