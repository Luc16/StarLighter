package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2

const val DIRECTION_TIMER = 30
const val DEATH_TIME = 60*60

class Drone(x: Float,
            y: Float,
            radius: Float,
            maxSpeed: Float = 800f,
            initialDir: Vector2 = Vector2(),
            color: Color = Color.YELLOW):
    Enemy(x, y, radius, color = color, maxSpeed = maxSpeed, initialDir = initialDir) {
    var timer = 0

    override fun update(delta: Float, player: PlayerBall) {
        if (!live) return
        timer++

        if (timer % DIRECTION_TIMER == 0) {
            direction.set(player.x - x, player.y - y).nor()
            timer = 0
        }
        update(delta)
        if (player.collideBullet(this, delta)){
            live = false
        }

        if (timer > DEATH_TIME) live = false
    }

    override fun draw(renderer: ShapeRenderer) {
        if (live) super.draw(renderer)
    }
}

