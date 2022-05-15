package com.github.Luc16.mygame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.mygame.HEIGHT
import com.github.Luc16.mygame.MyGame
import com.github.Luc16.mygame.WIDTH
import com.github.Luc16.mygame.components.*
import com.github.Luc16.mygame.screens.CustomScreen
import com.github.Luc16.mygame.utils.IVector2
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.random.Random

const val MAX_RADIUS = 500f
const val MIN_RADIUS = 100f
const val CLICK_MARGIN = 100f
const val MAX_BC_STAR_RADIUS = 3f

const val STAR_LIMIT = 50

class EnemyScreen(game: MyGame): CustomScreen(game) {
    private val camera = viewport.camera
    private val offset = Vector2()
    private val player = PlayerBall(0f, 0f, 10f, camera, Color.RED)
    private var prevPos = Vector2().setZero()
    private var enemies = linkedSetOf<Enemy>(ChargeEnemy(500f, 5000f, 20f, 200*200f))
    private var stars = mutableMapOf<IVector2, Ball>()

    private val numSectorsX = (WIDTH/(2*MAX_RADIUS)).toInt() + 2
    private val numSectorsY = (HEIGHT/(2*MAX_RADIUS)).toInt() + 2
    private val bGNumSectorsX = (WIDTH/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private val bGNumSectorsY = (HEIGHT/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private var seedOffset = 0
    private var score = 500
    private var frame = 0
    private var level = 0
    private var spawnFrameRate = 180
    private val textScore = GlyphLayout()
    private val textLife = GlyphLayout()
    private val minimapSprite = Sprite(Texture(Gdx.files.local("assets/minimap.png")))

    override fun show() {
        val file = Gdx.files.local("assets/seed.txt")
        seedOffset = (file.readString().toInt() + 1)%100_000_000
        file.writeString("$seedOffset", false)

        seedOffset = 7 // Tirar dps

        minimapSprite.setSize(200f, 200f)
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
        updateEnemies(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.speed = 0f
        offset.set(player.x - WIDTH/2, player.y - HEIGHT/2)

        level = when (score) {
            in 0..50 -> 0
            in 60..150 -> 1
            in 160..250 -> 2
            in 260..340 -> 3
            in 350..420 -> 4
            in 430..500 -> 5
            else -> level
        }

        draw(delta)
        batch.use(camera.combined){
            textScore.setText(font, "Score: $score")
            font.draw(batch, textScore, offset.x + WIDTH - textScore.width - 5, offset.y + HEIGHT - textScore.height - 5)
            textLife.setText(font, "Lives: ${player.lives}")
            font.draw(batch, textLife, offset.x + WIDTH - textLife.width - 5, offset.y + HEIGHT - textLife.height - textScore.height - 20)

            minimapSprite.run {
                setPosition(offset.x + 5f, offset.y + HEIGHT - height - 5)
                draw(batch)
            }
        }
        if (player.lives <= 0) reset()
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

    private fun updateEnemies(delta: Float){
        val enemiesToRemove = linkedSetOf<Enemy>()
        enemies.forEach { enemy ->
            enemy.update(delta, player)
            enemies.forEach { enemy.collideBall(it) }
            if (!enemy.live) {
                enemiesToRemove.add(enemy)
                if (!enemy.crached) score += 10
            }
        }
        enemiesToRemove.forEach { enemies.remove(it) }

        spawnEnemies()
    }

    private fun spawnEnemies(){
        if (enemies.size < level && frame%spawnFrameRate == 0){
            val x: Int
            val y: Int
            if (Random.nextBoolean()){
                x = listOf(-4500, 0, 4500).random()
                y = (if (x == 0) listOf(-4500, 4500) else listOf(-4500, 0 , 4500)).random()
            } else {
                y = listOf(-4500, 0, 4500).random()
                x = (if (y == 0) listOf(-4500, 4500) else listOf(-4500, 0 , 4500)).random()
            }


            enemies.add(when (Random.nextInt(1, 4)){
                1 -> BulletEnemy(player.x + x, player.y + y, 20f, 200*200f, color = Color.ORANGE)
                2 -> ChargeEnemy(player.x + x, player.y + y, 20f, 200*200f, color = Color.PINK)
                else -> DroneEnemy(player.x + x, player.y + y, 20f, 200*200f, color = Color.BLUE)
            })
        }
    }

    private fun drawMinimap(renderer: ShapeRenderer, delta: Float, sizeX: Float = 200f, sizeY: Float = 200f, ratio: Float = 0.02f) {
        val mapNumSectorsX = (sizeX/(2*MAX_RADIUS*ratio)).toInt()
        val mapNumSectorsY = (sizeY/(2*MAX_RADIUS*ratio)).toInt()
        val startSectorX = (player.x/(2*MAX_RADIUS)).toInt() - mapNumSectorsX/2
        val startSectorY = (player.y/(2*MAX_RADIUS)).toInt() - mapNumSectorsY/2


        val startPoint = Vector2(offset.x + 5f, offset.y + HEIGHT - sizeY - 5)
        renderer.color = Color.LIGHT_GRAY
        renderer.rect(startPoint.x, startPoint.y, sizeX, sizeY)

        forEachStarSectorIn(0 until mapNumSectorsX, 0 until mapNumSectorsY) { i, j ->
            val starI = startSectorX + i
            val starJ = startSectorY + j
            val rand = Random(createSeed(starI, starJ))

            if (rand.nextInt(0, 256) < STAR_LIMIT){
                if (stars[IVector2(starI, starJ)] == null)
                    stars[IVector2(starI, starJ)] = Ball(
                        (2*starI + 1) * MAX_RADIUS,
                        (2*starJ + 1) * MAX_RADIUS,
                        MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS),
                        color = if (rand.nextInt(0, 100) < 33) Color.YELLOW else Color.GRAY
                    )

                stars[IVector2(starI, starJ)]?.let { star ->
                    renderer.color = star.color
                    val circlePos = Vector2(
                        startPoint.x + ((2*i + 1) * MAX_RADIUS - player.x + (player.x/(2*MAX_RADIUS)).toInt()*2*MAX_RADIUS)*ratio,
                        startPoint.y + ((2*j + 1) * MAX_RADIUS - player.y + (player.y/(2*MAX_RADIUS)).toInt()*2*MAX_RADIUS)*ratio,
                    )
                    val minimapStarRadius = star.radius*ratio
                    if (circlePos.x - minimapStarRadius > startPoint.x &&
                        circlePos.x + minimapStarRadius < startPoint.x + sizeX &&
                        circlePos.y - minimapStarRadius > startPoint.y &&
                        circlePos.y + minimapStarRadius < startPoint.y + sizeY){
                        renderer.circle(circlePos.x, circlePos.y, minimapStarRadius)
                    }
                    enemies.forEach {
                        if (it.collideFixedBall(star, delta)) {
                            it.live = false
                            it.crached = true
                        }
                    }
                }

            }
        }

        renderer.color = Color.BLACK
        renderer.circle(startPoint.x + sizeX/2, startPoint.y + sizeY/2, 2f)

        enemies.forEach { it.run {
            val minimapPos = Vector2(startPoint.x + sizeX/2 + (x - player.x)*ratio, startPoint.y + sizeY/2 + (y - player.y)*ratio)
            renderer.color = color
            if (minimapPos.x - radius > startPoint.x &&
                minimapPos.x + radius < startPoint.x + sizeX &&
                minimapPos.y - radius > startPoint.y &&
                minimapPos.y + radius < startPoint.y + sizeY)
                    renderer.circle(minimapPos.x, minimapPos.y, 3f)

        }}

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

                stars[IVector2(i, j)]?.let { star ->
                    if (player.collideFixedBall(star, delta)) {
                        if (star.color == Color.YELLOW)
                            player.lives--
                        else {
                            star.color = Color.YELLOW
                            score += 10
                        }
                    }
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
            drawMinimap(renderer, delta)
        }
    }

    private fun reset(){
        score = 0
//        seedOffset++
        offset.set(-WIDTH/2, -HEIGHT/2)
        stars = mutableMapOf()
        player.nextPos.setZero()
        player.pos.setZero()
        camera.moveTo(Vector2().setZero())
        player.direction.setZero()
        player.lives = 4
        enemies = linkedSetOf(ChargeEnemy(500f, 500f, 20f, 200*200f))
    }
}