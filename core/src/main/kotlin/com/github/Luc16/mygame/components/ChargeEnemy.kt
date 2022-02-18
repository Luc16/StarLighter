package com.github.Luc16.mygame.components

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.github.Luc16.mygame.utils.dist2

const val CHARGE_SPEED = 800f
const val CHARGE_TIME = 60
const val CHARGE_DURATION = 20
const val SHIELD_RADIUS = 5f

class ChargeEnemy(x: Float,
                  y: Float,
                  radius: Float,
                  private val distOfAction: Float,
                  maxSpeed: Float = 600f,
                  color: Color = Color.PINK):
    Enemy(x, y, radius, color = color, maxSpeed = maxSpeed) {
    private var chargeTimer = 0
    var charging = false
    private var hit = false
    private var chargingTime = 0

    override fun update(delta: Float, player: PlayerBall) {
        if (!live) return
        if (!charging) direction.set(player.x - x, player.y - y).nor()
        if (hit) speed = 0f
        update(delta)

        if (dist2(pos, player.pos) <= distOfAction) {
            chargeTimer += if (chargeTimer > CHARGE_TIME) 0 else 1
            if (chargeTimer  > CHARGE_TIME && !hit){
                if (chargingTime == 0) charging = true
                hit = player.collideChargingEnemy(this, delta)
            } else {
                live = !player.collideEnemy(this, delta)
                speed = 0f
            }
        } else speed = maxSpeed

        if (charging) {
            chargingTime++
            speed = CHARGE_SPEED
            if (chargingTime > CHARGE_DURATION){
                charging = false
                chargeTimer = 0
                chargingTime = 0
                hit = false
                speed = 0f
            }
        }
    }

    override fun draw(renderer: ShapeRenderer) {
        if (!live) return
        if (!charging) {
            renderer.color = Color.GRAY
            renderer.circle(x, y, radius + SHIELD_RADIUS*(chargeTimer)/CHARGE_TIME)
        }
        else {
            renderer.color = Color.YELLOW
            renderer.circle(x, y, radius + SHIELD_RADIUS)
        }
        super.draw(renderer)
    }
}

