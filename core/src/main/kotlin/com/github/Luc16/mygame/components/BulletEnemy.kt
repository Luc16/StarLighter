package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2

const val SHOOT_TIME = 90
const val BULLET_RADIUS = 8f
const val BULLET_SPEED = 800f
const val BULLET_LIFETIME = 300

class BulletEnemy(x: Float,
            y: Float,
            radius: Float,
            private val distOfAction: Float,
            maxSpeed: Float = 500f,
            color: Color = Color.YELLOW):
    Enemy(x, y, radius, color = color, maxSpeed = maxSpeed) {
    private val bullets = linkedSetOf<Bullet>()
    private var shootTimer = 0

    override fun update(delta: Float, player: PlayerBall) {
        if (!live) return
        shootTimer += if (shootTimer > SHOOT_TIME) 0 else 1

        direction.set(player.x - x, player.y - y).nor()
        update(delta)
        if (dist2(pos, player.pos) <= distOfAction) {
            live = !player.collideEnemy(this, delta)
            speed = 0f
            if (shootTimer > SHOOT_TIME) {
                shootTimer = 0
                bullets.add(Bullet(
                    Vector2( x + 18f*direction.x, y + 18f*direction.y),
                    BULLET_RADIUS,
                    direction,
                    BULLET_LIFETIME,
                    BULLET_SPEED,
                    color = Color.YELLOW
                ))
            }
        }
        else speed = maxSpeed

        val bulletsToRemove = linkedSetOf<Bullet>()
        bullets.forEach { bullet ->
            bullet.update(delta, player)
            if (!bullet.live) bulletsToRemove.add(bullet)
        }
        bulletsToRemove.forEach { bullets.remove(it) }
    }

    override fun draw(renderer: ShapeRenderer) {
        if (!live) return
        renderer.color = Color.YELLOW
        val radiusSum = radius+ BULLET_RADIUS
        renderer.circle(x + radiusSum*direction.x, y + radiusSum*direction.y, BULLET_RADIUS*(shootTimer)/SHOOT_TIME)
        bullets.forEach { it.draw(renderer) }
        super.draw(renderer)
    }
}