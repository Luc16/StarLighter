package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2

open class Bullet(pos: Vector2,
                  radius: Float,
                  initialDir: Vector2,
                  private val lifeTime: Int,
                  maxSpeed: Float,
                  color: Color = Color.YELLOW):
    DynamicBall(pos.x, pos.y, radius, color = color, maxSpeed = maxSpeed, initialDir = initialDir) {
    var timer = 1
    var toDie = false
    var live = true

    open fun update(delta: Float, player: PlayerBall) {
        live = !toDie
        timer++
        update(delta)
        toDie = player.collideBullet(this, delta) || timer > lifeTime
    }
}