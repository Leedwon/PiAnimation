import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val width = 800.dp
private val height = 600.dp
private val radius = 128.dp

private val centerPointRadius = 2.dp

private const val INNER_SHIFT = -PI / 2
private const val OUTER_SHIFT = -49 * PI / 36

private val innerColor = Color.Blue
private val outerColor = Color.Green

fun main() = application {
    val state = rememberWindowState(size = DpSize(width, height))

    Window(onCloseRequest = ::exitApplication, state = state) {
        App()
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Box {
                val widthPx = with(LocalDensity.current) { width.toPx() }
                val heightPx = with(LocalDensity.current) { height.toPx() }
                val radius = with(LocalDensity.current) { radius.toPx() }
                val centerPointRadius = with(LocalDensity.current) { centerPointRadius.toPx() }

                val startX = widthPx / 2
                val startY = heightPx / 2

                val startOffset = Offset(startX, startY)

                val points = remember { mutableListOf<Offset>() }

                Sketch(timeSpeed = 5f) { time ->
                    val inner = calculateInnerFunction(
                        time = -time,
                        shift = INNER_SHIFT.toFloat()
                    ).times(radius)
                    val outer = calculateOuterFunction(
                        time = -time,
                        shift = OUTER_SHIFT.toFloat()
                    ).times(radius)

                    val innerOffset = inner.toOffset(
                        startX = startX,
                        startY = startY
                    )

                    val outerOffset = outer.toOffset(
                        startX = innerOffset.x,
                        startY = innerOffset.y
                    )

                    points.add(outerOffset)

                    drawCircle(
                        color = innerColor,
                        center = startOffset,
                        radius = centerPointRadius
                    )

                    drawLine(
                        color = innerColor,
                        start = startOffset,
                        end = innerOffset
                    )

                    drawLine(
                        color = outerColor,
                        start = innerOffset,
                        end = outerOffset
                    )

                    drawPoints(points)
                }
            }
        }
    }
}

private fun DrawScope.drawPoints(points: MutableList<Offset>) {
    points.zipWithNext().forEach { (start, end) ->
        drawLine(
            color = outerColor,
            start = start,
            end = end
        )
    }
}

@Composable
private fun Sketch(
    timeSpeed: Float = 1f,
    onDraw: DrawScope.(Float) -> Unit
) {
    var firstRun by remember { mutableStateOf(true) }
    var spec by remember { mutableStateOf(animSpecWithDelay) }

    val time = remember { AnimationState(0f) }

    LaunchedEffect(Unit) {
        while (isActive) {
            time.animateTo(
                targetValue = time.value + timeSpeed,
                animationSpec = spec,
                sequentialAnimation = true
            )
            if (firstRun) {
                spec = animSpecNoDelay
                firstRun = false
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .drawBehind {
                onDraw(time.value)
            }
    ) {}
}

// e^(t*i)
private fun calculateInnerFunction(
    time: Float,
    shift: Float = 0f
): Pair<Float, Float> {
    val x = cos(time + shift)
    val y = sin(time + shift)

    return x to y
}

// e^(pi*t*i)
private fun calculateOuterFunction(
    time: Float,
    shift: Float = 0f
): Pair<Float, Float> {
    val x = cos(PI * time + shift).toFloat()
    val y = sin(PI * time + shift).toFloat()

    return x to y
}

private fun Pair<Float, Float>.toOffset(startX: Float, startY: Float): Offset {
    return Offset(startX + first, startY + second)
}

private fun Pair<Float, Float>.times(value: Float): Pair<Float, Float> {
    return first * value to second * value
}

private val animSpecWithDelay: AnimationSpec<Float> = tween(5000, 5000, LinearEasing)
private val animSpecNoDelay: AnimationSpec<Float> = tween(5000, 0, LinearEasing)