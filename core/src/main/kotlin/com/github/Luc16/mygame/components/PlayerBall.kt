package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.*
import kotlin.math.min
import kotlin.math.sqrt

class PlayerBall(x: Float, y: Float, radius: Float,
                 private val camera: Camera,
                 color: Color = Color.YELLOW):
    DynamicBall(x, y, radius, color) {
    var lives = Int.MAX_VALUE

    init {
        direction.setZero()
    }

    override fun update(delta: Float) {
        camera.translate(nextPos.x - x, nextPos.y - y)
        super.update(delta)
    }

    fun collideEnemy(enemy: Enemy, delta: Float): Boolean{
        val vec = enemy.pos - nextPos
        val dot = vec.dot(direction)
        val cpOnLine = nextPos + direction*dot
        val dotCPDir = direction.dot(cpOnLine)

        val distToLine2 = dist2(enemy.pos, cpOnLine)
        val distToCompare2 = when {
            dotCPDir > direction.dot(nextPos) -> dist2(enemy.pos, nextPos)
            dotCPDir < direction.dot(pos) -> dist2(enemy.pos, pos)
            else -> distToLine2
        }

        val radiusSum2 = (radius + enemy.radius)*(radius + enemy.radius)
        if (distToCompare2 <= radiusSum2){
            val prevDir = Vector2(direction)
            val normal = (nextPos - enemy.pos).nor()
            bounce(normal)
            if (enemy is ChargeEnemy && enemy.charging) {
                lives--
                enemy.speed = 0f
                return true
            }
            val offset = sqrt(radiusSum2 - distToLine2) //+ 0.1f
            val movementCorrection = speed*delta - sqrt(dist2(pos, pos)) + 0.01f
            nextPos.set(
                cpOnLine.x - prevDir.x*offset + direction.x*movementCorrection,
                cpOnLine.y - prevDir.y*offset + direction.y*movementCorrection
            )
            return true
        }
        return false
    }

    fun collideBullet(bullet: Bullet, delta: Float): Boolean {
        if (bullet.toDie) return false
        val rSum = radius + bullet.radius
        val dx = direction.x*speed*delta - bullet.direction.x*bullet.speed*delta
        val dy = direction.y*speed*delta - bullet.direction.y*bullet.speed*delta
        val ddx = x - bullet.x
        val ddy = y - bullet.y

        val (t1, t2) = bhaskara(dx*dx + dy*dy, 2*(ddx*dx + ddy*dy), ddx*ddx + ddy*ddy - rSum*rSum)
        if (t1 == null || t2 == null) return false

        val tf = if (t1 in 0f..1f) t1 else t2

        if (tf in 0f..1f) {
//            val normal = Vector2(nextPos.x - bullet.nextPos.x, nextPos.y - bullet.nextPos.y).nor()
            val extraMov = tf - 1

            val bulletBackMov = extraMov*bullet.speed*delta
            bullet.nextPos.add(bullet.direction.x*bulletBackMov, bullet.direction.y*bulletBackMov)
            speed = 0f
            lives--
            return true
        }
        return false
    }

    fun collideChargingEnemy(enemy: Enemy, delta: Float): Boolean{
        val rSum = radius + enemy.radius
        val dx = direction.x*speed*delta - enemy.direction.x*enemy.speed*delta
        val dy = direction.y*speed*delta - enemy.direction.y*enemy.speed*delta
        val ddx = x - enemy.x
        val ddy = y - enemy.y

        val (t1, t2) = bhaskara(dx*dx + dy*dy, 2*(ddx*dx + ddy*dy), ddx*ddx + ddy*ddy - rSum*rSum)
        if (t1 == null || t2 == null) return false

        val tf = if (t1 in 0f..1f) t1 else if (t2 in 0f..1f) min(t1, t2) else t2


        if (tf in 0f..1f){
            val moveCorrection = 1f
            val extraMov = tf - 1
            val backMov = extraMov*speed*delta - moveCorrection

            nextPos.add(direction.x*backMov, direction.y*backMov)

            val enemyBackMov = extraMov*enemy.speed*delta - moveCorrection
            enemy.nextPos.add(enemy.direction.x*enemyBackMov, enemy.direction.y*enemyBackMov)

            speed = 0f
            lives--
            enemy.speed = 0f
            return true
        }
        return false
    }
}