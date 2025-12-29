package com.gongbaek.leanfit.domain.dashboard.service

import com.gongbaek.leanfit.domain.dashboard.dto.response.CalendarResponse
import com.gongbaek.leanfit.domain.dashboard.dto.response.CalendarSubscription
import com.gongbaek.leanfit.domain.dashboard.dto.response.CategoryBreakdown
import com.gongbaek.leanfit.domain.dashboard.dto.response.DashboardSummaryResponse
import com.gongbaek.leanfit.domain.dashboard.dto.response.DayPayments
import com.gongbaek.leanfit.domain.dashboard.dto.response.MonthComparison
import com.gongbaek.leanfit.domain.dashboard.dto.response.UpcomingPayment
import com.gongbaek.leanfit.domain.subscription.entity.Subscription
import com.gongbaek.leanfit.domain.subscription.entity.SubscriptionStatus
import com.gongbaek.leanfit.domain.subscription.repository.SubscriptionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import java.util.UUID

class DashboardService(
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepository(),
) {
    /**
     * 월간 요약
     * GET /dashboard/summary
     */
    fun getSummary(
        userId: UUID,
        year: Int?,
        month: Int?,
    ): DashboardSummaryResponse {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val targetYear = year ?: today.year
        val targetMonth = month ?: today.monthNumber

        val activeSubscriptions =
            subscriptionRepository
                .findAllByUserId(userId, SubscriptionStatus.ACTIVE)

        val totalMonthlyAmount = calculateMonthlyAmount(activeSubscriptions)

        val upcomingPayments =
            activeSubscriptions
                .filter { it.nextPaymentDate != null }
                .sortedBy { it.nextPaymentDate }
                .take(5)
                .map { subscription ->
                    val dDay = today.daysUntil(subscription.nextPaymentDate!!)
                    UpcomingPayment(
                        id = subscription.id.toString(),
                        name = subscription.serviceName,
                        amount = subscription.amount.toDouble(),
                        paymentDate = subscription.nextPaymentDate.toString(),
                        dDay = dDay,
                    )
                }

        val categoryBreakdown = calculateCategoryBreakdown(activeSubscriptions, totalMonthlyAmount)

        // 이전 달 데이터 비교
        val comparedToLastMonth = calculateMonthComparison(activeSubscriptions, totalMonthlyAmount, targetYear, targetMonth)

        return DashboardSummaryResponse(
            year = targetYear,
            month = targetMonth,
            totalAmount = totalMonthlyAmount,
            activeCount = activeSubscriptions.size,
            upcomingPayments = upcomingPayments,
            categoryBreakdown = categoryBreakdown,
            comparedToLastMonth = comparedToLastMonth,
        )
    }

    /**
     * 캘린더 뷰
     * GET /dashboard/calendar
     */
    fun getCalendar(
        userId: UUID,
        year: Int?,
        month: Int?,
    ): CalendarResponse {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val targetYear = year ?: today.year
        val targetMonth = month ?: today.monthNumber

        val activeSubscriptions =
            subscriptionRepository
                .findAllByUserId(userId, SubscriptionStatus.ACTIVE)

        val paymentsByDay =
            activeSubscriptions
                .groupBy { it.billingDay }
                .map { (day, subscriptions) ->
                    DayPayments(
                        day = day,
                        subscriptions =
                            subscriptions.map { subscription ->
                                CalendarSubscription(
                                    id = subscription.id.toString(),
                                    name = subscription.serviceName,
                                    amount = subscription.amount.toDouble(),
                                    iconUrl = subscription.iconUrl,
                                )
                            },
                        totalAmount = subscriptions.sumOf { it.amount.toDouble() },
                    )
                }
                .sortedBy { it.day }

        return CalendarResponse(
            year = targetYear,
            month = targetMonth,
            payments = paymentsByDay,
        )
    }

    private fun calculateMonthlyAmount(subscriptions: List<Subscription>): Double =
        subscriptions.sumOf { subscription ->
            when (subscription.billingCycle.name) {
                "MONTHLY" -> subscription.amount.toDouble()
                "YEARLY" -> subscription.amount.toDouble() / 12
                "WEEKLY" -> subscription.amount.toDouble() * 4
                else -> subscription.amount.toDouble()
            }
        }

    private fun calculateCategoryBreakdown(
        subscriptions: List<Subscription>,
        totalAmount: Double,
    ): List<CategoryBreakdown> {
        if (totalAmount == 0.0) return emptyList()

        return subscriptions
            .groupBy { it.category?.name ?: "OTHER" }
            .map { (category, subs) ->
                val categoryAmount =
                    subs.sumOf { subscription ->
                        when (subscription.billingCycle.name) {
                            "MONTHLY" -> subscription.amount.toDouble()
                            "YEARLY" -> subscription.amount.toDouble() / 12
                            "WEEKLY" -> subscription.amount.toDouble() * 4
                            else -> subscription.amount.toDouble()
                        }
                    }

                CategoryBreakdown(
                    category = category,
                    amount = categoryAmount,
                    count = subs.size,
                    percentage = (categoryAmount / totalAmount * 100).let { "%.1f".format(it).toDouble() },
                )
            }
            .sortedByDescending { it.amount }
    }

    /**
     * 이전 달과의 비교 계산
     * - 현재 달에 활성화되어 있으면서 이전 달에도 존재했던 구독만 비교
     */
    private fun calculateMonthComparison(
        currentSubscriptions: List<Subscription>,
        currentAmount: Double,
        targetYear: Int,
        targetMonth: Int,
    ): MonthComparison? {
        // 이전 달 첫 날 계산
        val targetDate = LocalDate(targetYear, targetMonth, 1)
        val lastMonthDate = targetDate.minus(1, DateTimeUnit.MONTH)

        // 이전 달에도 존재했던 구독 필터링 (시작일이 이전 달 이전인 구독들)
        val lastMonthSubscriptions =
            currentSubscriptions.filter { subscription ->
                subscription.startDate?.let { startDate ->
                    startDate <= lastMonthDate
                } ?: true // 시작일이 없으면 이전부터 있었다고 가정
            }

        val lastMonthAmount = calculateMonthlyAmount(lastMonthSubscriptions)

        // 이전 달 데이터가 없으면 비교 불가
        if (lastMonthAmount == 0.0 && currentAmount == 0.0) {
            return null
        }

        val amountDiff = currentAmount - lastMonthAmount
        val percentageDiff =
            if (lastMonthAmount == 0.0) {
                if (currentAmount > 0) 100.0 else 0.0
            } else {
                ((currentAmount - lastMonthAmount) / lastMonthAmount * 100)
                    .let { "%.1f".format(it).toDouble() }
            }

        return MonthComparison(
            amountDiff = amountDiff,
            percentageDiff = percentageDiff,
        )
    }
}
