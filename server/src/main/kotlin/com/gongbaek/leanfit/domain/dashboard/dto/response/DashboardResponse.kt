package com.gongbaek.leanfit.domain.dashboard.dto.response

import kotlinx.serialization.Serializable

/**
 * 월간 요약 응답
 * GET /dashboard/summary
 */
@Serializable
data class DashboardSummaryResponse(
    val year: Int,
    val month: Int,
    val totalAmount: Double,
    val activeCount: Int,
    val upcomingPayments: List<UpcomingPayment>,
    val categoryBreakdown: List<CategoryBreakdown>,
    val comparedToLastMonth: MonthComparison?,
)

@Serializable
data class UpcomingPayment(
    val id: String,
    val name: String,
    val amount: Double,
    val paymentDate: String,
    val dDay: Int,
)

@Serializable
data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val count: Int,
    val percentage: Double,
)

@Serializable
data class MonthComparison(
    val amountDiff: Double,
    val percentageDiff: Double,
)

/**
 * 캘린더 뷰 응답
 * GET /dashboard/calendar
 */
@Serializable
data class CalendarResponse(
    val year: Int,
    val month: Int,
    val payments: List<DayPayments>,
)

@Serializable
data class DayPayments(
    val day: Int,
    val subscriptions: List<CalendarSubscription>,
    val totalAmount: Double,
)

@Serializable
data class CalendarSubscription(
    val id: String,
    val name: String,
    val amount: Double,
    val iconUrl: String?,
)
