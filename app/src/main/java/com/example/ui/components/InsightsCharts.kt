package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.insights.DailyInsightPoint
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OasisError
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SageGreen
import kotlin.math.roundToInt

@Composable
fun WeeklyRevenueLineChart(
  points: List<DailyInsightPoint>,
  selectedIndex: Int,
  onSelectDay: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (points.isEmpty()) return

  val textMeasurer = rememberTextMeasurer()
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val selectedLabelColor = MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
  val gridColor = OasisOutlineVariant.copy(alpha = 0.45f)

  val maxRevenue = (points.maxOfOrNull { it.revenueUsd } ?: 0L).coerceAtLeast(100L)
  val niceCeiling = computeNiceCeiling(maxRevenue)
  val totalWindowRevenue = points.sumOf { it.revenueUsd }

  val animProgress by
    animateFloatAsState(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
      label = "revenue_chart_reveal",
    )

  val axisTextStyle =
    TextStyle(
      fontFamily = InterFontFamily,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = labelColor,
    )

  Canvas(
    modifier =
      modifier
        .fillMaxWidth()
        .height(210.dp)
        .semantics {
          contentDescription =
            "Weekly revenue trend chart totaling $totalWindowRevenue dollars across ${points.size} days"
        }
        .pointerInput(points) {
          detectTapGestures { offset ->
            val leftPad = 42.dp.toPx()
            val rightPad = 14.dp.toPx()
            val usableWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth
            val tappedIdx =
              (((offset.x - leftPad) / stepX).roundToInt()).coerceIn(0, points.lastIndex)
            onSelectDay(tappedIdx)
          }
        }
  ) {
    val leftPad = 42.dp.toPx()
    val rightPad = 14.dp.toPx()
    val topPad = 16.dp.toPx()
    val bottomPad = 28.dp.toPx()

    val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
    val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
    val ySteps = listOf(0f, 0.5f, 1f)
    ySteps.forEach { fraction ->
      val y = topPad + chartH * (1f - fraction)
      drawLine(
        color = gridColor,
        start = Offset(leftPad, y),
        end = Offset(leftPad + chartW, y),
        strokeWidth = 0.85.dp.toPx(),
        pathEffect = if (fraction == 0f) null else dashEffect,
      )
      val valueLabel = "$${(niceCeiling * fraction).roundToInt()}"
      val measured = textMeasurer.measure(valueLabel, style = axisTextStyle)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(0f, y - measured.size.height / 2f),
      )
    }

    val coords =
      points.mapIndexed { idx, point ->
        val x =
          if (points.size == 1) leftPad + chartW / 2f
          else leftPad + (idx.toFloat() / (points.size - 1).toFloat()) * chartW
        val ratio = (point.revenueUsd.toFloat() / niceCeiling.toFloat()).coerceIn(0f, 1f)
        val y = topPad + chartH * (1f - ratio)
        Offset(x, y)
      }

    // Selected vertical guide line
    if (selectedIndex in coords.indices) {
      val sel = coords[selectedIndex]
      drawLine(
        color = RefinedGold.copy(alpha = 0.55f),
        start = Offset(sel.x, topPad),
        end = Offset(sel.x, topPad + chartH),
        strokeWidth = 1.25.dp.toPx(),
      )
    }

    val linePath = Path()
    val fillPath = Path()
    coords.forEachIndexed { idx, pt ->
      if (idx == 0) {
        linePath.moveTo(pt.x, pt.y)
        fillPath.moveTo(pt.x, topPad + chartH)
        fillPath.lineTo(pt.x, pt.y)
      } else {
        val prev = coords[idx - 1]
        val ctrlX = (prev.x + pt.x) / 2f
        linePath.cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
        fillPath.cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
      }
    }
    coords.lastOrNull()?.let { last ->
      fillPath.lineTo(last.x, topPad + chartH)
      fillPath.close()
    }

    clipRect(
      left = 0f,
      top = 0f,
      right = leftPad + chartW * animProgress + 16.dp.toPx(),
      bottom = size.height,
    ) {
      drawPath(
        path = fillPath,
        brush =
          Brush.verticalGradient(
            colors = listOf(SageGreen.copy(alpha = 0.36f), Color.Transparent),
            startY = topPad,
            endY = topPad + chartH,
          ),
      )
      drawPath(
        path = linePath,
        color = OasisPrimary,
        style =
          Stroke(
            width = 2.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
          ),
      )

      coords.forEachIndexed { idx, pt ->
        val isSelected = idx == selectedIndex
        if (isSelected) {
          drawCircle(
            color = RefinedGold.copy(alpha = 0.28f),
            radius = 10.dp.toPx(),
            center = pt,
          )
        }
        drawCircle(
          color = if (isSelected) RefinedGold else OasisPrimary,
          radius = if (isSelected) 5.5.dp.toPx() else 4.dp.toPx(),
          center = pt,
        )
        drawCircle(
          color = surfaceColor,
          radius = if (isSelected) 2.5.dp.toPx() else 1.8.dp.toPx(),
          center = pt,
        )
      }
    }

    // X-axis day labels
    points.forEachIndexed { idx, pt ->
      val isSelected = idx == selectedIndex
      val measured =
        textMeasurer.measure(
          pt.dayLabel,
          style =
            axisTextStyle.copy(
              color = if (isSelected) selectedLabelColor else labelColor,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            ),
        )
      val xCenter = coords[idx].x
      val drawX = (xCenter - measured.size.width / 2f).coerceIn(leftPad - 6.dp.toPx(), size.width - measured.size.width)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(drawX, topPad + chartH + 8.dp.toPx()),
      )
    }
  }
}

@Composable
fun WeeklyBookingVolumeBarChart(
  points: List<DailyInsightPoint>,
  selectedIndex: Int,
  onSelectDay: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (points.isEmpty()) return

  val textMeasurer = rememberTextMeasurer()
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val selectedLabelColor = MaterialTheme.colorScheme.primary
  val highlightTrackColor = SageGreen.copy(alpha = 0.14f)
  val gridColor = OasisOutlineVariant.copy(alpha = 0.45f)

  val maxCount = (points.maxOfOrNull { it.totalBookings } ?: 0).coerceAtLeast(4)
  val ceiling = if (maxCount <= 4) 4 else ((maxCount + 1) / 2) * 2
  val totalBookings = points.sumOf { it.totalBookings }

  val animProgress by
    animateFloatAsState(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
      label = "volume_chart_reveal",
    )

  val axisTextStyle =
    TextStyle(
      fontFamily = InterFontFamily,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = labelColor,
    )

  Canvas(
    modifier =
      modifier
        .fillMaxWidth()
        .height(200.dp)
        .semantics {
          contentDescription =
            "Weekly booking volume bar chart showing $totalBookings total bookings across ${points.size} days"
        }
        .pointerInput(points) {
          detectTapGestures { offset ->
            val leftPad = 28.dp.toPx()
            val rightPad = 10.dp.toPx()
            val usableWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val slotW = usableWidth / points.size.toFloat()
            val tappedIdx =
              ((offset.x - leftPad) / slotW).toInt().coerceIn(0, points.lastIndex)
            onSelectDay(tappedIdx)
          }
        }
  ) {
    val leftPad = 28.dp.toPx()
    val rightPad = 10.dp.toPx()
    val topPad = 18.dp.toPx()
    val bottomPad = 28.dp.toPx()

    val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
    val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()), 0f)
    listOf(0f, 0.5f, 1f).forEach { fraction ->
      val y = topPad + chartH * (1f - fraction)
      drawLine(
        color = gridColor,
        start = Offset(leftPad, y),
        end = Offset(leftPad + chartW, y),
        strokeWidth = 0.85.dp.toPx(),
        pathEffect = if (fraction == 0f) null else dashEffect,
      )
      val valueLabel = "${(ceiling * fraction).roundToInt()}"
      val measured = textMeasurer.measure(valueLabel, style = axisTextStyle)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(0f, y - measured.size.height / 2f),
      )
    }

    val slotW = chartW / points.size.toFloat()
    val barW = (slotW * 0.44f).coerceIn(14.dp.toPx(), 28.dp.toPx())
    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

    points.forEachIndexed { idx, pt ->
      val isSelected = idx == selectedIndex
      val slotLeft = leftPad + idx * slotW
      val barLeft = slotLeft + (slotW - barW) / 2f

      if (isSelected) {
        drawRoundRect(
          color = highlightTrackColor,
          topLeft = Offset(slotLeft + 3.dp.toPx(), topPad),
          size = Size(slotW - 6.dp.toPx(), chartH),
          cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        )
      }

      val activeRatio = (pt.activeBookings.toFloat() / ceiling.toFloat()).coerceIn(0f, 1f)
      val cancelledRatio = (pt.cancelledBookings.toFloat() / ceiling.toFloat()).coerceIn(0f, 1f)
      val activeH = chartH * activeRatio * animProgress
      val cancelledH = chartH * cancelledRatio * animProgress

      // Baseline stub when 0 bookings so the column stays visually anchored
      if (pt.totalBookings == 0) {
        drawRoundRect(
          color = OasisOutlineVariant.copy(alpha = 0.35f),
          topLeft = Offset(barLeft, topPad + chartH - 3.dp.toPx()),
          size = Size(barW, 3.dp.toPx()),
          cornerRadius = cornerRadius,
        )
      } else {
        // Active portion at bottom
        if (activeH > 0f) {
          drawRoundRect(
            brush =
              Brush.verticalGradient(
                colors =
                  if (isSelected) listOf(RefinedGold, OasisPrimary)
                  else listOf(SageGreen, OasisPrimary)
              ),
            topLeft = Offset(barLeft, topPad + chartH - activeH),
            size = Size(barW, activeH),
            cornerRadius = cornerRadius,
          )
        }
        // Cancelled portion stacked directly above
        if (cancelledH > 0f) {
          val gap = if (activeH > 0f) 2.dp.toPx() else 0f
          val cancelTop = (topPad + chartH - activeH - gap - cancelledH).coerceAtLeast(topPad)
          drawRoundRect(
            color = OasisError.copy(alpha = 0.72f),
            topLeft = Offset(barLeft, cancelTop),
            size = Size(barW, cancelledH),
            cornerRadius = cornerRadius,
          )
        }

        // Total count tag centered above bar
        val countMeasured =
          textMeasurer.measure(
            "${pt.totalBookings}",
            style =
              axisTextStyle.copy(
                color = if (isSelected) selectedLabelColor else labelColor,
                fontWeight = FontWeight.SemiBold,
              ),
          )
        val totalBarTop = (topPad + chartH - activeH - cancelledH).coerceAtLeast(topPad)
        drawText(
          textLayoutResult = countMeasured,
          topLeft =
            Offset(
              slotLeft + (slotW - countMeasured.size.width) / 2f,
              (totalBarTop - countMeasured.size.height - 2.dp.toPx()).coerceAtLeast(0f),
            ),
        )
      }

      val dayMeasured =
        textMeasurer.measure(
          pt.dayLabel,
          style =
            axisTextStyle.copy(
              color = if (isSelected) selectedLabelColor else labelColor,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            ),
        )
      drawText(
        textLayoutResult = dayMeasured,
        topLeft = Offset(slotLeft + (slotW - dayMeasured.size.width) / 2f, topPad + chartH + 8.dp.toPx()),
      )
    }
  }
}

private fun computeNiceCeiling(rawMax: Long): Long {
  if (rawMax <= 100L) return 100L
  val step =
    when {
      rawMax <= 300L -> 50L
      rawMax <= 1000L -> 100L
      rawMax <= 3000L -> 250L
      else -> 500L
    }
  return ((rawMax + step - 1) / step) * step
}
