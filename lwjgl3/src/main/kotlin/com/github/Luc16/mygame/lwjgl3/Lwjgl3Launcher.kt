package com.github.Luc16.mygame.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.Luc16.mygame.HEIGHT
import com.github.Luc16.mygame.MyGame
import com.github.Luc16.mygame.WIDTH


fun main() {
    Lwjgl3Application(MyGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("MyGame")
        setWindowedMode(WIDTH.toInt(), HEIGHT.toInt())
        setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
    })
}
