package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.bhaskara
import com.github.Luc16.mygame.utils.dist2
import com.github.Luc16.mygame.utils.toRad
import kotlin.math.*

const val MAX_SPEED = 600f
const val DECELERATION = 0f

open class DynamicBall(iniX: Float,
                       iniY: Float,
                       radius: Float,
                       color: Color = Color.YELLOW,
                       initialDir: Vector2 = Vector2(),
                       private val deceleration: Float = DECELERATION,
                       val maxSpeed: Float = MAX_SPEED
): Ball(iniX, iniY, radius, color) {
    val direction = Vector2(initialDir)
    var speed = maxSpeed
    val nextPos = Vector2(pos)
    private val radius2 get() = radius*radius

    fun move(valX: Float, valY: Float){
        pos.set(nextPos)
        nextPos.x += valX
        nextPos.y += valY
    }

    private fun move(vec: Vector2){
        move(vec.x, vec.y)
    }

    private fun moveTo(vec: Vector2){
        move(vec.x - x, vec.y - y)
    }

    fun moveTo(valX: Float, valY: Float){
        move(valX - x, valY - y)
    }

    open fun update(delta: Float){
//        move(direction.x*speed/60, direction.y*speed/60) //???????????
        move(direction.x*speed*delta, direction.y*speed*delta)
        speed -= deceleration*delta
        if (speed < 0) speed = 0f
    }

    fun collideFixedBall(other: Ball, delta: Float): Boolean {
        val vec = Vector2(other.x - nextPos.x, other.y - nextPos.y)
        val dot = vec.dot(direction)
        val cpOnLine = Vector2(nextPos.x + direction.x*dot, nextPos.y + direction.y*dot)
        val dotCPDir = direction.dot(cpOnLine)

        val distToLine2 = dist2(other.pos, cpOnLine)
        val distToCompare2 = when {
            dotCPDir > direction.dot(nextPos) -> dist2(other.pos, nextPos)
            dotCPDir < direction.dot(pos) -> dist2(other.pos, pos)
            else -> distToLine2
        }

        val radiusSum2 = (radius + other.radius)*(radius + other.radius)
        if (distToCompare2 <= radiusSum2){
            val offset = sqrt(radiusSum2 - distToLine2) //+ 0.1f
            val prevDir = Vector2(direction)
            val normal = Vector2(nextPos.x - other.x, nextPos.y - other.y).nor()
            bounce(normal)
            val movementCorrection = speed*delta - sqrt(dist2(pos, pos)) + 0.01f
            nextPos.set(
                cpOnLine.x - prevDir.x*offset + direction.x*movementCorrection,
                cpOnLine.y - prevDir.y*offset + direction.y*movementCorrection
            )
            return true
        }
        return false
    }

    fun collideBall(other: DynamicBall){
        if (dist2(nextPos, other.nextPos) < (radius + other.radius)*(radius + other.radius)){
            val offset = radius + other.radius - sqrt(dist2(nextPos, other.nextPos))
            val normal = Vector2(nextPos.x - other.nextPos.x, nextPos.y - other.nextPos.y).nor()

            nextPos.add(normal.x * offset/2, normal.y * offset/2)
            other.nextPos.add(-normal.x * offset/2, -normal.y * offset/2)
        }

    }

    fun bounce(normal: Vector2){
        val dot = direction.dot(normal)
        direction.x -= 2*normal.x*dot
        direction.y -= 2*normal.y*dot
        direction.nor()
    }

    fun projectCircle(axis: Vector2): Pair<Float, Float>{
        val direction = Vector2(axis.x*radius, axis.y*radius)
        val p1 = Vector2(nextPos.x + direction.x, nextPos.y + direction.y)
        val p2 = Vector2(nextPos.x - direction.x, nextPos.y - direction.y)

        var min = p1.dot(axis)
        var max = p2.dot(axis)

        if (max < min) min = max.also { max = min }

        return Pair(min, max)
    }

    fun changeDirection(dir: Vector2){
        direction.set(dir).nor()
        speed = maxSpeed
    }

    fun bounceOfWalls(screenRect: Rectangle){
        when {
            nextPos.x + radius > screenRect.width -> {
                nextPos.add(screenRect.width - (radius + nextPos.x) - 0.1f, 0f)
                bounce(Vector2(-1f, 0f))
            }
            nextPos.x - radius < 0 -> {
                nextPos.add(-nextPos.x + radius + 0.1f, 0f)
                bounce(Vector2(1f, 0f))
            }
            nextPos.y + radius > screenRect.height -> {
                nextPos.add(0f, screenRect.height - radius - nextPos.y - 0.1f)
                bounce(Vector2(0f, -1f))
            }
            nextPos.y - radius < 0 -> {
                nextPos.add(0f, -nextPos.y + radius + 0.1f)
                bounce(Vector2(0f, 1f))
            }
        }
    }

}