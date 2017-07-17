package shine.st.dashboard.data

import org.joda.time.DateTime

/**
  * Created by shinest on 04/07/2017.
  */
object Model {

  object TimeGroup extends Enumeration {
    type TimeTag = Value
    val HOURLY, DAILY, MONTH = Value
  }

  object Device extends Enumeration {
    type Device = Value
    val WEB, APP = Value
  }

  import Device._

  object Source extends Enumeration {
    type Source = Value
    val DIRECT, EDM, GOOGLE_AD, FACEBOOK_AD = Value
  }

  import Source._

  case class Category(id: Int, name: String)

  case class RawData(id: Int, time: DateTime, pageView: Int, category: Category, device: Device, amount: Option[Int] = None)

  case class DailyData(date: DateTime, visitor: Int, pageView: Int, amount: Int, orderCount: Int, customer: Int, category: Category, device: Device)

  case class HourlyData(date: DateTime, hour: Int, visitor: Int, pageView: Int, amount: Int, orderCount: Int, customer: Int, category: Category, device: Device)

  case class DailyOrder(date: DateTime, amount: Int, orderCount: Int, customer: Int, category: Category, source: Source)

  case class HourlyOrder(date: DateTime, hour: Int, amount: Int, orderCount: Int, customer: Int, category: Category, source: Source)

  case class Summarize(date: DateTime, visitor: Int, conversionRate: Double, device: Device)

  case class Tag(id: Int, name: String, count: Int)

}


