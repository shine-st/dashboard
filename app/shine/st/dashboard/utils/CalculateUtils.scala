package shine.st.dashboard.utils

import shine.st.dashboard.data.Model.DailyData
import org.joda.time.{DateTime, Duration}

/**
  * Created by shinest on 06/07/2017.
  */
object CalculateUtils {
  def bigDecimalFormatter(x: Double) = BigDecimal(x).setScale(0, BigDecimal.RoundingMode.HALF_UP).toInt

  def calculateAverage[A](list: List[A])(f: A => Int) = {
    bigDecimalFormatter(list.foldLeft(0.0)((a, b) => a + f(b)) / list.size)
  }

  def movingAverage(begin: DateTime, end: DateTime, source: List[DailyData])(implicit averageDays: Int) = {
    val d = new Duration(begin, end)

    (0 to d.getStandardDays.toInt).map(p => begin.plusDays(p)).map { date =>
      val data = source.find(_.date == date).get

      val period = (1 to averageDays).map(day => date.minusDays(day)).map(minus => source.find(_.date == minus).get).toList
      val visitorAverage = calculateAverage(period)(d => d.visitor)
      val pageViewAverage = calculateAverage(period)(d => d.pageView)
      val amountAverage = calculateAverage(period)(d => d.amount)
      val orderCountAverage = calculateAverage(period)(d => d.orderCount)
      val customerAverage = calculateAverage(period)(d => d.customer)
      DailyData(date, visitorAverage, pageViewAverage, amountAverage, orderCountAverage, customerAverage, data.category, data.device)

    }.toList

  }


}
