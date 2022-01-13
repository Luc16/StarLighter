package com.github.Luc16.mygame.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.utils.dist2
import com.github.Luc16.mygame.utils.ortho

class DroneEnemy(x: Float,
                  y: Float,
                  radius: Float,
                  private val distOfAction: Float,
                  maxSpeed: Float = 500f,
                  color: Color = Color.YELLOW):
    Enemy(x, y, radius, color = color, maxSpeed = maxSpeed) {
    private val drones = linkedSetOf<Drone>()
    private var dronesReleased = false

    override fun update(delta: Float, player: PlayerBall) {
        if (!live) return

        speed = if (!dronesReleased) {
            direction.set(player.x - x, player.y - y).nor()
            maxSpeed
        } else 0f
        update(delta)

        if (dist2(pos, player.pos) <= distOfAction) {
            if (player.collideEnemy(this, delta)){
                live = false
                drones.forEach { it.live = false }
            }
            if (!dronesReleased) {
                val dirs = listOf(direction, direction.ortho(), direction.ortho().scl(-1f))
                repeat(3){
                    drones.add(Drone(x, y, 8f, color = Color.YELLOW, initialDir = dirs[it]))
                }
            }
        }

        val dronesToRemove = linkedSetOf<Drone>()
        drones.forEach { drone ->
            drone.update(delta, player)
            if (!drone.live) dronesToRemove.add(drone)
        }
        dronesToRemove.forEach { drones.remove(it) }
        dronesReleased = drones.isNotEmpty()
    }

    override fun draw(renderer: ShapeRenderer) {
        if (!live) return
        drones.forEach { it.draw(renderer) }
        super.draw(renderer)
    }
}