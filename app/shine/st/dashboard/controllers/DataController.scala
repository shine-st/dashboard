package shine.st.dashboard.controllers

import javax.inject._

import org.joda.time.DateTime
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter
import shine.st.common.DateTimeUtils
import shine.st.dashboard.data.Model._
import shine.st.dashboard.data.{DailyDataDao, DailyOrderDao, _}
import shine.st.dashboard.utils.CalculateUtils
import shine.st.dashboard.utils.memo.CategoryMemo


/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class DataController @Inject()(categoryMemo: CategoryMemo,
                               hourlyOrderDao: HourlyOrderDao,
                               dailyOrderDao: DailyOrderDao,
                               hourlyDataDao: HourlyDataDao,
                               dailyDataDao: DailyDataDao,
                               cc: ControllerComponents,
                               cache: SyncCacheApi) extends AbstractController(cc) {

  val emptyJson = Json.obj()

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  def device() = Action { implicit request: Request[AnyContent] =>
    val begin = DateTimeUtils.parseDate(request.getQueryString("begin").get).get
    val end = DateTimeUtils.parseDate(request.getQueryString("end").get).get
    val category = categoryMemo.get(request.getQueryString("categoryId").map(_.toInt).get)
    val device = Device.withName(request.getQueryString("device").get)
    val timeGroup = TimeGroup.withName(request.getQueryString("timeGroup").get)

    val result = timeGroup match {
      case TimeGroup.DAILY =>
        val allDailyDataList = dailyDataDao.queryByChart(begin.minusDays(movingAverageDays), end, category, device)
        val averageDailyDataList = CalculateUtils.movingAverage(begin, end, allDailyDataList)

        val dailyDataList = allDailyDataList.filter(_.date.getMillis >= begin.getMillis)
        Json.obj("daily" -> dailyDataList, "average" -> averageDailyDataList)

      case TimeGroup.MONTH =>
        val allDailyDataList = dailyDataDao.queryByChart(begin.dayOfMonth().withMinimumValue(), end, category, device)
        val monthDataList = allDailyDataList.groupBy(_.date.monthOfYear().get()).map { case (month, l) =>
          val visitor = l.foldLeft(0)((a, b) => a + b.visitor)
          val pageView = l.foldLeft(0)((a, b) => a + b.pageView)
          val amount = l.foldLeft(0)((a, b) => a + b.amount)
          val orderCount = l.foldLeft(0)((a, b) => a + b.orderCount)
          val customer = l.foldLeft(0)((a, b) => a + b.customer)
          val date = l.head.date.dayOfMonth().withMinimumValue()

          DailyData(date, visitor, pageView, amount, orderCount, customer, category, device)
        }.toList.sortWith(_.date.getMillis <= _.date.getMillis)

        Json.obj("month" -> monthDataList)

      case _ =>
        Json.obj("message" -> "wrong query string")
    }

    Ok(result)

  }

  def todayDeviceHourly() = Action { implicit request: Request[AnyContent] =>
    val category = categoryMemo.get(request.getQueryString("categoryId").map(_.toInt).get)
    val device = Device.withName(request.getQueryString("device").get)
    val today = new DateTime
    val hour = today.hourOfDay().get
    val hourlyDataList = hourlyDataDao.queryByDate(today, hour, category, device).sortWith(_.hour <= _.hour)

    Ok(Json.obj("hourly" -> hourlyDataList))
  }

  def orderSource() = Action { implicit request: Request[AnyContent] =>
    val begin = DateTimeUtils.parseDate(request.getQueryString("begin").get).get
    val end = DateTimeUtils.parseDate(request.getQueryString("end").get).get
    val category = categoryMemo.get(request.getQueryString("categoryId").map(_.toInt).get)
    val timeGroup = TimeGroup.withName(request.getQueryString("timeGroup").get)


    val result = timeGroup match {
      case TimeGroup.DAILY =>
        dailyOrderDao.queryByChart(begin, end, category).groupBy(_.date).mapValues(_.sortBy(_.source)).toList.sortWith(_._1.getMillis <= _._1.getMillis).flatMap(_._2)

      case TimeGroup.MONTH =>
        dailyOrderDao.queryByChart(begin.dayOfMonth().withMinimumValue(), end, category)
          .groupBy { d =>
            new DateTime().millisOfDay().withMinimumValue().withYear(d.date.getYear).withMonthOfYear(d.date.monthOfYear.get).withDayOfMonth(1)
          }.mapValues { l =>
          l.groupBy(_.source).map { case (source, l) =>
            val amount = l.foldLeft(0)((a, b) => a + b.amount)
            val orderCount = l.foldLeft(0)((a, b) => a + b.orderCount)
            val customer = l.foldLeft(0)((a, b) => a + b.customer)
            val date = l.head.date.dayOfMonth().withMinimumValue()
            DailyOrder(date, amount, orderCount, customer, category, source)
          }.toList.sortBy(_.source)
        }.toList.sortWith(_._1.getMillis <= _._1.getMillis).flatMap(_._2)
    }

    Ok(Json.toJson(result))
  }


  def todayOrderSourceHourly() = Action { implicit request: Request[AnyContent] =>
    val today = new DateTime
    val hour = today.hourOfDay().get
    val category = categoryMemo.get(request.getQueryString("categoryId").map(_.toInt).get)
    val timeGroup = TimeGroup.withName(request.getQueryString("timeGroup").get)


    val allHourlyOrderList = hourlyOrderDao.queryByDate(today, hour, category).groupBy(_.hour).mapValues(_.sortBy(_.source)).toList.sortWith(_._1 <= _._1).flatMap(_._2)

    Ok(Json.toJson(allHourlyOrderList))
  }

  def orderCustomer() = Action { implicit request: Request[AnyContent] =>
    val begin = DateTimeUtils.parseDate(request.getQueryString("begin").get).get
    val end = DateTimeUtils.parseDate(request.getQueryString("end").get).get
    val category = categoryMemo.get(request.getQueryString("categoryId").map(_.toInt).get)
    val timeGroup = TimeGroup.withName(request.getQueryString("timeGroup").get)


    val result = timeGroup match {
      case TimeGroup.DAILY =>
        dailyOrderDao.queryByChart(begin, end, category)
          .groupBy(o => (o.category, o.date))
          .mapValues(o => (o.foldLeft(0.toLong)((a, b) => a + b.amount), o.foldLeft(0)((a, b) => a + b.customer)))
          .toList.sortWith(_._1._2.getMillis <= _._1._2.getMillis)
          .map { case (k, v) =>
            Json.obj(
              "date" -> k._2,
              "category" -> k._1,
              "amount" -> v._1,
              "customer" -> v._2,
              "average" -> v._1 / v._2
            )
          }

      case TimeGroup.MONTH =>
        dailyOrderDao.queryByChart(begin.dayOfMonth().withMinimumValue(), end, category)
          .groupBy(o => (o.category, new DateTime().millisOfDay().withMinimumValue().withYear(o.date.getYear).withMonthOfYear(o.date.monthOfYear.get).withDayOfMonth(1)))
          .mapValues(o => (o.foldLeft(0.toLong)((a, b) => a + b.amount), o.foldLeft(0)((a, b) => a + b.customer)))
          .toList.sortWith(_._1._2.getMillis <= _._1._2.getMillis)
          .map { case (k, v) =>
            Json.obj(
              "date" -> k._2,
              "category" -> k._1,
              "amount" -> v._1,
              "customer" -> v._2,
              "average" -> v._1 / v._2
            )
          }
    }

    Ok(Json.toJson(result))
  }

  def todaySummarize() = Action { implicit request: Request[AnyContent] =>
    val today = new DateTime
    val hour = today.hourOfDay.get
    val percentage = today.minuteOfHour.get / 60.toDouble

    val result = hourlyDataDao.queryByDate(today, hour, categoryMemo.get(1))
      .groupBy(_.device)
      .map { case (d, list) =>
        val currentHour: HourlyData = list.filterNot(_.hour < hour)
          .map(h => h.copy(visitor = CalculateUtils.bigDecimalFormatter(h.visitor * percentage), pageView = CalculateUtils.bigDecimalFormatter(h.pageView * percentage), orderCount = CalculateUtils.bigDecimalFormatter(h.orderCount * percentage))).head
        val todayList = currentHour :: (list.filter(_.hour < hour))

        val visitor = todayList.foldLeft(0)((a, b) => a + b.visitor)
        val conversation = todayList.foldLeft(0)((a, b) => a + b.orderCount).toDouble / todayList.foldLeft(0)((a, b) => a + b.pageView)
        d.toString -> Summarize(today, visitor, conversation, d)
      }

    Ok(Json.toJson(result))
  }

  def customerTagIntersection() = Action { implicit request: Request[AnyContent] =>
    val xop = request.getQueryString("x").map(_.toInt)
    val yop = request.getQueryString("y").map(_.toInt)

    val count = for (
      x <- xop;
      y <- yop;
      intersection <- cache.get[List[List[Int]]]("member.tag.intersection")
    ) yield {
      intersection(x)(y)
    }

    Ok(Json.obj("count" -> count))
  }


  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.DataController.device,
        routes.javascript.DataController.todayDeviceHourly,
        routes.javascript.DataController.orderSource,
        routes.javascript.DataController.todayOrderSourceHourly,
        routes.javascript.DataController.orderCustomer,
        routes.javascript.DataController.todaySummarize,
        routes.javascript.DataController.customerTagIntersection
      )
    ).as("text/javascript")
  }

}
