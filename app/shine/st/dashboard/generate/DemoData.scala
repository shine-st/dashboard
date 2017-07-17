package shine.st.dashboard.generate

import org.joda.time.{DateTime, Duration}
import shine.st.dashboard.data.Model._
import shine.st.dashboard.data._
import shine.st.dashboard.utils.CalculateUtils.bigDecimalFormatter
import shine.st.dashboard.utils.RandomUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration => FutureDuration}
import scala.concurrent.{Await, Future}

/**
  * Created by shinest on 04/07/2017.
  */


object DemoData extends PlayInjector {
  val wholeSite = Category(1, "全站")
  val categoryList = List(Category(2, "A"), Category(3, "B"), Category(4, "C"), Category(5, "D"))
  val device = List(Device.WEB, Device.APP)
  val source = List(Source.DIRECT, Source.EDM, Source.GOOGLE_AD, Source.FACEBOOK_AD)

  val dailyDataDao = injector.instanceOf[DailyDataDao]
  val dailyOrderDao = injector.instanceOf[DailyOrderDao]
  val hourlyDataDao = injector.instanceOf[HourlyDataDao]
  val hourlyOrderDao = injector.instanceOf[HourlyOrderDao]

  def main(args: Array[String]): Unit = {

    val allDaily = generateAllDaily(new DateTime(2016, 6, 1, 0, 0, 0, 0), new DateTime(2017, 12, 31, 0, 0, 0, 0))
    println("daily shine.st.dashboard.data shine.st.dashboard.generate finish")

    val dailyData = allDaily.flatMap(_._1)
    val f1 = Future(dailyDataDao.insertBatch(dailyData))

    val dailyOrder = allDaily.flatMap(_._2)
    val f2 = Future(dailyOrderDao.insertBatch(dailyOrder))

    val hourlyData = generateHourlyData(dailyData)
    val f3 = Future(hourlyDataDao.insertBatch(hourlyData))
    println("hourly shine.st.dashboard.data shine.st.dashboard.generate finish")

    val hourlyOrder = generateHourlyOrder(dailyOrder)
    val f4 = Future(hourlyOrderDao.insertBatch(hourlyOrder))
    println("hourly order shine.st.dashboard.generate finish")


    val result = Await.result(Future.sequence(Seq(f1, f2, f3, f4)), FutureDuration.Inf)

    println(result)

    closeInjector
  }

  def generateHourlyData(list: List[DailyData]) = {
    val wholeSiteDailyData = list.filter(_.category == wholeSite)
    val categoryDailyData = list.filterNot(_.category == wholeSite)

    val categoryHourlyDataWithPercentage = categoryDailyData.groupBy(d => (d.date, d.device)).mapValues { dl =>
      dl.flatMap { d =>
        val hourDistribute = hourlyPercentage(1, ((1 to 5).toList, 5)) ++: hourlyPercentage(2, ((6 to 11).toList, 11), ((12 to 17).toList, 17), ((0 :: (18 to 23).toList), 24))
        val (visitor, pageView, amount, orderCount, customer) = (d.visitor, d.pageView, d.amount, d.orderCount, d.customer)
        hourDistribute.map { case (hour, dis) =>
          val percentage = dis / 100.0
          (HourlyData(d.date, hour, bigDecimalFormatter(visitor * percentage), bigDecimalFormatter(pageView * percentage), bigDecimalFormatter(amount * percentage), bigDecimalFormatter(orderCount * percentage), bigDecimalFormatter(customer * percentage), d.category, d.device), percentage)
        }
      }
    }

    val wholeSitePercentage = categoryHourlyDataWithPercentage.mapValues { d =>
      d.groupBy(dd => dd._1.hour).mapValues(dd => dd.foldLeft(0.0)((a, b) => a + b._2) / dd.size)
    }

    val wholeSiteHourlyData = wholeSiteDailyData.flatMap { d =>
      val hourDisribute = wholeSitePercentage((d.date, d.device)).toList
      val (visitor, pageView, amount, orderCount, customer) = (d.visitor, d.pageView, d.amount, d.orderCount, d.customer)
      hourDisribute.map { case (hour, percentage) =>
        HourlyData(d.date, hour, bigDecimalFormatter(visitor * percentage), bigDecimalFormatter(pageView * percentage), bigDecimalFormatter(amount * percentage), bigDecimalFormatter(orderCount * percentage), bigDecimalFormatter(customer * percentage), d.category, d.device)
      }
    }

    val categoryHourlyData = categoryHourlyDataWithPercentage.flatMap { case (k, v) => v.map(_._1) }.toList

    categoryHourlyData ++: wholeSiteHourlyData

  }


  def generateHourlyOrder(list: List[DailyOrder]) = {
    val wholeSiteDailyData = list.filter(_.category == wholeSite)
    val categoryDailyData = list.filterNot(_.category == wholeSite)

    val categoryHourlyDataWithPercentage = categoryDailyData.groupBy(d => (d.date)).mapValues { dl =>
      dl.flatMap { d =>
        val hourDistribute = hourlyPercentage(1, ((1 to 5).toList, 5)) ++: hourlyPercentage(2, ((6 to 11).toList, 11), ((12 to 17).toList, 17), ((0 :: (18 to 23).toList), 24))
        val (amount, orderCount, customer) = (d.amount, d.orderCount, d.customer)
        hourDistribute.map { case (hour, dis) =>
          val percentage = dis / 100.0
          (HourlyOrder(d.date, hour, bigDecimalFormatter(amount * percentage), bigDecimalFormatter(orderCount * percentage), bigDecimalFormatter(customer * percentage), d.category, d.source), percentage)
        }
      }
    }

    val wholeSitePercentage = categoryHourlyDataWithPercentage.mapValues { d =>
      d.groupBy(dd => dd._1.hour).mapValues(dd => dd.foldLeft(0.0)((a, b) => a + b._2) / dd.size)
    }

    val wholeSiteHourlyData = wholeSiteDailyData.flatMap { d =>
      val hourDisribute = wholeSitePercentage(d.date).toList
      val (amount, orderCount, customer) = (d.amount, d.orderCount, d.customer)
      hourDisribute.map { case (hour, percentage) =>
        HourlyOrder(d.date, hour, bigDecimalFormatter(amount * percentage), bigDecimalFormatter(orderCount * percentage), bigDecimalFormatter(customer * percentage), d.category, d.source)
      }
    }

    val categoryHourlyData = categoryHourlyDataWithPercentage.flatMap { case (k, v) => v.map(_._1) }.toList

    categoryHourlyData ++: wholeSiteHourlyData

  }


  def hourlyPercentage(base: Int, period: (List[Int], Int)*) = {
    period.flatMap { case (hour, quota) =>
      distributionQuota(hour, quota, base)
    }
  }

  def distributionQuota[A](list: List[A], quota: Int, base: Int): List[(A, Int)] = {
    if (list.size == 1) {
      list.map(s => (s, quota + base))
    } else {
      val index = RandomUtils.randomInt(list.size) - 1
      val percentage = RandomUtils.betweenRandom(1, quota)
      val data = list(index)
      (data, percentage + base) :: distributionQuota(list.filterNot(_ == data), quota - percentage, base)
    }
  }


  def generateAllDaily(begin: DateTime, end: DateTime) = {
    val d = new Duration(begin, end)

    (0 to d.getStandardDays.toInt).map { plusDay =>
      val dateTime = begin.plusDays(plusDay)
      val visitor = RandomUtils.betweenRandom(50000, 150000)
      val conversionRate = RandomUtils.betweenRandomDouble(1.0, 6.0)
      val raw = for (
        (_, id) <- List.fill(visitor)(0).zipWithIndex;
        c <- viewList(categoryList);
        d <- viewList(device)
      ) yield {
        val pageView = RandomUtils.randomInt(10)
        val conversionRateWithViewPageCount = 1 - scala.math.pow((1 - conversionRate / 100), pageView)
        if (RandomUtils.randomBoolean(conversionRateWithViewPageCount)) {
          val amount = RandomUtils.betweenRandom(100, 5000)
          RawData(id, dateTime, pageView, c, d, Some(amount))
        } else
          RawData(id, dateTime, pageView, c, d)
      }

      (generateDailyData(dateTime, raw).toList, generateDailyOrder(dateTime, raw))
    }.toList
  }

  def generateDailyData(dateTime: DateTime, raw: List[RawData]) = {
    val categoryDailyData = raw.groupBy(r => (r.category, r.device))
      .map { case (k, v) =>
        val pageView = v.foldLeft(0)((a, b) => a + b.pageView)
        val orders = v.filter(_.amount.isDefined)
        val amount = orders.foldLeft(0)((a, b) => a + b.amount.get)

        DailyData(dateTime, v.size, pageView, amount, orders.size, orders.size, k._1, k._2)
      }

    val wholeSiteDailyData = raw.groupBy(r => r.device)
      .map { case (k, v) =>
        val visitor = v.map(_.id).distinct.size
        val pageView = v.foldLeft(0)((a, b) => a + b.pageView)
        val orders = v.filter(_.amount.isDefined)
        val amount = orders.foldLeft(0)((a, b) => a + b.amount.get)
        val customers = orders.map(_.id).distinct.size

        DailyData(dateTime, visitor, pageView, amount, orders.size, customers, wholeSite, k)
      }

    categoryDailyData ++: wholeSiteDailyData

  }

  def generateDailyOrder(dateTime: DateTime, raw: List[RawData]) = {

    (raw.groupBy(_.category) + (wholeSite -> raw))
      .flatMap { case (c, v) =>
        val orders = v.filter(_.amount.isDefined)
        val amount = orders.foldLeft(0)((a, b) => a + b.amount.get)
        val customers = orders.map(_.id).distinct.size

        distributionQuota(source, 80, 5)
          .map { case (s, quota) =>
            val percentage = quota / 100.0
            DailyOrder(dateTime, bigDecimalFormatter(amount * percentage), bigDecimalFormatter(orders.size * percentage), bigDecimalFormatter(customers * percentage), c, s)
          }
      }.toList
  }

  def viewList[A](list: List[A]) = {
    val viewCount = RandomUtils.randomInt(list.size)

    def take(count: Int, takeList: List[A]): List[A] = {
      if (count == 1)
        List(takeList(RandomUtils.randomInt(takeList.size) - 1))
      else {
        val data = takeList(RandomUtils.randomInt(takeList.size) - 1)
        data :: take(count - 1, takeList.filterNot(_ == data))
      }
    }

    if (viewCount == list.size)
      list
    else
      take(viewCount, list)
  }

}
