package com.ac10.tetrislol.ui

import android.graphics.Paint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ac10.tetrislol.R
import com.ac10.tetrislol.logic.Brick
import com.ac10.tetrislol.logic.NextMatrix
import com.ac10.tetrislol.logic.Tetrimino
import com.ac10.tetrislol.logic.TetriminoType
import com.ac10.tetrislol.logic.viewmodel.GameMainViewModel
import com.ac10.tetrislol.logic.viewmodel.GameStatus
import com.ac10.tetrislol.ui.theme.BrickMatrix
import com.ac10.tetrislol.ui.theme.BrickTetrimino
import com.ac10.tetrislol.ui.theme.ScreenBackground
import kotlin.math.min

@Composable
fun GameScreen(modifier: Modifier = Modifier) {

    val viewModel = viewModel<GameMainViewModel>()
    val viewState = viewModel.viewState.value

    Box(
        modifier
            .background(Color.Black)
            .padding(1.dp)
            .background(ScreenBackground)
            .padding(10.dp)
    ) {

        val animateValue by rememberInfiniteTransition().animateFloat(
            initialValue = 0f, targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()

        ) {

            val brickSize = min(
                size.width / viewState.matrix.first,
                size.height / viewState.matrix.second
            )

            drawMatrix(brickSize, viewState.matrix)
            drawMatrixBorder(brickSize, viewState.matrix)
            drawBricks(viewState.bricks, brickSize, viewState.matrix)
            drawSpirit(viewState.tetrimino, brickSize, viewState.matrix)
            drawText(viewState.gameStatus, brickSize, viewState.matrix, animateValue)

        }

        GameScoreboard(
            tetrimino = run {
                if (viewState.tetrimino == Tetrimino.Empty) Tetrimino.Empty
                else viewState.tetriminoNext.rotate()
            },
            score = viewState.score,
            line = viewState.line,
            level = viewState.level,
            isMute = viewState.isMute,
            isPaused = viewState.isPaused
        )
    }
}


@Composable
fun GameScoreboard(
    modifier: Modifier = Modifier,
    brickSize: Float = 35f,
    tetrimino: Tetrimino,
    score: Int = 0,
    line: Int = 0,
    level: Int = 1,
    isMute: Boolean = false,
    isPaused: Boolean = false
) {
    Row(modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(0.65f))
        val textSize = 12.sp
        val margin = 12.dp
        Column(
            Modifier
                .fillMaxHeight()
                .weight(0.35f)
        ) {
            Text("Score", fontSize = textSize)
            LEDNumber(Modifier.fillMaxWidth(), score, 6)

            Spacer(modifier = Modifier.height(margin))

            Text("Lines", fontSize = textSize)
            LEDNumber(Modifier.fillMaxWidth(), line, 6)

            Spacer(modifier = Modifier.height(margin))

            Text("Level", fontSize = textSize)
            LEDNumber(Modifier.fillMaxWidth(), level, 1)

            Spacer(modifier = Modifier.height(margin))

            Text("Next", fontSize = textSize)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
            ) {
                drawMatrix(brickSize, NextMatrix)
                drawSpirit(
                    tetrimino.adjustOffset(NextMatrix),
                    brickSize = brickSize, NextMatrix
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Row {
                Image(
                    modifier = Modifier.width(15.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_music_off_24),
                    colorFilter = ColorFilter.tint(if (isMute) BrickTetrimino else BrickMatrix),
                    contentDescription = null
                )
                Image(
                    modifier = Modifier.width(16.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_pause_24),
                    colorFilter = ColorFilter.tint(if (isPaused) BrickTetrimino else BrickMatrix),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.weight(1f))

                LEDClock()

            }
        }
    }
}


private fun DrawScope.drawText(
    gameStatus: GameStatus,
    brickSize: Float,
    matrix: Pair<Int, Int>,
    alpha: Float,
) {

    val center = Offset(
        brickSize * matrix.first / 2,
        brickSize * matrix.second / 2
    )
    val drawText = { text: String, size: Float ->

        drawIntoCanvas {
            it.nativeCanvas.drawText(
                text,
                center.x,
                center.y,
                Paint().apply {
                    color = Color.Black.copy(alpha = alpha).toArgb()
                    textSize = size
                    textAlign = Paint.Align.CENTER
                    style = Paint.Style.FILL_AND_STROKE
                    strokeWidth = size / 12
                }
            )

        }
    }
    if (gameStatus == GameStatus.Onboard) {
        drawText("TETRIS", 80f)
    } else if (gameStatus == GameStatus.GameOver) {
        drawText("GAME OVER", 60f)
    }
}

private fun DrawScope.drawMatrix(brickSize: Float, matrix: Pair<Int, Int>) {
    (0 until matrix.first).forEach { x ->
        (0 until matrix.second).forEach { y ->
            drawBrick(
                brickSize,
                Offset(x.toFloat(), y.toFloat()),
                BrickMatrix
            )
        }
    }
}

private fun DrawScope.drawMatrixBorder(brickSize: Float, matrix: Pair<Int, Int>) {

    val gap = matrix.first * brickSize * 0.05f
    drawRect(
        Color.Black,
        size = Size(
            matrix.first * brickSize + gap,
            matrix.second * brickSize + gap
        ),
        topLeft = Offset(
            -gap / 2,
            -gap / 2
        ),
        style = Stroke(1.dp.toPx())
    )

}

private fun DrawScope.drawBricks(brick: List<Brick>, brickSize: Float, matrix: Pair<Int, Int>) {
    clipRect(
        0f, 0f,
        matrix.first * brickSize,
        matrix.second * brickSize
    ) {
        brick.forEach {
            drawBrick(brickSize, it.location, BrickTetrimino)
        }
    }
}

private fun DrawScope.drawSpirit(tetrimino: Tetrimino, brickSize: Float, matrix: Pair<Int, Int>) {
    clipRect(
        0f, 0f,
        matrix.first * brickSize,
        matrix.second * brickSize
    ) {
        tetrimino.location.forEach {
            drawBrick(
                brickSize,
                Offset(it.x, it.y),
                BrickTetrimino
            )
        }
    }
}

private fun DrawScope.drawBrick(
    brickSize: Float,
    offset: Offset,
    color: Color
) {

    val actualLocation = Offset(
        offset.x * brickSize,
        offset.y * brickSize
    )

    val outerSize = brickSize * 0.8f
    val outerOffset = (brickSize - outerSize) / 2

    drawRect(
        color,
        topLeft = actualLocation + Offset(outerOffset, outerOffset),
        size = Size(outerSize, outerSize),
        style = Stroke(outerSize / 10)
    )

    val innerSize = brickSize * 0.5f
    val innerOffset = (brickSize - innerSize) / 2

    drawRect(
        color,
        actualLocation + Offset(innerOffset, innerOffset),
        size = Size(innerSize, innerSize)
    )

}


@Preview
@Composable
fun PreviewGamescreen(
    modifier: Modifier = Modifier
        .width(260.dp)
        .height(300.dp)
) {

    Box(
        modifier
            .background(Color.Black)
            .padding(1.dp)
            .background(ScreenBackground)
            .padding(10.dp)
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
        ) {

            val brickSize = min(
                size.width / 12,
                size.height / 24
            )

            drawMatrix(brickSize = brickSize, 12 to 24)
            drawMatrixBorder(brickSize = brickSize, 12 to 24)
        }

        val type = TetriminoType[6]
        GameScoreboard(
            tetrimino = Tetrimino(type, Offset(0f, 0f)).rotate(),
            score = 1204,
            line = 12
        )

    }


}

@Preview
@Composable
fun PreviewSpiritType() {
    Row(
        Modifier
            .size(300.dp, 50.dp)
            .background(ScreenBackground)
    ) {
        val matrix = 2 to 4
        TetriminoType.forEach {
            Canvas(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(5.dp)

            ) {
                drawBricks(
                    Brick.of(
                        Tetrimino(it).adjustOffset(matrix)
                    ), min(
                        size.width / matrix.first,
                        size.height / matrix.second
                    ), matrix
                )
            }
        }

    }

}



