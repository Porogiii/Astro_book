package com.example.astro_book.opengl

import android.content.Context
import android.opengl.Matrix
import com.example.astro_book.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SolarSystem(private val context: Context) {
    // Солнце
    private val sun = Sphere(0.3f)

    // Планеты
    private val mercury = Sphere(0.06f)
    private val venus = Sphere(0.10f)
    private val earth = Sphere(0.11f)
    private val mars = Sphere(0.08f)
    private val jupiter = Sphere(0.25f)
    private val saturn = Sphere(0.22f)
    private val uranus = Sphere(0.15f)
    private val neptune = Sphere(0.14f)

    // Луна
    private val moon = Sphere(0.03f)

    // Углы
    private var mercuryAngle = 0f
    private var venusAngle = 45f
    private var earthAngle = 90f
    private var marsAngle = 135f
    private var jupiterAngle = 180f
    private var saturnAngle = 225f
    private var uranusAngle = 270f
    private var neptuneAngle = 315f
    private var moonAngle = 0f

    // Скорость
    private val mercurySpeed = 2.0f
    private val venusSpeed = 1.5f
    private val earthSpeed = 1.0f
    private val marsSpeed = 0.8f
    private val jupiterSpeed = 0.5f
    private val saturnSpeed = 0.4f
    private val uranusSpeed = 0.3f
    private val neptuneSpeed = 0.2f
    private val moonSpeed = 5.0f

    // радиусы
    private val mercuryOrbit = 0.5f
    private val venusOrbit = 0.8f
    private val earthOrbit = 1.1f
    private val marsOrbit = 1.4f
    private val jupiterOrbit = 1.9f
    private val saturnOrbit = 2.3f
    private val uranusOrbit = 2.7f
    private val neptuneOrbit = 3.1f
    private val moonOrbit = 0.18f

    private var selectedPlanetIndex = 0
    private val planets = listOf(mercury, venus, earth, mars, jupiter, saturn, uranus, neptune, moon)
    private val planetOrbits = listOf(mercuryOrbit, venusOrbit, earthOrbit, marsOrbit, jupiterOrbit, saturnOrbit, uranusOrbit, neptuneOrbit, moonOrbit)
    private val planetRadii = listOf(0.06f, 0.10f, 0.11f, 0.08f, 0.25f, 0.22f, 0.15f, 0.14f, 0.03f)
    private val planetNames = listOf("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Moon")

    init {
        // текстуры
        sun.loadTexture(context, R.drawable.sun)
        mercury.loadTexture(context, R.drawable.mercury)
        venus.loadTexture(context, R.drawable.venus)
        earth.loadTexture(context, R.drawable.earth)
        mars.loadTexture(context, R.drawable.mars)
        jupiter.loadTexture(context, R.drawable.jupiter)
        saturn.loadTexture(context, R.drawable.saturn)
        uranus.loadTexture(context, R.drawable.uranus)
        neptune.loadTexture(context, R.drawable.neptune)
        moon.loadTexture(context, R.drawable.moon)
    }

    fun update(deltaTime: Float) {
        mercuryAngle += mercurySpeed * deltaTime * 10f
        venusAngle += venusSpeed * deltaTime * 10f
        earthAngle += earthSpeed * deltaTime * 10f
        marsAngle += marsSpeed * deltaTime * 10f
        jupiterAngle += jupiterSpeed * deltaTime * 10f
        saturnAngle += saturnSpeed * deltaTime * 10f
        uranusAngle += uranusSpeed * deltaTime * 10f
        neptuneAngle += neptuneSpeed * deltaTime * 10f
        moonAngle += moonSpeed * deltaTime * 10f

        if (mercuryAngle >= 360f) mercuryAngle -= 360f
        if (venusAngle >= 360f) venusAngle -= 360f
        if (earthAngle >= 360f) earthAngle -= 360f
        if (marsAngle >= 360f) marsAngle -= 360f
        if (jupiterAngle >= 360f) jupiterAngle -= 360f
        if (saturnAngle >= 360f) saturnAngle -= 360f
        if (uranusAngle >= 360f) uranusAngle -= 360f
        if (neptuneAngle >= 360f) neptuneAngle -= 360f
        if (moonAngle >= 360f) moonAngle -= 360f
    }

    fun draw(mvpMatrix: FloatArray) {
        // Солнце
        val sunMatrix = FloatArray(16)
        Matrix.setIdentityM(sunMatrix, 0)
        val sunMVP = FloatArray(16)
        Matrix.multiplyMM(sunMVP, 0, mvpMatrix, 0, sunMatrix, 0)
        sun.draw(sunMVP)

        // планеты
        drawPlanet(mercury, mercuryOrbit, mercuryAngle, mvpMatrix)
        drawPlanet(venus, venusOrbit, venusAngle, mvpMatrix)

        // Земля
        val earthX = earthOrbit * Math.cos(Math.toRadians(earthAngle.toDouble())).toFloat()
        val earthZ = earthOrbit * Math.sin(Math.toRadians(earthAngle.toDouble())).toFloat()
        drawPlanet(earth, earthOrbit, earthAngle, mvpMatrix)

        // Луна
        val moonMatrix = FloatArray(16)
        Matrix.setIdentityM(moonMatrix, 0)
        Matrix.translateM(moonMatrix, 0, earthX, 0f, earthZ)
        val moonY = moonOrbit * cos(Math.toRadians(moonAngle.toDouble())).toFloat()
        val moonZ = moonOrbit * sin(Math.toRadians(moonAngle.toDouble())).toFloat()
        Matrix.translateM(moonMatrix, 0, 0f, moonY, moonZ)
        val moonMVP = FloatArray(16)
        Matrix.multiplyMM(moonMVP, 0, mvpMatrix, 0, moonMatrix, 0)
        moon.draw(moonMVP)

        drawPlanet(mars, marsOrbit, marsAngle, mvpMatrix)
        drawPlanet(jupiter, jupiterOrbit, jupiterAngle, mvpMatrix)
        drawPlanet(saturn, saturnOrbit, saturnAngle, mvpMatrix)
        drawPlanet(uranus, uranusOrbit, uranusAngle, mvpMatrix)
        drawPlanet(neptune, neptuneOrbit, neptuneAngle, mvpMatrix)
    }

    private fun drawPlanet(
        planet: Sphere,
        orbitRadius: Float,
        angle: Float,
        mvpMatrix: FloatArray
    ) {
        val planetMatrix = FloatArray(16)
        Matrix.setIdentityM(planetMatrix, 0)

        val x = orbitRadius * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
        val z = orbitRadius * Math.sin(Math.toRadians(angle.toDouble())).toFloat()

        Matrix.translateM(planetMatrix, 0, x, 0f, z)

        val planetMVP = FloatArray(16)
        Matrix.multiplyMM(planetMVP, 0, mvpMatrix, 0, planetMatrix, 0)
        planet.draw(planetMVP)
    }

    fun selectNextPlanet() {
        selectedPlanetIndex = (selectedPlanetIndex + 1) % 9
    }
    fun selectPrevPlanet() {
        selectedPlanetIndex = (selectedPlanetIndex - 1 + 9) % 9
    }

    private fun getPlanetAngle(index: Int): Float = when(index) {
        0 -> mercuryAngle; 1 -> venusAngle; 2 -> earthAngle; 3 -> marsAngle
        4 -> jupiterAngle; 5 -> saturnAngle; 6 -> uranusAngle; 7 -> neptuneAngle
        8 -> moonAngle
        else -> 0f
    }

    fun drawSelectionCube(mvpMatrix: FloatArray, cube: Cube) {
        val planetRadius = planetRadii[selectedPlanetIndex]
        val cubeSize = planetRadius * 2.2f

        val cubeMatrix = FloatArray(16)
        Matrix.setIdentityM(cubeMatrix, 0)

        if (selectedPlanetIndex == 8) {
            val earthX = earthOrbit * cos(earthAngle * PI / 180.0).toFloat()
            val earthZ = earthOrbit * sin(earthAngle * PI / 180.0).toFloat()
            val moonY = moonOrbit * cos(Math.toRadians(moonAngle.toDouble())).toFloat()
            val moonZ = moonOrbit * sin(Math.toRadians(moonAngle.toDouble())).toFloat()

            Matrix.translateM(cubeMatrix, 0, earthX, moonY, earthZ + moonZ)
        } else {
            val orbit = planetOrbits[selectedPlanetIndex]
            val angle = getPlanetAngle(selectedPlanetIndex)
            val x = orbit * cos(angle * PI / 180.0).toFloat()
            val z = orbit * sin(angle * PI / 180.0).toFloat()
            Matrix.translateM(cubeMatrix, 0, x, 0f, z)
        }

        Matrix.scaleM(cubeMatrix, 0, cubeSize, cubeSize, cubeSize)
        Matrix.rotateM(cubeMatrix, 0, System.currentTimeMillis() * 0.001f, 1f, 1f, 0.3f)

        val cubeMVP = FloatArray(16)
        Matrix.multiplyMM(cubeMVP, 0, mvpMatrix, 0, cubeMatrix, 0)
        cube.draw(cubeMVP)
    }
}
