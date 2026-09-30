package cards.sleevd.pixelate

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

public const val DefaultBlockRatio: Float = 0.22f

public const val MinimumSafeBlockRatio: Float = 0.18f

public fun Modifier.pixelate(
    blockRatio: Float = DefaultBlockRatio,
    minBlockSize: Dp = 4.dp,
    maxBlockSize: Dp = 14.dp,
    featherEdges: Boolean = true,
): Modifier = drawWithCache {
    val srcWidth = size.width
    val srcHeight = size.height

    if (srcWidth < 1f || srcHeight < 1f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    val cell = (srcHeight * blockRatio).coerceIn(minBlockSize.toPx(), maxBlockSize.toPx())
    val coarseWidth = (srcWidth / cell).roundToInt().coerceAtLeast(1)
    val coarseHeight = (srcHeight / cell).roundToInt().coerceAtLeast(1)

    val fineWidth = coarseWidth * 2
    val fineHeight = coarseHeight * 2

    val coarse = ImageBitmap(coarseWidth, coarseHeight)
    val fine = ImageBitmap(fineWidth, fineHeight)
    val coarseCanvas = Canvas(coarse)
    val fineCanvas = Canvas(fine)
    val coarseSize = Size(coarseWidth.toFloat(), coarseHeight.toFloat())
    val fineSize = Size(fineWidth.toFloat(), fineHeight.toFloat())

    onDrawWithContent {
        val contentScope = this

        draw(this, layoutDirection, coarseCanvas, coarseSize) {
            drawRect(color = Color.Transparent, blendMode = BlendMode.Src)
            scale(
                scaleX = coarseWidth / srcWidth,
                scaleY = coarseHeight / srcHeight,
                pivot = Offset.Zero,
            ) {
                contentScope.drawContent()
            }
        }

        draw(this, layoutDirection, fineCanvas, fineSize) {
            drawImage(
                image = coarse,
                srcSize = IntSize(coarseWidth, coarseHeight),
                dstSize = IntSize(fineWidth, fineHeight),
                filterQuality = FilterQuality.Low,
                blendMode = BlendMode.Src,
            )
        }

        if (!featherEdges) {
            drawImage(
                image = fine,
                srcSize = IntSize(fineWidth, fineHeight),
                dstSize = IntSize(srcWidth.roundToInt(), srcHeight.roundToInt()),
                filterQuality = FilterQuality.None,
            )
            return@onDrawWithContent
        }

        drawContext.canvas.saveLayer(Rect(0f, 0f, srcWidth, srcHeight), Paint())
        drawImage(
            image = fine,
            srcSize = IntSize(fineWidth, fineHeight),
            dstSize = IntSize(srcWidth.roundToInt(), srcHeight.roundToInt()),
            filterQuality = FilterQuality.None,
        )
        val feather = cell
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = feather,
                ),
            size = Size(feather, srcHeight),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = srcWidth - feather,
                    endX = srcWidth,
                ),
            topLeft = Offset(srcWidth - feather, 0f),
            size = Size(feather, srcHeight),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush =
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = feather,
                ),
            size = Size(srcWidth, feather),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush =
                Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = srcHeight - feather,
                    endY = srcHeight,
                ),
            topLeft = Offset(0f, srcHeight - feather),
            size = Size(srcWidth, feather),
            blendMode = BlendMode.DstIn,
        )
        drawContext.canvas.restore()
    }
}

/**
 * Renders [content] pixelated into a mosaic of solid blocks.
 *
 * ```kotlin
 * Pixelate {
 *     Column {
 *         Text(total, style = MaterialTheme.typography.headlineMedium)
 *         Text(change, style = MaterialTheme.typography.labelSmall)
 *     }
 * }
 * ```
 *
 * The mosaic covers the composed size of [content], so the cell grid does not restart per child.
 * Use [Modifier.pixelate] directly if you already have a node to hang a modifier on.
 *
 * @param modifier Applied to the container, outside the pixelation. Padding or a background set
 *   here is not pixelated.
 * @param blockRatio Cell size as a fraction of content height. Bigger is blockier. Below
 *   [MinimumSafeBlockRatio], text starts to become legible again.
 * @param minBlockSize Stops small content collapsing into one cell.
 * @param maxBlockSize Stops tall content becoming a few huge squares.
 * @param featherEdges Fades alpha to zero at the edges instead of hard-clipping.
 * @param content The real content, rendered with real data and then obscured.
 */
@Composable
public fun Pixelate(
    modifier: Modifier = Modifier,
    blockRatio: Float = DefaultBlockRatio,
    minBlockSize: Dp = 4.dp,
    maxBlockSize: Dp = 14.dp,
    featherEdges: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier.pixelate(
                blockRatio = blockRatio,
                minBlockSize = minBlockSize,
                maxBlockSize = maxBlockSize,
                featherEdges = featherEdges,
            )
    ) {
        content()
    }
}
