package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.HEIGHT
import com.github.Luc16.mygame.MyGame
import com.github.Luc16.mygame.WIDTH
import com.github.Luc16.mygame.components.Ball
import com.github.Luc16.mygame.components.Enemy
import com.github.Luc16.mygame.components.PlayerBall
import com.github.Luc16.mygame.screens.*
import com.github.Luc16.mygame.utils.IVector2
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.math.roundToInt
import kotlin.random.Random

const val MAX_RADIUS = 500f
const val MIN_RADIUS = 50f
const val CLICK_MARGIN = 100f
const val MAX_BC_STAR_RADIUS = 3f

const val STAR_LIMIT = 50

class EnemyScreen(game: MyGame): CustomScreen(game) {
    private val camera = viewport.camera
    private val offset = Vector2()
    private val player = PlayerBall(0f, 0f, 10f, camera, Color.RED)
    private var prevPos = Vector2().setZero()
    private val enemies = mutableListOf<Enemy>(Enemy(3000f, 3000f, 20f, 200*200f, color = Color.BLUE))
    private var stars = mutableMapOf<IVector2, Ball>()

    private val numSectorsX = (WIDTH/(2*MAX_RADIUS)).toInt() + 2
    private val numSectorsY = (HEIGHT/(2*MAX_RADIUS)).toInt() + 2
    private val bGNumSectorsX = (WIDTH/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private val bGNumSectorsY = (HEIGHT/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private var seedOffset = 0
    private var score = 0
    private val textLayout = GlyphLayout()
    private var frame = 0

    override fun show() {
        val file = Gdx.files.local("assets/seed.txt")
        seedOffset = (file.readString().toInt() + 1)%100_000_000
        file.writeString("$seedOffset", false)

        seedOffset = 7 // Tirar dps
    }

    private fun createSeed(i: Int, j: Int): Int = i and 0xFFFF shl 16 or (j and 0xFFFF) + seedOffset

    private fun forEachStarSectorIn(rangeI: IntRange, rangeJ: IntRange, func: (Int, Int) -> Unit) {
        for (i in rangeI){
            for (j in rangeJ) {
                func(i, j)
            }
        }
    }

    override fun render(delta: Float) {
        frame++
        handleInputs()
        player.update(delta)
        enemies.forEachIndexed { i, enemy ->
            enemy.update(delta, player)
            for(j in i + 1 until enemies.size){
                enemy.collideBall(enemies[j])
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.speed = 0f
        offset.set(player.x - WIDTH/2, player.y - HEIGHT/2)

        draw(delta)
        batch.use(camera.combined){
            textLayout.setText(font, "Score: $score")
            font.draw(batch, textLayout, offset.x + WIDTH - textLayout.width - 5, offset.y + HEIGHT - textLayout.height - 5)
        }
    }

    private fun handleInputs() {
        handleSwipe()
    }

    private fun handleSwipe(){
        when {
            Gdx.input.justTouched() || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) -> {
                prevPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            }
            (!Gdx.input.isTouched || !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) && !prevPos.isZero  -> {
                val dir = Vector2(Gdx.input.x.toFloat() - prevPos.x, -(Gdx.input.y.toFloat() - prevPos.y))
                if (!dir.isZero(CLICK_MARGIN)) player.changeDirection(dir)
                prevPos.setZero()
            }
            !Gdx.input.isTouched -> prevPos.setZero()
        }
    }

    private fun drawMinimap(renderer: ShapeRenderer, sizeX: Float = 200f, sizeY: Float = 140f, ratio: Float = 0.01f) {
        val mapNumSectorsX = (sizeX/(2*MAX_RADIUS*ratio)).toInt()
        val mapNumSectorsY = (sizeY/(2*MAX_RADIUS*ratio)).toInt()
        val startSectorX = (player.x/(2*MAX_RADIUS) - mapNumSectorsX/2).toInt()
        val startSectorY = (player.y/(2*MAX_RADIUS) - mapNumSectorsY/2).toInt()

        val startPoint = Vector2(offset.x + 5f, offset.y + HEIGHT - sizeY + 5)
        renderer.color = Color.LIGHT_GRAY
        renderer.rect(startPoint.x, startPoint.y, sizeX, sizeY)

        forEachStarSectorIn(0..mapNumSectorsX, 0..mapNumSectorsY) { i, j ->
            val rand = Random(createSeed(startSectorX + i,startSectorY + j))
            val correction = Vector2(if (player.x > 0f) 2*MAX_RADIUS else 0f, if (player.y > 0f) 2*MAX_RADIUS else 0f)
            if (rand.nextInt(0, 256) < STAR_LIMIT){
                renderer.color = Color.GRAY
                stars[IVector2(startSectorX + i,startSectorY + j)]?.let { star -> renderer.color = star.color }
                renderer.circle(
                    startPoint.x + ((2*i + 1)*MAX_RADIUS - player.x + (player.x/(2*MAX_RADIUS)).toInt()*2*MAX_RADIUS + correction.x)*ratio,
                    startPoint.y + ((2*j + 1)*MAX_RADIUS - player.y + (player.y/(2*MAX_RADIUS)).toInt()*2*MAX_RADIUS + correction.y)*ratio,
                    ratio*(MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS))
                )
            }
        }

        renderer.color = Color.BLACK
        renderer.circle(startPoint.x + sizeX/2, startPoint.y + sizeY/2, 2f)

        renderer.color = Color.BLUE
        enemies.forEach {
            if (it.live)
                renderer.circle(startPoint.x + sizeX/2 + (it.x - offset.x)*ratio, startPoint.y + sizeY/2 + (it.y - offset.y)*ratio, 3f)
        }

    }

    private fun drawBackgroundStars(renderer: ShapeRenderer){
        val bGStartSectorX = (offset.x/(2*MAX_BC_STAR_RADIUS)).toInt() - 1
        val bGStartSectorY = (offset.y/(2*MAX_BC_STAR_RADIUS)).toInt() - 1
        renderer.color = Color.LIGHT_GRAY
        forEachStarSectorIn(
            bGStartSectorX..bGStartSectorX+bGNumSectorsX,
            bGStartSectorY..bGStartSectorY+bGNumSectorsY
        ){ i, j ->
            val rand = Random(createSeed(i, j))
            if (rand.nextInt(0, 256) < 3){
                renderer.circle(
                    (2*i + 1) * MAX_BC_STAR_RADIUS,
                    (j*2 + 1) * MAX_BC_STAR_RADIUS,
                    1 + rand.nextFloat() * (MAX_BC_STAR_RADIUS - 1)
                )
            }
        }

    }

    private fun handleEntities(renderer: ShapeRenderer, delta: Float){
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 1
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 1
        forEachStarSectorIn(
            startSectorX..startSectorX+numSectorsX,
            startSectorY..startSectorY+numSectorsY
        ) { i, j ->
            val rand = Random(createSeed(i, j))
            if (rand.nextInt(0, 256) < STAR_LIMIT){
                if (stars[IVector2(i, j)] == null)
                    stars[IVector2(i, j)] = Ball(
                        (2*i + 1) * MAX_RADIUS,
                        (2*j + 1) * MAX_RADIUS,
                        MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS),
                        color = Color.GRAY
                    )
                stars[IVector2(i, j)]?.let { star ->
                    if (player.collideFixedBall(star, delta)) {
                        if (star.color == Color.YELLOW) {
                            reset()
                            return@forEachStarSectorIn
                        }
                        star.color = Color.YELLOW
                        score += 100
                    }
                    enemies.forEach { if (it.live) it.live = !it.collideFixedBall(star, delta) }
                    star.draw(renderer)
                }
            }
        }
    }

    private fun draw(delta: Float){
        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            drawBackgroundStars(renderer)
            handleEntities(renderer, delta)
            player.draw(renderer)
            enemies.forEach { it.draw(renderer) }
            drawMinimap(renderer)
        }
    }

    private fun reset(){
        score = 0
        seedOffset++
        offset.set(-WIDTH/2, -HEIGHT/2)
        stars = mutableMapOf()
        player.nextPos.setZero()
        player.pos.setZero()
        camera.moveTo(Vector2().setZero())
        player.direction.setZero()
    }
}