package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.bhaskara
import com.github.Luc16.mygame.utils.translate

class PlayerBall(x: Float, y: Float, radius: Float,
                 private val camera: Camera,
                 color: Color = Color.YELLOW):
    DynamicBall(x, y, radius, color) {
    private var live = true

    init {
        direction.setZero()
    }

    override fun update(delta: Float) {
        camera.translate(nextPos.x - x, nextPos.y - y)
        super.update(delta)
    }

    fun collideEnemy(enemy: Enemy, delta: Float): Boolean{
        val rSum = radius + enemy.radius
        val dx = direction.x*speed*delta - enemy.direction.x*enemy.speed*delta
        val dy = direction.y*speed*delta - enemy.direction.y*enemy.speed*delta
        val ddx = x - enemy.x
        val ddy = y - enemy.y

        val (t1, t2) = bhaskara(dx*dx + dy*dy, 2*(ddx*dx + ddy*dy), ddx*ddx + ddy*ddy - rSum*rSum)
        if (t1 == null || t2 == null) return false

        val tf = if (t1 in 0f..1f) t1 else t2

        if (tf in 0f..1f){
            val normal = Vector2(nextPos.x - enemy.nextPos.x, nextPos.y - enemy.nextPos.y).nor()
            val extraMov = tf - 1
            val backMov = extraMov*speed*delta
            nextPos.add(direction.x*backMov, direction.y*backMov)
            bounce(normal)
            nextPos.add(backMov*direction.x, backMov*direction.y)

            return true
        }
        return false
    }

    fun collideBullet(bullet: DynamicBall, delta: Float): Boolean {
        val rSum = radius + bullet.radius
        val dx = direction.x*speed*delta - bullet.direction.x*bullet.speed*delta
        val dy = direction.y*speed*delta - bullet.direction.y*bullet.speed*delta
        val ddx = x - bullet.x
        val ddy = y - bullet.y

        val (t1, t2) = bhaskara(dx*dx + dy*dy, 2*(ddx*dx + ddy*dy), ddx*ddx + ddy*ddy - rSum*rSum)
        if (t1 == null || t2 == null) return false

        val tf = if (t1 in 0f..1f) t1 else t2

        if (tf in 0f..1f) {
            speed = 0f
            return true
        }
        return false
    }
}