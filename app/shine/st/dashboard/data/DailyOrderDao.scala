package shine.st.dashboard.data

import java.sql.{PreparedStatement, ResultSet}
import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Configuration
import shine.st.common.DateTimeUtils
import shine.st.dashboard.data.Model.{Category, DailyOrder, Source}
import shine.st.dashboard.utils.memo.CategoryMemo

/**
  * Created by shinest on 04/07/2017.
  */
@Singleton
class DailyOrderDao @Inject()(categoryMemo: CategoryMemo, override val config: Configuration) extends DemoDao[DailyOrder] {
  override val insertSql: String = "insert into DailyOrder values (str_to_date(?, '%Y-%m-%d %T'), ?, ?, ?, ?, ?)"


  override def setData(data: DailyOrder, ps: PreparedStatement): Unit = {
    ps.setString(1, DateTimeUtils.formatDate(data.date))
    ps.setInt(2, data.amount)
    ps.setInt(3, data.orderCount)
    ps.setInt(4, data.customer)
    ps.setInt(5, data.category.id)
    ps.setString(6, data.source.toString)
  }

  override def generate(rs: ResultSet): DailyOrder = {
    val category = categoryMemo.get(rs.getInt("category_id"))
    DailyOrder(new DateTime(rs.getTimestamp("date").getTime), rs.getInt("amount"), rs.getInt("order_count"), rs.getInt("customer"), category, Source.withName(rs.getString("source")))
  }

  def queryByChart(begin: DateTime, end: DateTime, category: Category) = {
    val sql = "select * from DailyOrder where date >= str_to_date(? ,'%Y-%m-%d %T') and date <= str_to_date(? ,'%Y-%m-%d %T') and category_id = ?"
    query(sql) { ps =>
      ps.setString(1, DateTimeUtils.formatDate(begin))
      ps.setString(2, DateTimeUtils.formatDate(end))
      ps.setInt(3, category.id)
    }
  }
}

