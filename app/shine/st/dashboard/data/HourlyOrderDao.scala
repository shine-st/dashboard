package shine.st.dashboard.data

import java.sql.{PreparedStatement, ResultSet}
import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Configuration
import shine.st.common.DateTimeUtils
import shine.st.dashboard.data.Model._
import shine.st.dashboard.utils.memo.CategoryMemo

/**
  * Created by shinest on 04/07/2017.
  */
@Singleton
class HourlyOrderDao @Inject()(categoryMemo: CategoryMemo, override val config: Configuration) extends DemoDao[HourlyOrder] {
  override val insertSql: String = "insert into HourlyOrder values (str_to_date(?, '%Y-%m-%d %T'), ?, ?, ?, ?, ?, ?)"


  override def setData(data: HourlyOrder, ps: PreparedStatement): Unit = {
    ps.setString(1, DateTimeUtils.formatDate(data.date))
    ps.setInt(2, data.hour)
    ps.setInt(3, data.amount)
    ps.setInt(4, data.orderCount)
    ps.setInt(5, data.customer)
    ps.setInt(6, data.category.id)
    ps.setString(7, data.source.toString)
  }

  override def generate(rs: ResultSet): HourlyOrder = {
    val category = categoryMemo.get(rs.getInt("category_id"))
    val hour = rs.getInt("hour")
    HourlyOrder(new DateTime(rs.getTimestamp("date").getTime).plusHours(hour), hour, rs.getInt("amount"), rs.getInt("order_count"), rs.getInt("customer"), category, Source.withName(rs.getString("source")))
  }

  def queryByDate(date: DateTime, hour: Int, category: Category) = {
    val sql = "select * from HourlyOrder where date = str_to_date(? ,'%Y-%m-%d %T') and category_id = ? and hour <= ?"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(date))
      ps.setInt(2, category.id)
      ps.setInt(3, hour)
    }
  }
}