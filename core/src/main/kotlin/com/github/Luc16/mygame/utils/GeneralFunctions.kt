package com.github.Luc16.mygame.utils

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

fun Float.toRad(): Float = PI.toFloat() * this/180
fun Float.toDeg(): Float = 180 * this/PI.toFloat()

fun Vector2.ortho(): Vector2 = Vector2(-this.y, this.x)

fun Camera.translate(x: Float = 0f, y: Float = 0f) {
    translate(x, y, 0f)
    update()
}

fun randomColor(offset: Float) = Color(
    offset + Random.nextFloat(),
    offset + Random.nextFloat(),
    offset + Random.nextFloat(), 1f
)

fun dist2(p1: Vector2, p2: Vector2): Float = (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y)
fun dist2(p1: Vector2, x: Float, y: Float): Float = (p1.x - x)*(p1.x - x) + (p1.y - y)*(p1.y - y)
fun dist2(x1: Float, y1: Float, x2: Float, y2: Float): Float = (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2)

fun bhaskara(a: Float, b: Float, c: Float): Pair<Float?, Float?>{
    val delta = b*b-4*a*c
    if (delta < 0) return Pair(null, null)
    val sqrtDelta = sqrt(delta)
    return Pair((-b+sqrtDelta)/(2*a), (-b-sqrtDelta)/(2*a))
}

