package com.gemora.prismquest

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 12, 35)
        window.navigationBarColor = Color.rgb(12, 12, 35)
        setContentView(GameView(this))
    }
}

class GameView(context: android.content.Context) : View(context) {

    private val n = 6
    private val gems = Array(n) { IntArray(n) }
    private val colors = intArrayOf(
        Color.rgb(255, 65, 100),
        Color.rgb(40, 190, 255),
        Color.rgb(75, 235, 145),
        Color.rgb(255, 195, 45),
        Color.rgb(180, 95, 255),
        Color.rgb(255, 125, 35)
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var score = 0
    private var level = 1
    private var selectedR = -1
    private var selectedC = -1
    private var cell = 0f
    private var startX = 0f
    private var startY = 0f
    private var boardX = 0f
    private var boardY = 0f

    init {
        resetBoard()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun resetBoard() {
        for (r in 0 until n) {
            for (c in 0 until n) {
                var g: Int
                do {
                    g = Random.nextInt(6)
                } while (
                    (c >= 2 && gems[r][c-1] == g && gems[r][c-2] == g) ||
                    (r >= 2 && gems[r-1][c] == g && gems[r-2][c] == g)
                )
                gems[r][c] = g
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(12, 12, 35))

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = width * 0.085f
        paint.typeface = android.graphics.Typeface.create(
            "sans-serif-black", android.graphics.Typeface.BOLD
        )
        canvas.drawText("GEMORA", width / 2f, height * 0.09f, paint)

        paint.color = Color.rgb(170, 145, 255)
        paint.textSize = width * 0.032f
        canvas.drawText("PRISM QUEST", width / 2f, height * 0.125f, paint)

        paint.color = Color.rgb(35, 35, 75)
        val info = RectF(width*.07f, height*.17f, width*.93f, height*.25f)
        canvas.drawRoundRect(info, 22f, 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = width * .042f
        canvas.drawText("LEVEL $level", width*.28f, height*.22f, paint)
        canvas.drawText("SCORE: $score", width*.72f, height*.22f, paint)

        cell = width * .135f
        val boardSize = cell * n
        boardX = (width - boardSize) / 2f
        boardY = height * .32f

        paint.color = Color.rgb(32, 30, 68)
        canvas.drawRoundRect(
            RectF(boardX-9, boardY-9, boardX+boardSize+9,
                boardY+boardSize+9), 20f, 20f, paint
        )

        for (r in 0 until n) {
            for (c in 0 until n) {
                val x = boardX + c*cell
                val y = boardY + r*cell

                paint.color = Color.rgb(48, 45, 88)
                canvas.drawRoundRect(
                    RectF(x+3, y+3, x+cell-3, y+cell-3),
                    12f, 12f, paint
                )

                if (gems[r][c] in 0..5) {
                    val radius = cell * .34f
                    paint.color = colors[gems[r][c]]
                    canvas.drawCircle(
                        x+cell/2, y+cell/2, radius, paint
                    )

                    paint.color = Color.WHITE
                    paint.alpha = 110
                    canvas.drawCircle(
                        x+cell*.39f, y+cell*.38f,
                        radius*.25f, paint
                    )
                    paint.alpha = 255
                }

                if (selectedR == r && selectedC == c) {
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 5f
                    canvas.drawRoundRect(
                        RectF(x+3, y+3, x+cell-3, y+cell-3),
                        12f, 12f, paint
                    )
                    paint.style = Paint.Style.FILL
                }
            }
        }

        paint.color = Color.rgb(190, 175, 255)
        paint.textSize = width * .037f
        canvas.drawText(
            "MATCH 3 OR MORE GEMS!",
            width/2f, boardY+boardSize+height*.07f, paint
        )

        paint.color = Color.WHITE
        paint.textSize = width * .032f
        canvas.drawText(
            "Touch two nearby gems to swap",
            width/2f, boardY+boardSize+height*.105f, paint
        )
    }

    private fun findMatches(): Set<Pair<Int, Int>> {
        val found = mutableSetOf<Pair<Int, Int>>()

        for (r in 0 until n) {
            for (c in 0 until n-2) {
                val g = gems[r][c]
                if (g >= 0 && gems[r][c+1] == g && gems[r][c+2] == g) {
                    var k = c
                    while (k < n && gems[r][k] == g) {
                        found.add(r to k)
                        k++
                    }
                }
            }
        }

        for (c in 0 until n) {
            for (r in 0 until n-2) {
                val g = gems[r][c]
                if (g >= 0 && gems[r+1][c] == g && gems[r+2][c] == g) {
                    var k = r
                    while (k < n && gems[k][c] == g) {
                        found.add(k to c)
                        k++
                    }
                }
            }
        }
        return found
    }

    private fun clearMatches() {
        var matches = findMatches()

        while (matches.isNotEmpty()) {
            score += matches.size * 10

            for ((r, c) in matches) gems[r][c] = -1

            for (c in 0 until n) {
                var write = n-1
                for (r in n-1 downTo 0) {
                    if (gems[r][c] >= 0) {
                        gems[write][c] = gems[r][c]
                        write--
                    }
                }
                while (write >= 0) {
                    gems[write][c] = Random.nextInt(6)
                    write--
                }
            }
            matches = findMatches()
        }

        level = 1 + score / 500
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val c1 = ((startX-boardX)/cell).toInt()
                val r1 = ((startY-boardY)/cell).toInt()
                val c2 = ((event.x-boardX)/cell).toInt()
                val r2 = ((event.y-boardY)/cell).toInt()

                if (r1 in 0 until n && c1 in 0 until n &&
                    r2 in 0 until n && c2 in 0 until n) {

                    if (kotlin.math.abs(r1-r2) +
                        kotlin.math.abs(c1-c2) == 1) {

                        val temp = gems[r1][c1]
                        gems[r1][c1] = gems[r2][c2]
                        gems[r2][c2] = temp

                        if (findMatches().isNotEmpty()) {
                            clearMatches()
                        } else {
                            val back = gems[r1][c1]
                            gems[r1][c1] = gems[r2][c2]
                            gems[r2][c2] = back
                        }
                    }
                }
                invalidate()
                return true
            }
        }
        return true
    }
}
