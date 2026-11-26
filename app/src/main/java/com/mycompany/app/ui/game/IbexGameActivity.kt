package com.mycompany.app.ui.game

import android.content.Context
import android.content.Intent
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import com.mycompany.app.ui.components.Sym

/**
 * "Steibock-Sprung", the app's easter egg (2026-11-23): an ibex runs through the Alps; tap (or swipe up, space, ↑) to
 * jump over rocks, cowbells and milk churns, swipe down (or ↓) to duck under the golden eagle or to drop faster. The
 * speed grows with the score; day and night take turns. Opened from the bar on a page that failed for lack of a
 * connection, and by tapping the app version seven times in Settings > About. The best score is kept
 * ([ZtwoSettings.ibexBest]).
 *
 * The world is measured in units: 160 high (ground at [GROUND]); one unit is drawn [scale] pixels large.
 */
class IbexGameActivity : OpumComposeActivity() {

    private val resumed = mutableStateOf(true)

    override fun onPause() {
        resumed.value = false
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        resumed.value = true
    }

    @Composable
    override fun Content() {
        val game = remember { Game(ZtwoSettings.ibexBest.value) }
        val focus = remember { FocusRequester() }
        var frame by remember { mutableIntStateOf(0) }
        val active = resumed.value

        LaunchedEffect(game.state, active) {
            if (game.state != State.RUNNING || !active) return@LaunchedEffect
            var last = 0L
            while (game.state == State.RUNNING) {
                withFrameNanos { now ->
                    val dt = if (last == 0L) 0f else min((now - last) / 1_000_000_000f, 0.05f)
                    last = now
                    game.step(dt)
                    frame++
                }
            }
            if (game.best > ZtwoSettings.ibexBest.value) {
                ZtwoSettings.edit(this@IbexGameActivity) { set(ZtwoSettings.ibexBest, game.best) }
            }
        }
        // the game waits while the app is in the background
        LaunchedEffect(active) { if (!active && game.state == State.RUNNING) game.state = State.PAUSED }
        LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) {} }

        Box(
            Modifier.fillMaxSize()
                .focusRequester(focus).focusable()
                .onKeyEvent { e ->
                    when {
                        e.type == KeyEventType.KeyDown && (e.key == Key.Spacebar || e.key == Key.DirectionUp) -> { game.press(); true }
                        e.key == Key.DirectionDown -> { game.duck(e.type == KeyEventType.KeyDown); true }
                        else -> false
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        game.press()
                        var ducking = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if (!ducking && change.position.y - down.position.y > 24.dp.toPx()) {
                                ducking = true
                                game.duck(true)
                            }
                        }
                        if (ducking) game.duck(false)
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                @Suppress("UNUSED_EXPRESSION") frame
                game.draw(this)
            }
            // score, top right
            Column(Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(16.dp), horizontalAlignment = Alignment.End) {
                Text(
                    "%05d".format(game.score), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    color = game.ink(),
                )
                if (game.best > 0) Text(
                    stringResource(R.string.game_best) + " " + "%05d".format(game.best), fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp, color = game.ink().copy(alpha = 0.75f),
                )
            }
            IconButton(onClick = { finish() }, modifier = Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(4.dp)) {
                Icon(Sym.Close, contentDescription = stringResource(R.string.close), tint = game.ink())
            }
            // start / pause / game over
            if (game.state != State.RUNNING) {
                Column(Modifier.align(BiasAlignment(0f, -0.3f)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    when (game.state) {
                        State.READY -> Text(
                            stringResource(R.string.game_title), fontWeight = FontWeight.Bold, fontSize = 30.sp,
                            color = game.ink(), textAlign = TextAlign.Center,
                        )
                        State.OVER -> Text(
                            stringResource(R.string.game_over), fontWeight = FontWeight.Bold, fontSize = 30.sp,
                            color = Color(0xFFDA291C), textAlign = TextAlign.Center,
                        )
                        else -> Unit
                    }
                    Text(
                        stringResource(R.string.game_tap_start), fontSize = 17.sp, color = game.ink(),
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    enum class State { READY, RUNNING, PAUSED, OVER }

    private class Obstacle(val kind: Int, var x: Float, val y: Float, val w: Float, val h: Float)

    /** The game state and its drawing; all positions in world units. */
    private inner class Game(var best: Int) {
        var state by mutableStateOf(State.READY)
        var score by mutableIntStateOf(0)
        private var overAt = 0L
        private var distance = 0f
        private var speed = START_SPEED
        private var ibexY = 0f          // height of the feet above the ground
        private var ibexV = 0f
        private var ducking = false
        private var runClock = 0f
        private var nextGap = 120f
        private val obstacles = ArrayList<Obstacle>()
        private var nightness = 0f      // 0 day … 1 night, follows [night]
        private var night = false
        private var flash = 0f
        private var scale = 1f
        private var worldW = 300f
        private val clouds = FloatArray(6) { Random.nextFloat() * 400f }
        private val stars = Array(40) { floatArrayOf(Random.nextFloat(), Random.nextFloat() * 0.55f) }

        fun press() {
            when (state) {
                State.READY -> restart()
                State.OVER -> if (android.os.SystemClock.elapsedRealtime() - overAt > 600) restart()
                State.PAUSED -> state = State.RUNNING
                State.RUNNING -> if (ibexY <= 0f && !ducking) ibexV = JUMP
            }
        }

        fun duck(on: Boolean) {
            if (state != State.RUNNING) return
            ducking = on
        }

        private fun restart() {
            score = 0; distance = 0f; speed = START_SPEED; ibexY = 0f; ibexV = 0f; ducking = false
            obstacles.clear(); nextGap = 140f; night = false
            state = State.RUNNING
        }

        fun ink(): Color = lerp(Color(0xFF1B2A3A), Color(0xFFF4F1EA), nightness)

        fun step(dt: Float) {
            if (dt <= 0f) return
            speed = min(MAX_SPEED, START_SPEED + distance / 60f)
            val move = speed * dt
            distance += move
            val newScore = (distance / 8f).toInt()
            if (newScore / 100 > score / 100) flash = 1f
            score = newScore
            if (score > best) best = score
            night = (score / 700) % 2 == 1
            nightness += ((if (night) 1f else 0f) - nightness) * min(1f, dt * 1.5f)
            flash = max(0f, flash - dt * 2f)
            runClock += dt * (speed / 40f)
            for (i in clouds.indices) clouds[i] -= move * 0.15f
            // the ibex: gravity, faster drop while "duck" is held in the air
            if (ibexY > 0f || ibexV > 0f) {
                ibexV -= GRAVITY * dt * (if (ducking) 3f else 1f)
                ibexY += ibexV * dt
                if (ibexY <= 0f) { ibexY = 0f; ibexV = 0f }
            }
            // obstacles move left; a new one comes when the last is far enough
            for (o in obstacles) o.x -= move
            obstacles.removeAll { it.x + it.w < -20f }
            val lastX = obstacles.lastOrNull()?.x ?: -1000f
            if (worldW - lastX > nextGap) {
                obstacles.add(spawn(worldW + 10f))
                val minGap = speed * 0.7f + 70f
                nextGap = minGap + Random.nextFloat() * minGap
            }
            // collision, with slightly smaller boxes than the pictures
            val ih = if (ducking && ibexY <= 0f) DUCK_H else IBEX_H
            val iw = if (ducking && ibexY <= 0f) DUCK_W else IBEX_W
            val il = IBEX_X + 4f; val ir = IBEX_X + iw - 4f
            val ib = ibexY + 1f; val it = ibexY + ih - 3f
            for (o in obstacles) {
                if (o.x + 2f < ir && o.x + o.w - 2f > il && o.y + 2f < it && o.y + o.h - 2f > ib) {
                    state = State.OVER
                    overAt = android.os.SystemClock.elapsedRealtime()
                    return
                }
            }
        }

        private fun spawn(x: Float): Obstacle {
            val eagleAllowed = score > 250
            return when (if (eagleAllowed) Random.nextInt(5) else Random.nextInt(3)) {
                0 -> Obstacle(0, x, 0f, 16f, 11f)                     // rock
                1 -> Obstacle(1, x, 0f, 11f, 14f)                     // cowbell
                2 -> Obstacle(2, x, 0f, 10f, 16f)                     // milk churn
                else -> {
                    // eagle: low (jump), middle (duck) or high (pass under)
                    val y = when (Random.nextInt(3)) { 0 -> 4f; 1 -> 15f; else -> 30f }
                    Obstacle(3, x, y, 22f, 12f)
                }
            }
        }

        // ---- drawing ----

        fun draw(d: DrawScope) = with(d) {
            scale = min(size.height / 160f, size.width / 210f)
            worldW = size.width / scale
            // the ground line: lower on a tall (portrait) screen, so the sky and the mountains get the room
            val groundPx = if (size.height > size.width) size.height * 0.66f else size.height / 2f + 40f * scale
            fun sx(x: Float) = x * scale
            fun sy(y: Float) = groundPx - y * scale         // y = height above ground

            // sky
            val skyTop = lerp(Color(0xFF7EC8F4), Color(0xFF0B1A3A), nightness)
            val skyLow = lerp(Color(0xFFE6F4FC), Color(0xFF24365E), nightness)
            drawRect(Brush.verticalGradient(listOf(skyTop, skyLow), startY = 0f, endY = groundPx))
            // stars and moon at night, sun by day
            if (nightness > 0.05f) {
                for (s in stars) drawRect(Color.White.copy(alpha = nightness * 0.8f), Offset(s[0] * size.width, s[1] * groundPx), Size(scale, scale))
                drawCircle(Color(0xFFF7F3D6).copy(alpha = nightness), radius = 7f * scale, center = Offset(size.width * 0.78f, groundPx * 0.25f))
            }
            if (nightness < 0.95f) {
                drawCircle(Color(0xFFFFD34E).copy(alpha = 1f - nightness), radius = 8f * scale, center = Offset(size.width * 0.2f, groundPx * 0.22f))
            }
            // clouds
            for (i in clouds.indices) {
                if (clouds[i] < -40f) clouds[i] += worldW + 80f + Random.nextFloat() * 120f
                val cy = groundPx * (0.12f + 0.07f * (i % 3))
                drawCloud(Offset(sx(clouds[i]), cy), lerp(Color.White, Color(0xFF5A6C8F), nightness))
            }
            // far mountains (slow), the Matterhorn among them, then green hills (faster)
            val skyUnits = groundPx / scale
            drawRange(groundPx, distance * 0.08f, 420f, max(62f, skyUnits * 0.42f), lerp(Color(0xFF8FA6C2), Color(0xFF34466A), nightness), snow = true, matterhorn = true)
            drawRange(groundPx, distance * 0.25f, 260f, max(28f, skyUnits * 0.16f), lerp(Color(0xFF5FAF4E), Color(0xFF26502E), nightness), snow = false, matterhorn = false)
            // meadow and ground
            drawRect(
                Brush.verticalGradient(
                    listOf(lerp(Color(0xFF4E9A3C), Color(0xFF1E3E24), nightness), lerp(Color(0xFF2F6B26), Color(0xFF10261A), nightness)),
                    startY = groundPx, endY = size.height,
                ),
                Offset(0f, groundPx), Size(size.width, size.height - groundPx),
            )
            drawRect(lerp(Color(0xFF8A6A43), Color(0xFF4A3A28), nightness), Offset(0f, groundPx + 2f * scale), Size(size.width, 3f * scale))
            // a few edelweiss in the meadow, scrolling with the ground
            val flowerStep = 67f
            var fl = -(distance % flowerStep)
            var row = (distance / flowerStep).toInt()
            while (fl < worldW) {
                val depth = 10f + (row * 37 % 5) * ((size.height - groundPx) / scale - 14f) / 5f
                val c = Offset(sx(fl + (row * 13 % 20)), groundPx + depth * scale)
                drawCircle(lerp(Color.White, Color(0xFFAAB4C8), nightness), radius = 1.4f * scale, center = c)
                drawCircle(lerp(Color(0xFFF2C230), Color(0xFF8A7A3A), nightness), radius = 0.6f * scale, center = c)
                fl += flowerStep; row++
            }
            val tuftStep = 23f
            var tx = -(distance % tuftStep)
            while (tx < worldW) {
                drawRect(lerp(Color(0xFF3C7F2E), Color(0xFF173320), nightness), Offset(sx(tx), groundPx - scale), Size(2f * scale, scale))
                tx += tuftStep
            }
            // a Swiss flag on a pole every now and then
            val flagStep = 520f
            val fx = flagStep - (distance * 0.6f % flagStep) - 30f
            drawFlag(Offset(sx(fx), groundPx))

            // obstacles
            for (o in obstacles) {
                val sprite = when (o.kind) {
                    0 -> Sprites.rock
                    1 -> Sprites.cowbell
                    2 -> Sprites.churn
                    else -> if ((runClock * 2).toInt() % 2 == 0) Sprites.eagleUp else Sprites.eagleDown
                }
                drawSprite(sprite, sx(o.x), sy(o.y + o.h))
            }
            // the ibex
            val sprite = when {
                state == State.OVER -> Sprites.ibexHit
                ibexY > 0f -> Sprites.ibexJump
                ducking -> if (runClock.toInt() % 2 == 0) Sprites.ibexDuckA else Sprites.ibexDuckB
                state != State.RUNNING -> Sprites.ibexA
                else -> if (runClock.toInt() % 2 == 0) Sprites.ibexA else Sprites.ibexB
            }
            val h = if (ducking && ibexY <= 0f && state == State.RUNNING) DUCK_H else IBEX_H
            drawSprite(sprite, sx(IBEX_X), sy(ibexY + h))
            // every hundred points the score flashes the sky a little
            if (flash > 0f) drawRect(Color.White.copy(alpha = flash * 0.15f))
        }

        private fun DrawScope.drawSprite(sprite: ImageBitmap, x: Float, y: Float) {
            drawImage(
                sprite, dstOffset = IntOffset(x.toInt(), y.toInt()),
                dstSize = IntSize((sprite.width * scale).toInt(), (sprite.height * scale).toInt()),
                filterQuality = FilterQuality.None,
            )
        }

        private fun DrawScope.drawCloud(at: Offset, color: Color) {
            val u = scale
            drawRect(color, at, Size(18 * u, 4 * u))
            drawRect(color, at + Offset(4 * u, -3 * u), Size(9 * u, 3 * u))
            drawRect(color, at + Offset(2 * u, -1.5f * u), Size(14 * u, 2 * u))
        }

        private fun DrawScope.drawFlag(foot: Offset) {
            val u = scale
            drawRect(Color(0xFF9E9E9E), foot - Offset(0f, 30 * u), Size(u, 30 * u))
            val flag = foot + Offset(u, -30 * u)
            drawRect(Color(0xFFDA291C), flag, Size(10 * u, 10 * u))
            drawRect(Color.White, flag + Offset(4 * u, 2 * u), Size(2 * u, 6 * u))
            drawRect(Color.White, flag + Offset(2 * u, 4 * u), Size(6 * u, 2 * u))
        }

        /**
         * A mountain range as one path: peaks every [period] units, [height] high, scrolled by [shift]. Snow caps follow
         * the outline down from each summit, so their edges lie on the slopes.
         */
        private fun DrawScope.drawRange(groundPx: Float, shift: Float, period: Float, height: Float, color: Color, snow: Boolean, matterhorn: Boolean) {
            val u = scale
            val xs = ArrayList<Float>()
            val ys = ArrayList<Float>()
            val summits = ArrayList<Int>()     // index of each summit in xs / ys
            val caps = ArrayList<Float>()      // depth of its snow cap, in pixels
            fun vertex(x: Float, y: Float) { xs.add(x * u); ys.add(y) }
            vertex(0f, groundPx)
            var x = -(shift % period) - period
            var i = (shift / period).toInt()
            while (x < worldW + period) {
                // three peaks per period, of different heights; the first of every third period is the Matterhorn
                val horn = matterhorn && i % 3 == 0
                val peaks = if (horn) listOf(0.2f to 1.25f, 0.5f to 0.7f, 0.8f to 0.85f) else listOf(0.15f to 0.75f, 0.45f to 1f, 0.78f to 0.6f)
                var px = x
                for ((at, hf) in peaks) {
                    val peakX = x + at * period
                    val peakY = groundPx - height * hf * u
                    vertex(px, groundPx - height * 0.25f * u)
                    val crooked = horn && hf > 1.2f
                    if (crooked) vertex(peakX - 6f, peakY + 8f * u)   // the Matterhorn's hooked summit
                    summits.add(xs.size); caps.add(height * hf * 0.22f * u)
                    vertex(peakX, peakY)
                    if (crooked) vertex(peakX + 3f, peakY + 3f * u)
                    px = peakX + period * 0.15f
                }
                x += period
                i++
            }
            vertex(worldW, groundPx)
            val path = Path()
            path.moveTo(xs[0], ys[0])
            for (k in 1 until xs.size) path.lineTo(xs[k], ys[k])
            path.close()
            drawPath(path, color)
            if (!snow) return
            val snowPath = Path()
            for ((n, top) in summits.withIndex()) {
                val line = ys[top] + caps[n]
                // from the summit along the outline to where it reaches the snow line, on either side
                fun edge(dir: Int, into: MutableList<Offset>) {
                    var k = top
                    while (k + dir in xs.indices) {
                        val nk = k + dir
                        if (ys[nk] >= line) {
                            val f = (line - ys[k]) / (ys[nk] - ys[k])
                            into.add(Offset(xs[k] + (xs[nk] - xs[k]) * f, line))
                            return
                        }
                        into.add(Offset(xs[nk], ys[nk]))
                        k = nk
                    }
                }
                val left = ArrayList<Offset>(); val right = ArrayList<Offset>()
                edge(-1, left); edge(1, right)
                if (left.isEmpty() || right.isEmpty()) continue
                snowPath.moveTo(xs[top], ys[top])
                for (p in right) snowPath.lineTo(p.x, p.y)
                // a ragged lower edge, a little above and below the snow line
                val r = right.last(); val l = left.last()
                for ((t, f) in listOf(0.25f to 1.18f, 0.5f to 0.82f, 0.75f to 1.12f)) {
                    snowPath.lineTo(r.x + (l.x - r.x) * t, ys[top] + caps[n] * f)
                }
                for (p in left.asReversed()) snowPath.lineTo(p.x, p.y)
                snowPath.close()
            }
            drawPath(snowPath, lerp(Color.White, Color(0xFFB8C4DA), nightness))
        }
    }

    companion object {
        private const val START_SPEED = 120f
        private const val MAX_SPEED = 300f
        private const val GRAVITY = 1500f
        private const val JUMP = 330f
        private const val IBEX_X = 22f
        private const val IBEX_W = 29f
        private const val IBEX_H = 23f
        private const val DUCK_W = 31f
        private const val DUCK_H = 14f

        fun open(context: Context) {
            context.startActivity(Intent(context, IbexGameActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

/** The pictures, as small pixel maps ('.' = clear); one map character per colour. */
private object Sprites {
    private val palette = mapOf(
        'B' to 0xFF9C8576.toInt(),   // ibex grey brown
        'D' to 0xFF6E5A4D.toInt(),   // shade, far legs, beard
        'L' to 0xFFBCAA9A.toInt(),   // light belly
        'H' to 0xFF5E4A3A.toInt(),   // horn
        'h' to 0xFF3A2D23.toInt(),   // horn ridges
        'K' to 0xFF1A1A1A.toInt(),   // eye, hooves
        'W' to 0xFFFFFFFF.toInt(),
        'G' to 0xFF8C8C8C.toInt(),   // rock grey
        'g' to 0xFF6B6B6B.toInt(),
        'S' to 0xFFF2F6FA.toInt(),   // snow
        'Y' to 0xFFE0A526.toInt(),   // bell gold
        'y' to 0xFFB57F12.toInt(),
        'R' to 0xFFDA291C.toInt(),   // Swiss red
        'M' to 0xFFC9D1D9.toInt(),   // churn metal
        'm' to 0xFF8E99A4.toInt(),
        'E' to 0xFF6B4423.toInt(),   // eagle brown
        'e' to 0xFF4A2E17.toInt(),
        'O' to 0xFFE8B04A.toInt(),   // eagle head gold
        'P' to 0xFFF2C230.toInt(),   // beak
    )

    private fun bitmap(rows: List<String>): ImageBitmap {
        val w = rows.maxOf { it.length }
        val bmp = createBitmap(w, rows.size)
        for ((y, row) in rows.withIndex()) for ((x, c) in row.withIndex()) {
            val color = palette[c] ?: continue
            bmp[x, y] = color
        }
        return bmp.asImageBitmap()
    }

    val ibexA = bitmap(listOf(
        "..............hhh............",
        "............hhHHHhhhh........",
        "...........hhH...HHHHH.......",
        "...........hH.......hH.......",
        "..........hH........hHH......",
        "..........hH.........hHH.....",
        "..........hH.........hDD.....",
        "...........h.........DBBBB...",
        "....................DBBBKBBB.",
        ".....BBBBBBBBBBBBBBBBBBBBBBBD",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBBD",
        "..DDBBBBBBBBBBBBBBBBBBB.DDD..",
        "...DBBBBBBBBBBBBBBBBBBB..DD..",
        "...DBBBBBBBBBBBBBBBBBBB..D...",
        "...DBBBBBBBBBBBBBBBB.........",
        "....LLLLLLLLLLLLLLL..........",
        ".....BBDLLLLLLLLD.BB.........",
        ".....BBD........D.BB.........",
        ".....BBD........D.BB.........",
        "....BB..D......D...BB........",
        "....BB..D......D...BB........",
        "....BB..D......D...BB........",
        "....KK..K......K...KK........",
    ))
    val ibexB = bitmap(listOf(
        "..............hhh............",
        "............hhHHHhhhh........",
        "...........hhH...HHHHH.......",
        "...........hH.......hH.......",
        "..........hH........hHH......",
        "..........hH.........hHH.....",
        "..........hH.........hDD.....",
        "...........h.........DBBBB...",
        "....................DBBBKBBB.",
        ".....BBBBBBBBBBBBBBBBBBBBBBBD",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBBD",
        "..DDBBBBBBBBBBBBBBBBBBB.DDD..",
        "...DBBBBBBBBBBBBBBBBBBB..DD..",
        "...DBBBBBBBBBBBBBBBBBBB..D...",
        "...DBBBBBBBBBBBBBBBB.........",
        "....LLLLLLLLLLLLLLL..........",
        "......DBBLLLLLLLLDBB.........",
        "......DBB........DBB.........",
        "......DBB........DBB.........",
        "......DBB........DBB.........",
        "......DBB........DBB.........",
        "......DBB........DBB.........",
        "......KKK........KKK.........",
    ))
    val ibexJump = bitmap(listOf(
        "..............hhh............",
        "............hhHHHhhhh........",
        "...........hhH...HHHHH.......",
        "...........hH.......hH.......",
        "..........hH........hHH......",
        "..........hH.........hHH.....",
        "..........hH.........hDD.....",
        "...........h.........DBBBB...",
        "....................DBBBKBBB.",
        ".....BBBBBBBBBBBBBBBBBBBBBBBD",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBBD",
        "..DDBBBBBBBBBBBBBBBBBBB.DDD..",
        "...DBBBBBBBBBBBBBBBBBBB..DD..",
        "...DBBBBBBBBBBBBBBBBBBB..D...",
        "...DBBBBBBBBBBBBBBBB.........",
        "....LLLLLLLLLLLLLLL..........",
        "....BB.LLLLLLLLLL.BBB........",
        "...BB..............BBB.......",
        "..BB.................KK......",
        "..KK.........................",
        ".............................",
        ".............................",
        ".............................",
    ))
    val ibexHit = bitmap(listOf(
        "..............hhh............",
        "............hhHHHhhhh........",
        "...........hhH...HHHHH.......",
        "...........hH.......hH.......",
        "..........hH........hHH......",
        "..........hH.........hHH.....",
        "..........hH.........hDD.....",
        "...........h.........DBBBB...",
        "....................DBBBWBBB.",
        ".....BBBBBBBBBBBBBBBBBBBBBBBD",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBBD",
        "..DDBBBBBBBBBBBBBBBBBBB.DDD..",
        "...DBBBBBBBBBBBBBBBBBBB..DD..",
        "...DBBBBBBBBBBBBBBBBBBB..D...",
        "...DBBBBBBBBBBBBBBBB.........",
        "....LLLLLLLLLLLLLLL..........",
        ".....BBDLLLLLLLLD.BB.........",
        ".....BBD........D.BB.........",
        ".....BBD........D.BB.........",
        "....BB..D......D...BB........",
        "....BB..D......D...BB........",
        "....BB..D......D...BB........",
        "....KK..K......K...KK........",
    ))
    val ibexDuckA = bitmap(listOf(
        "...............................",
        "................hhhhhhh........",
        ".............hhhHHHHHHHHH......",
        "...........hhHHH........hH.....",
        ".....BBBBBBBBBBBBBBBBBB.BBBBB..",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBKBBB",
        "...DBBBBBBBBBBBBBBBBBBBBBBBBBBB",
        "...DBBBBBBBBBBBBBBBBBBBBBBBBBBD",
        "...DBBBBBBBBBBBBBBBBBBBBB..DDD.",
        "....BLLLLLLLLLLLLLLLLLLBB...DD.",
        "....BB.B..........B.B..........",
        "....BB..BB.......B...BB........",
        "....BB..BB......BB...BB........",
        "....KK..KK......KK...KK........",
    ))
    val ibexDuckB = bitmap(listOf(
        "...............................",
        "................hhhhhhh........",
        ".............hhhHHHHHHHHH......",
        "...........hhHHH........hH.....",
        ".....BBBBBBBBBBBBBBBBBB.BBBBB..",
        ".DD..BBBBBBBBBBBBBBBBBBBBBBKBBB",
        "...DBBBBBBBBBBBBBBBBBBBBBBBBBBB",
        "...DBBBBBBBBBBBBBBBBBBBBBBBBBBD",
        "...DBBBBBBBBBBBBBBBBBBBBB..DDD.",
        "....BLLLLLLLLLLLLLLLLLLBB...DD.",
        "......BB...........BB..........",
        "......BB...........BB..........",
        "......BB...........BB..........",
        "......KK...........KK..........",
    ))

    val rock = bitmap(listOf(
        "......SSSS......",
        "....SSSSSSS.....",
        "...SGSSSGGSS....",
        "..GGGGSGGGGGG...",
        "..GGgGGGGGgGGG..",
        ".GGGGGGgGGGGGG..",
        ".GgGGGGGGGGGgGG.",
        "GGGGGgGGGGGGGGG.",
        "GGGGGGGGGGgGGGGG",
        "GgGGGGGGGGGGGGGG",
        "gggggggggggggggg",
    ))
    val cowbell = bitmap(listOf(
        "....RRR....",
        "...RWRWR...",
        "...R...R...",
        "...yyyyy...",
        "..yYYYYYy..",
        "..YYYYYYY..",
        "..YYYWYYY..",
        ".yYYYYYYYy.",
        ".YYYYYYYYY.",
        ".YYYYYYYYY.",
        "yYYYYYYYYYy",
        "YYYYYYYYYYY",
        "yyyyyyyyyyy",
        "....mKm....",
    ))
    val churn = bitmap(listOf(
        "...mMMm...",
        "..mMMMMm..",
        "...MMMM...",
        "...MMMM...",
        "..mMMMMm..",
        ".mMMMMMMm.",
        "mMMMMMMMMm",
        "MMMWMMMMMM",
        "MMMWMMMMMM",
        "mmmmmmmmmm",
        "MRRRRRRRRM",
        "MRRWWRRRRM",
        "MMMMMMMMMM",
        "MMMMMMMMMM",
        "mMMMMMMMMm",
        "mmmmmmmmmm",
    ))
    val eagleUp = bitmap(listOf(
        "eE....................",
        ".eEE..................",
        "..eEEE................",
        "...eEEEE.......OO.....",
        "....eEEEEE...OOOOO....",
        ".....EEEEEEEEOOKOPP...",
        "......EEEEEEEEOOO.PP..",
        "....EEEEEEEEEEE.......",
        "..eEEEE..EEEEE........",
        ".........E..E.........",
        "........PP..PP........",
        "......................",
    ))
    val eagleDown = bitmap(listOf(
        "......................",
        "...............OO.....",
        ".............OOOOO....",
        ".....EEEEEEEEOOKOPP...",
        "....EEEEEEEEEEOOO.PP..",
        "...eEEEEEEEEEEE.......",
        "..eEEEE..EEEEEE.......",
        ".eEEE.....eEEEE.......",
        "eEE........eEEE.......",
        "eE..........eEE.......",
        "..............eE......",
        "......................",
    ))
}
