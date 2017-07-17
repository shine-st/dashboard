package shine.st.dashboard.data

import java.sql.{PreparedStatement, ResultSet}
import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Configuration
import shine.st.common.DateTimeUtils
import shine.st.dashboard.data.Model.Device.Device
import shine.st.dashboard.data.Model.{Category, Device, HourlyData}
import shine.st.dashboard.utils.memo.CategoryMemo

/**
  * Created by shinest on 04/07/2017.
  */
@Singleton
class HourlyDataDao @Inject()(categoryMemo: CategoryMemo, override val config: Configuration) extends DemoDao[HourlyData] {
  override val insertSql: String = "insert into HourlyData values (str_to_date(?, '%Y-%m-%d %T'), ?, ?, ?, ?, ?, ?, ?, ?)"


  override def setData(data: HourlyData, ps: PreparedStatement): Unit = {
    ps.setString(1, DateTimeUtils.formatDate(data.date))
    ps.setInt(2, data.hour)
    ps.setInt(3, data.visitor)
    ps.setInt(4, data.pageView)
    ps.setInt(5, data.amount)
    ps.setInt(6, data.orderCount)
    ps.setInt(7, data.customer)
    ps.setInt(8, data.category.id)
    ps.setString(9, data.device.toString)
  }

  override def generate(rs: ResultSet): HourlyData = {
    val category = categoryMemo.get(rs.getInt("category_id"))
    val hour = rs.getInt("hour")
    HourlyData(new DateTime(rs.getTimestamp("date").getTime).plusHours(hour), hour, rs.getInt("visitor"), rs.getInt("page_view"), rs.getInt("amount"), rs.getInt("order_count"), rs.getInt("customer"), category, Device.withName(rs.getString("device")))
  }

  def queryByDate(date: DateTime, hour: Int, category: Category, device: Device) = {
    val sql = "select * from HourlyData where date = str_to_date(? ,'%Y-%m-%d %T') and category_id = ? and device = ? and hour <= ?"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(date))
      ps.setInt(2, category.id)
      ps.setString(3, device.toString)
      ps.setInt(4, hour)
    }
  }

  def queryByDate(date: DateTime, hour: Int, category: Category) = {
    val sql = "select * from HourlyData where date = str_to_date(? ,'%Y-%m-%d %T') and category_id = ? and hour <= ?"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(date))
      ps.setInt(2, category.id)
      ps.setInt(3, hour)
    }
  }
}