package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.github.Luc16.mygame.utils.dist2

const val BULLET_RADIUS = 8f
const val SHOOT_TIME = 90

class Enemy(x: Float,
            y: Float,
            radius: Float,
            private val distOfAction: Float,
            maxSpeed: Float = 500f,
            color: Color = Color.YELLOW):
    DynamicBall(x, y, radius, color, maxSpeed = maxSpeed) {
    private val bullets = mutableListOf<DynamicBall>()
    private var shootTimer = 0
    private var live = true

    fun update(delta: Float, player: PlayerBall) {
        if (!live) return
        shootTimer += if (shootTimer > SHOOT_TIME) 0 else 1
        direction.set(player.x - x, player.y - y).nor()
        update(delta)
        if (dist2(pos, player.pos) <= distOfAction) {
            live = !player.collideMovingBall(this, delta)
            speed = 0f
            if (shootTimer > SHOOT_TIME) {
                shootTimer = 0
                bullets.add(DynamicBall(
                    x + 18f*direction.x,
                    y + 18f*direction.y,
                    BULLET_RADIUS,
                    initialDir = direction,
                    maxSpeed = 800f,
                    color = Color.YELLOW
                ))
            }
        }
        else speed = maxSpeed

        val bulletsToRemove = mutableListOf<DynamicBall>()
        bullets.forEach { bullet ->
            bullet.update(delta)
            if (player.collideMovingBall(bullet, delta)) {
                bulletsToRemove.add(bullet)
                player.speed = 0f
            }

            if (dist2(bullet.pos, pos) > 500*500) bulletsToRemove.add(bullet)
        }
        bulletsToRemove.forEach { bullets.remove(it) }
    }

    override fun draw(renderer: ShapeRenderer) {
        if (live) {
            renderer.color = Color.YELLOW
            val radiusSum = radius+ BULLET_RADIUS
            renderer.circle(x + radiusSum*direction.x, y + radiusSum*direction.y, BULLET_RADIUS*(shootTimer)/SHOOT_TIME)
            bullets.forEach { it.draw(renderer) }
            super.draw(renderer)
        }
    }
}