package com.example.ui.insights

import com.example.data.model.SpaBooking
import com.example.ui.screens.currentTodayIso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class InsightsWindow(val label: String) {
  LAST_7_DAYS("Past 7 Days"),
  THIS_WEEK("This Week"),
  NEXT_7_DAYS("Next 7 Days"),
}

data class DailyInsightPoint(
  val dateIso: String,
  val dayLabel: String,
  val fullDateLabel: String,
  val revenueUsd: Long,
  val activeBookings: Int,
  val cancelledBookings: Int,
) {
  val totalBookings: Int
    get() = activeBookings + cancelledBookings
}

data class CategoryInsight(
  val category: String,
  val revenueUsd: Long,
  val bookingCount: Int,
  val shareFraction: Float,
)

data class TherapistInsight(
  val therapistName: String,
  val revenueUsd: Long,
  val bookingCount: Int,
)

data class BusinessInsightsSummary(
  val window: InsightsWindow,
  val dailyPoints: List<DailyInsightPoint>,
  val windowRevenueUsd: Long,
  val windowActiveBookings: Int,
  val windowCancelledBookings: Int,
  val averageTicketUsd: Long,
  val totalAllTimeRevenueUsd: Long,
  val totalAllTimeActiveBookings: Int,
  val retentionRatePercent: Int,
  val peakDayLabel: String?,
  val peakDayRevenueUsd: Long,
  val categoryBreakdown: List<CategoryInsight>,
  val therapistLeaderboard: List<TherapistInsight>,
)

fun pickDefaultInsightsWindow(
  bookings: List<SpaBooking>,
  todayIso: String = currentTodayIso(),
): InsightsWindow {
  val last7Dates = buildWindowDates(InsightsWindow.LAST_7_DAYS, todayIso).map { it.first }.toSet()
  if (bookings.any { it.dateIso in last7Dates }) return InsightsWindow.LAST_7_DAYS

  val next7Dates = buildWindowDates(InsightsWindow.NEXT_7_DAYS, todayIso).map { it.first }.toSet()
  if (bookings.any { it.dateIso in next7Dates }) return InsightsWindow.NEXT_7_DAYS

  return InsightsWindow.THIS_WEEK
}

fun buildWindowDates(
  window: InsightsWindow,
  todayIso: String = currentTodayIso(),
): List<Triple<String, String, String>> {
  val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val shortDayFmt = SimpleDateFormat("EEE", Locale.US)
  val fullDateFmt = SimpleDateFormat("EEE, MMM d", Locale.US)

  val baseCal =
    Calendar.getInstance(Locale.US).apply {
      val parsed = runCatching { isoFmt.parse(todayIso) }.getOrNull()
      if (parsed != null) {
        time = parsed
      }
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

  val startCal =
    (baseCal.clone() as Calendar).apply {
      when (window) {
        InsightsWindow.LAST_7_DAYS -> add(Calendar.DAY_OF_YEAR, -6)
        InsightsWindow.NEXT_7_DAYS -> {
          // Starts on todayIso and covers today..today+6
        }
        InsightsWindow.THIS_WEEK -> {
          firstDayOfWeek = Calendar.MONDAY
          val dayOfWeek = get(Calendar.DAY_OF_WEEK)
          val diffFromMonday =
            when (dayOfWeek) {
              Calendar.SUNDAY -> -6
              else -> Calendar.MONDAY - dayOfWeek
            }
          add(Calendar.DAY_OF_YEAR, diffFromMonday)
        }
      }
    }

  return (0 until 7).map { offset ->
    val dayCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
    val date = dayCal.time
    Triple(isoFmt.format(date), shortDayFmt.format(date), fullDateFmt.format(date))
  }
}

fun computeBusinessInsights(
  bookings: List<SpaBooking>,
  window: InsightsWindow,
  todayIso: String = currentTodayIso(),
): BusinessInsightsSummary {
  val windowDates = buildWindowDates(window = window, todayIso = todayIso)
  val bookingsByDate = bookings.groupBy { it.dateIso }

  val dailyPoints =
    windowDates.map { (iso, dayShort, fullLabel) ->
      val dayBookings = bookingsByDate[iso].orEmpty()
      val active = dayBookings.filter { it.status != "cancelled" }
      val cancelled = dayBookings.count { it.status == "cancelled" }
      DailyInsightPoint(
        dateIso = iso,
        dayLabel = dayShort,
        fullDateLabel = fullLabel,
        revenueUsd = active.sumOf { it.priceUsd },
        activeBookings = active.size,
        cancelledBookings = cancelled,
      )
    }

  val windowRevenue = dailyPoints.sumOf { it.revenueUsd }
  val windowActive = dailyPoints.sumOf { it.activeBookings }
  val windowCancelled = dailyPoints.sumOf { it.cancelledBookings }
  val windowTotalBookings = windowActive + windowCancelled
  val averageTicket = if (windowActive > 0) windowRevenue / windowActive else 0L

  val peakPoint = dailyPoints.maxByOrNull { it.revenueUsd }?.takeIf { it.revenueUsd > 0L }

  val allActiveBookings = bookings.filter { it.status != "cancelled" }
  val totalAllTimeRevenue = allActiveBookings.sumOf { it.priceUsd }
  val totalAllTimeActive = allActiveBookings.size
  val retentionRate =
    if (windowTotalBookings > 0) ((windowActive * 100f) / windowTotalBookings).toInt()
    else if (bookings.isNotEmpty()) ((totalAllTimeActive * 100f) / bookings.size).toInt()
    else 100

  val categoryBreakdown =
    allActiveBookings
      .groupBy { it.treatmentCategory.ifBlank { "Signature Ritual" } }
      .map { (category, list) ->
        val rev = list.sumOf { it.priceUsd }
        val share =
          if (totalAllTimeRevenue > 0L) (rev.toFloat() / totalAllTimeRevenue.toFloat())
          else 0f
        CategoryInsight(
          category = category,
          revenueUsd = rev,
          bookingCount = list.size,
          shareFraction = share.coerceIn(0f, 1f),
        )
      }
      .sortedByDescending { it.revenueUsd }

  val therapistLeaderboard =
    allActiveBookings
      .groupBy { it.therapistName.ifBlank { "Lead Specialist" } }
      .map { (name, list) ->
        TherapistInsight(
          therapistName = name,
          revenueUsd = list.sumOf { it.priceUsd },
          bookingCount = list.size,
        )
      }
      .sortedByDescending { it.revenueUsd }

  return BusinessInsightsSummary(
    window = window,
    dailyPoints = dailyPoints,
    windowRevenueUsd = windowRevenue,
    windowActiveBookings = windowActive,
    windowCancelledBookings = windowCancelled,
    averageTicketUsd = averageTicket,
    totalAllTimeRevenueUsd = totalAllTimeRevenue,
    totalAllTimeActiveBookings = totalAllTimeActive,
    retentionRatePercent = retentionRate,
    peakDayLabel = peakPoint?.fullDateLabel,
    peakDayRevenueUsd = peakPoint?.revenueUsd ?: 0L,
    categoryBreakdown = categoryBreakdown,
    therapistLeaderboard = therapistLeaderboard,
  )
}
