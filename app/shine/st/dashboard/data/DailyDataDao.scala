package shine.st.dashboard.data

import java.sql.{PreparedStatement, ResultSet}
import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Configuration
import shine.st.common.DateTimeUtils
import shine.st.dashboard.data.Model.Device.Device
import shine.st.dashboard.data.Model.{Category, DailyData, Device}
import shine.st.dashboard.utils.memo.CategoryMemo

/**
  * Created by shinest on 04/07/2017.
  */
@Singleton
class DailyDataDao @Inject()(categoryMemo: CategoryMemo, override val config: Configuration) extends DemoDao[DailyData] {
  override val insertSql: String = "insert into DailyData values (str_to_date(?, '%Y-%m-%d %T'), ?, ?, ?, ?, ?, ?, ?)"

  override def setData(data: DailyData, ps: PreparedStatement): Unit = {
    ps.setString(1, DateTimeUtils.formatDate(data.date))
    ps.setInt(2, data.visitor)
    ps.setInt(3, data.pageView)
    ps.setInt(4, data.amount)
    ps.setInt(5, data.orderCount)
    ps.setInt(6, data.customer)
    ps.setInt(7, data.category.id)
    ps.setString(8, data.device.toString)
  }


  def queryByChart(begin: DateTime, end: DateTime, category: Category, device: Device) = {
    val sql = "select * from DailyData where date >= str_to_date(? ,'%Y-%m-%d %T') and date <= str_to_date(? ,'%Y-%m-%d %T') and category_id = ? and device = ?"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(begin))
      ps.setString(2, DateTimeUtils.formatDate(end))
      ps.setInt(3, category.id)
      ps.setString(4, device.toString)
    }
  }

  def queryByDate(begin: DateTime, end: DateTime) = {
    val sql = "select * from DailyData where date >= str_to_date(? ,'%Y-%m-%d %T') and date <= str_to_date(? ,'%Y-%m-%d %T')"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(begin))
      ps.setString(2, DateTimeUtils.formatDate(end))
    }
  }

  override def generate(rs: ResultSet): DailyData = {
    val category = categoryMemo.get(rs.getInt("category_id"))
    DailyData(new DateTime(rs.getTimestamp("date").getTime), rs.getInt("visitor"), rs.getInt("page_view"), rs.getInt("amount"), rs.getInt("order_count"), rs.getInt("customer"), category, Device.withName(rs.getString("device")))
  }
}